/*
 * Spring Batch Plus
 *
 * Copyright 2022-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.batch.plus.job.metadata;

import static com.navercorp.spring.batch.plus.job.metadata.CheckMaxJobInstanceIdToDeleteTasklet.MAX_ID_KEY;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.buildJobParams;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateFrom;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestJobRepositoryConfig.class)
class CheckMaxJobInstanceIdToDeleteTaskletIT {

	@Autowired
	JobRepository jobRepository;

	CheckMaxJobInstanceIdToDeleteTasklet tasklet;

	@BeforeEach
	void setUp(@Autowired JobMetadataDao dao, @Autowired JobRepositoryTestUtils testUtils) {
		DateTimeFormatter baseDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		this.tasklet = new CheckMaxJobInstanceIdToDeleteTasklet(dao, "baseDate", baseDateFormatter);
		testUtils.removeJobExecutions();
	}

	@Test
	void testExecuteWhenNoMetadata() {
		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepContribution stepContribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		RepeatStatus repeatStatus = tasklet.execute(stepContribution, chunkContext);
		ExitStatus exitStatus = tasklet.afterStep(stepExecution);

		// then
		assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);
		assertThat(exitStatus).isEqualTo(CheckMaxJobInstanceIdToDeleteTasklet.EMPTY);
		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		assertThat(jobExecutionContext.containsKey(MAX_ID_KEY)).isFalse();
	}

	@Test
	void testExecuteWhenNeedToDelete() throws Exception {
		// given
		JobExecution execution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		execution1.setCreateTime(dateTo(2022, 3, 14));
		jobRepository.update(execution1);

		JobExecution execution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		execution2.setCreateTime(dateFrom(2022, 3, 15));
		jobRepository.update(execution2);

		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepContribution stepContribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

		// when
		RepeatStatus repeatStatus = tasklet.execute(stepContribution, chunkContext);
		ExitStatus exitStatus = tasklet.afterStep(stepExecution);

		// then
		assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);
		assertThat(exitStatus).isEqualTo(ExitStatus.COMPLETED);
		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		long maxJobInstanceId = jobExecutionContext.getLong(MAX_ID_KEY);
		assertThat(maxJobInstanceId).isEqualTo(execution1.getJobId());
	}

	@Test
	void testExecuteWhenNoNeedToDelete() throws Exception {
		// given
		JobExecution execution = jobRepository.createJobExecution("testJob2", buildJobParams());
		execution.setCreateTime(dateFrom(2022, 2, 15));
		jobRepository.update(execution);

		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/02/15")
			.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepContribution stepContribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

		// when
		RepeatStatus repeatStatus = tasklet.execute(stepContribution, chunkContext);
		ExitStatus exitStatus = tasklet.afterStep(stepExecution);

		// then
		assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);
		assertThat(exitStatus).isEqualTo(CheckMaxJobInstanceIdToDeleteTasklet.EMPTY);
		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		assertThat(jobExecutionContext.containsKey(MAX_ID_KEY)).isFalse();
	}
}
