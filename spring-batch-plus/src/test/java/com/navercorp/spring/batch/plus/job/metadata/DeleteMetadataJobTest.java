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

import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.buildJobParams;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateFrom;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateTo;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.randomBetween;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestJobRepositoryConfig.class)
class DeleteMetadataJobTest {

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobMetadataCountDao countDao;

	Job job;

	JobLauncher jobLauncher;

	@BeforeEach
	void setUp(
		@Autowired DataSource dataSource,
		@Autowired String tablePrefix,
		@Autowired JobRepositoryTestUtils testUtils
	) {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(this.jobRepository);
		jobLauncher.setTaskExecutor(new SyncTaskExecutor());
		this.jobLauncher = jobLauncher;

		this.job = new DeleteMetadataJobBuilder(this.jobRepository, dataSource)
			.tablePrefix(tablePrefix)
			.build();

		testUtils.removeJobExecutions();
	}

	@Test
	void testRunFailWithInvalidParameter() {
		// given
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseLine", "2022/03/15") // "baseDate" is correct.
			.toJobParameters();

		// when, then
		assertThatExceptionOfType(JobParametersInvalidException.class)
			.isThrownBy(() ->
				jobLauncher.run(job, jobParameters)
			)
			.withMessageContaining("do not contain required keys: [baseDate]");
	}

	@Test
	void testRunWhenNeedNotToDelete() throws Exception {
		// given
		int countToCreate = randomBetween(10, 50);
		for (int i = 0; i < countToCreate; ++i) {
			JobExecution jobExecution = jobRepository.createJobExecution("testJobToRemove" + i, buildJobParams());
			jobExecution.setCreateTime(dateFrom(2022, 3, 15));
			jobRepository.update(jobExecution);
			jobRepository.add(new StepExecution("testStep", jobExecution));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();

		// when
		JobExecution actualExecution = jobLauncher.run(job, jobParameters);

		// then
		assertThat(actualExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(actualExecution.getStepExecutions()).hasSize(1);

		int expectedJobMetadataCount = countToCreate + 1; // includes deleteMetadataJob itself
		assertThat(countDao.countJobInstances()).isEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutions()).isEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutionContexts()).isEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutionParams()).isEqualTo(expectedJobMetadataCount);

		int expectedStepMetadataCount = countToCreate + 1; // deleteMetadataJob executes one step
		assertThat(countDao.countStepExecutions()).isEqualTo(expectedStepMetadataCount);
		assertThat(countDao.countStepExecutionContext()).isEqualTo(expectedStepMetadataCount);
	}

	@Test
	void testRunWhenNeedToDelete() throws Exception {
		// given
		int countToCreateBeforeBaseDate = randomBetween(10, 50);
		for (int i = 0; i < countToCreateBeforeBaseDate; ++i) {
			JobExecution jobExecution = jobRepository.createJobExecution("testJobToRemove" + i, buildJobParams());
			jobExecution.setCreateTime(dateTo(2022, 3, 14));
			jobRepository.update(jobExecution);
			jobRepository.add(new StepExecution("testStep", jobExecution));
		}
		int countToCreateAfterBaseDate = randomBetween(10, 50);
		for (int i = 0; i < countToCreateAfterBaseDate; ++i) {
			JobExecution jobExecution = jobRepository.createJobExecution("testJobToRemains" + i, buildJobParams());
			jobExecution.setCreateTime(dateFrom(2022, 3, 15));
			jobRepository.update(jobExecution);
			jobRepository.add(new StepExecution("testStep", jobExecution));
		}

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();
		JobExecution actualExecution = jobLauncher.run(job, jobParameters);

		// then
		assertThat(actualExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(actualExecution.getStepExecutions()).hasSize(2);

		int expectedJobMetadataCount = countToCreateAfterBaseDate + 1; // includes deleteMetadataJob itself
		assertThat(countDao.countJobInstances()).isEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutions()).isEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutionContexts()).isEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutionParams()).isEqualTo(expectedJobMetadataCount);

		int expectedStepMetadataCount = countToCreateAfterBaseDate + 2; // deleteMetadataJob has 2 steps
		assertThat(countDao.countStepExecutions()).isEqualTo(expectedStepMetadataCount);
		assertThat(countDao.countStepExecutionContext()).isEqualTo(expectedStepMetadataCount);
	}
}
