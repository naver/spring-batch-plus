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
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.createJobExecution;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.createStepExecution;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateFrom;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateTo;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.randomBetween;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
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

	JobOperator jobOperator;

	@BeforeEach
	void setUp(
		@Autowired DataSource dataSource,
		@Autowired String tablePrefix
	) {
		TaskExecutorJobOperator jobOperator = new TaskExecutorJobOperator();
		jobOperator.setJobRepository(this.jobRepository);
		jobOperator.setTaskExecutor(new SyncTaskExecutor());
		this.jobOperator = jobOperator;

		this.job = new DeleteMetadataJobBuilder(this.jobRepository, dataSource)
			.tablePrefix(tablePrefix)
			.build();

		// JobOperator.start() in Spring Batch 6.0 creates the JobInstance before validating parameters,
		// so failed validations leave orphan instances. Delete by instance to also drop their executions.
		for (String jobName : this.jobRepository.getJobNames()) {
			for (JobInstance instance : this.jobRepository.findJobInstances(jobName)) {
				this.jobRepository.deleteJobInstance(instance);
			}
		}
	}

	@Test
	void testRunFailWithInvalidParameter() {
		// given
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseLine", "2022/03/15") // "baseDate" is correct.
			.toJobParameters();

		// when, then
		assertThatExceptionOfType(InvalidJobParametersException.class)
			.isThrownBy(() ->
				jobOperator.start(job, jobParameters)
			)
			.withMessageContaining("do not contain required keys: [baseDate]");
	}

	@Test
	void testRunWhenNeedNotToDelete() throws Exception {
		// given
		int countToCreate = randomBetween(10, 50);
		for (int i = 0; i < countToCreate; ++i) {
			JobExecution jobExecution = createJobExecution(jobRepository, "testJobToRemove" + i, buildJobParams());
			jobExecution.setCreateTime(dateFrom(2022, 3, 15));
			jobRepository.update(jobExecution);
			createStepExecution(jobRepository, "testStep", jobExecution);
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();

		// when
		JobExecution actualExecution = jobOperator.start(job, jobParameters);

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
			JobExecution jobExecution = createJobExecution(jobRepository, "testJobToRemove" + i, buildJobParams());
			jobExecution.setCreateTime(dateTo(2022, 3, 14));
			jobRepository.update(jobExecution);
			createStepExecution(jobRepository, "testStep", jobExecution);
		}
		int countToCreateAfterBaseDate = randomBetween(10, 50);
		for (int i = 0; i < countToCreateAfterBaseDate; ++i) {
			JobExecution jobExecution = createJobExecution(jobRepository, "testJobToRemains" + i, buildJobParams());
			jobExecution.setCreateTime(dateFrom(2022, 3, 15));
			jobRepository.update(jobExecution);
			createStepExecution(jobRepository, "testStep", jobExecution);
		}

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();
		JobExecution actualExecution = jobOperator.start(job, jobParameters);

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

	@Test
	void testRunShouldNotRemoveWhenDryRun() throws Exception {
		// given
		int countToCreate = randomBetween(10, 50);
		for (int i = 0; i < countToCreate; ++i) {
			JobExecution jobExecution = createJobExecution(jobRepository, "testJobToRemove" + i, buildJobParams());
			jobExecution.setCreateTime(dateTo(2022, 3, 14));
			jobRepository.update(jobExecution);
			createStepExecution(jobRepository, "testStep", jobExecution);
		}

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.addString("dryRun", "true")
			.toJobParameters();
		JobExecution actualExecution = jobOperator.start(job, jobParameters);

		// then
		assertThat(actualExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(actualExecution.getStepExecutions()).hasSize(2);

		int expectedJobMetadataCount = countToCreate + 1; // includes deleteMetadataJob itself
		assertThat(countDao.countJobInstances()).isGreaterThanOrEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutions()).isGreaterThanOrEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutionContexts()).isGreaterThanOrEqualTo(expectedJobMetadataCount);
		assertThat(countDao.countJobExecutionParams()).isGreaterThanOrEqualTo(expectedJobMetadataCount);

		int expectedStepMetadataCount = countToCreate + 2; // deleteMetadataJob has 2 steps
		assertThat(countDao.countStepExecutions()).isGreaterThanOrEqualTo(expectedStepMetadataCount);
		assertThat(countDao.countStepExecutionContext()).isGreaterThanOrEqualTo(expectedStepMetadataCount);
	}
}
