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

import static com.navercorp.spring.batch.plus.job.metadata.DeleteMetadataTasklet.DELETION_RANGE_LENGTH;
import static com.navercorp.spring.batch.plus.job.metadata.DeleteMetadataTasklet.LOW_ID_KEY;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.buildJobParams;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.randomBetween;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
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
class DeleteMetadataTaskletTest {

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobMetadataCountDao countDao;

	DeleteMetadataTasklet tasklet;

	@BeforeEach
	void setUp(@Autowired JobMetadataDao dao, @Autowired JobRepositoryTestUtils testUtils) {
		this.tasklet = new DeleteMetadataTasklet(dao);
		testUtils.removeJobExecutions();
	}

	@Test
	void testExecuteWhen1stStart() throws Exception {
		// given
		int countToCreate = randomBetween(10, 300);
		int countToDelete = countToCreate - randomBetween(1, countToCreate - 1);
		long lastJobInstanceId = 0;
		for (int i = 0; i < countToCreate; i++) {
			JobExecution jobExecution = jobRepository.createJobExecution("testJob" + i, buildJobParams());
			jobRepository.add(new StepExecution("testStep", jobExecution));
			lastJobInstanceId = jobExecution.getJobId();
		}

		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepContribution stepContribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		int countRemains = countToCreate - countToDelete;
		jobExecutionContext.putLong(CheckMaxJobInstanceIdToDeleteTasklet.MAX_ID_KEY, lastJobInstanceId - countRemains);

		// when
		tasklet.beforeStep(stepExecution);

		int repeatCount = 0;
		RepeatStatus repeatStatus = RepeatStatus.CONTINUABLE;
		while (repeatStatus != RepeatStatus.FINISHED) {
			repeatStatus = tasklet.execute(stepContribution, chunkContext);
			repeatCount++;
		}

		// then
		assertThat(stepContribution.getWriteCount()).isEqualTo(countToDelete);
		int expectedChunkCount = Double.valueOf(Math.ceil((double)countToDelete / DELETION_RANGE_LENGTH)).intValue();
		assertThat(repeatCount).isEqualByComparingTo(expectedChunkCount);

		assertThat(countDao.countJobInstances()).isEqualTo(countRemains);
		assertThat(countDao.countJobExecutions()).isEqualTo(countRemains);
		assertThat(countDao.countJobExecutionContexts()).isEqualTo(countRemains);
		assertThat(countDao.countJobExecutionParams()).isEqualTo(countRemains);
		assertThat(countDao.countStepExecutions()).isEqualTo(countRemains);
		assertThat(countDao.countStepExecutionContext()).isEqualTo(countRemains);
	}

	@Test
	void testExecuteWhenNothingToDelete() throws Exception {
		// given
		int countToCreate = randomBetween(10, 300);
		for (int i = 0; i < countToCreate; i++) {
			JobExecution jobExecution = jobRepository.createJobExecution("testJob" + i, buildJobParams());
			jobRepository.add(new StepExecution("testStep", jobExecution));
		}

		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepContribution stepContribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		jobExecutionContext.putLong(CheckMaxJobInstanceIdToDeleteTasklet.MAX_ID_KEY, 0);

		// when
		tasklet.beforeStep(stepExecution);

		int repeatCount = 0;
		RepeatStatus repeatStatus = RepeatStatus.CONTINUABLE;
		while (repeatStatus != RepeatStatus.FINISHED) {
			repeatStatus = tasklet.execute(stepContribution, chunkContext);
			repeatCount++;
		}

		// then
		assertThat(stepContribution.getWriteCount()).isEqualTo(0);
		assertThat(repeatCount).isEqualByComparingTo(1);

		assertThat(countDao.countJobInstances()).isEqualTo(countToCreate);
		assertThat(countDao.countJobExecutions()).isEqualTo(countToCreate);
		assertThat(countDao.countJobExecutionContexts()).isEqualTo(countToCreate);
		assertThat(countDao.countJobExecutionParams()).isEqualTo(countToCreate);
		assertThat(countDao.countStepExecutions()).isEqualTo(countToCreate);
		assertThat(countDao.countStepExecutionContext()).isEqualTo(countToCreate);
	}

	@Test
	void testExecuteWhenRestart() throws Exception {
		// given
		int countToCreate = randomBetween(10, 300);
		long lastJobInstanceId = 0;
		for (int i = 0; i < countToCreate; i++) {
			JobExecution jobExecution = jobRepository.createJobExecution("testJob" + i, buildJobParams());
			jobRepository.add(new StepExecution("testStep", jobExecution));
			lastJobInstanceId = jobExecution.getJobId();
		}

		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepContribution stepContribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		jobExecutionContext.putLong(CheckMaxJobInstanceIdToDeleteTasklet.MAX_ID_KEY, lastJobInstanceId);

		long lowJobInstanceIdLastExecution = lastJobInstanceId - randomBetween(1L, countToCreate - 1);
		stepExecution.getExecutionContext().putLong(LOW_ID_KEY, lowJobInstanceIdLastExecution);

		// when
		tasklet.beforeStep(stepExecution);

		int repeatCount = 0;
		RepeatStatus repeatStatus = RepeatStatus.CONTINUABLE;
		while (repeatStatus != RepeatStatus.FINISHED) {
			repeatStatus = tasklet.execute(stepContribution, chunkContext);
			repeatCount++;
		}

		// then
		long countToDelete = lastJobInstanceId - lowJobInstanceIdLastExecution + 1;
		assertThat(stepContribution.getWriteCount()).isEqualTo(countToDelete);
		int expectedChunkCount = Double.valueOf(Math.ceil((double)countToDelete / DELETION_RANGE_LENGTH)).intValue();
		assertThat(repeatCount).isEqualByComparingTo(expectedChunkCount);
	}
}
