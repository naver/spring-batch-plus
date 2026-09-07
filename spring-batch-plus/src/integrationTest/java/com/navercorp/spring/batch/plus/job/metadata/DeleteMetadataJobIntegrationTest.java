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

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionManager;

/**
 * Covers metadata cleanup through a complete job execution.
 */
class DeleteMetadataJobIntegrationTest {

	@Test
	void startShouldDeleteMetadataPredatingBaseDate() throws Exception {
		int expiredMetadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		int retainedMetadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class);
		context.refresh();
		DataSource dataSource = context.getBean(DataSource.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource).build();
		JobOperator jobOperator = context.getBean(JobOperator.class);
		for (int i = 0; i < expiredMetadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 14, 0, 0));
		}
		for (int i = 0; i < retainedMetadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 16, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(actual.getStepExecutions()).hasSize(2);
		assertThat(countRows(dataSource, "JOB_INSTANCE")).isEqualTo(retainedMetadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION")).isEqualTo(retainedMetadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION_CONTEXT")).isEqualTo(retainedMetadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION_PARAMS")).isEqualTo(retainedMetadataCount + 1);
		assertThat(countRows(dataSource, "STEP_EXECUTION")).isEqualTo(retainedMetadataCount + 2);
		assertThat(countRows(dataSource, "STEP_EXECUTION_CONTEXT")).isEqualTo(retainedMetadataCount + 2);
	}

	@Test
	void startShouldPreserveMetadataWhenDryRunIsEnabled() throws Exception {
		int metadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class);
		context.refresh();
		DataSource dataSource = context.getBean(DataSource.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource).build();
		JobOperator jobOperator = context.getBean(JobOperator.class);
		for (int i = 0; i < metadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 14, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.addString("dryRun", "true")
			.toJobParameters();

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(actual.getStepExecutions()).hasSize(2);
		assertThat(countRows(dataSource, "JOB_INSTANCE")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION_CONTEXT")).isEqualTo(metadataCount + 1);
		// One row per job parameter, and this run passes two.
		assertThat(countRows(dataSource, "JOB_EXECUTION_PARAMS")).isEqualTo(metadataCount + 2);
		assertThat(countRows(dataSource, "STEP_EXECUTION")).isEqualTo(metadataCount + 2);
		assertThat(countRows(dataSource, "STEP_EXECUTION_CONTEXT")).isEqualTo(metadataCount + 2);
	}

	@Test
	void startShouldRejectParametersWithoutBaseDate() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class);
		context.refresh();
		DataSource dataSource = context.getBean(DataSource.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource).build();
		JobOperator jobOperator = context.getBean(JobOperator.class);
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseLine", "2022/03/15")
			.toJobParameters();

		assertThatExceptionOfType(InvalidJobParametersException.class)
			.isThrownBy(() -> jobOperator.start(job, jobParameters));
	}

	@Test
	void startShouldCompleteAfterCheckStepWhenNoMetadataPredatesBaseDate() throws Exception {
		int metadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class);
		context.refresh();
		DataSource dataSource = context.getBean(DataSource.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource).build();
		JobOperator jobOperator = context.getBean(JobOperator.class);
		for (int i = 0; i < metadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 16, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
		assertThat(actual.getStepExecutions()).hasSize(1);
		assertThat(countRows(dataSource, "JOB_INSTANCE")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION_CONTEXT")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "JOB_EXECUTION_PARAMS")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "STEP_EXECUTION")).isEqualTo(metadataCount + 1);
		assertThat(countRows(dataSource, "STEP_EXECUTION_CONTEXT")).isEqualTo(metadataCount + 1);
	}

	private void createJobExecution(JobRepository jobRepository, LocalDateTime createTime) {
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("id", UUID.randomUUID().toString())
			.toJobParameters();
		JobInstance jobInstance = jobRepository.createJobInstance(UUID.randomUUID().toString(), jobParameters);
		JobExecution jobExecution = jobRepository.createJobExecution(
			jobInstance,
			jobParameters,
			new ExecutionContext()
		);
		jobExecution.setCreateTime(createTime);
		jobRepository.update(jobExecution);
		jobRepository.createStepExecution("testStep", jobExecution);
	}

	private int countRows(DataSource dataSource, String tableName) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_" + tableName);
	}

	@EnableBatchProcessing
	@EnableJdbcJobRepository(
		dataSourceRef = "metadataDataSource",
		transactionManagerRef = "metadataTransactionManager"
	)
	private static class BatchConfiguration {

		@Bean
		TransactionManager metadataTransactionManager() {
			return new DataSourceTransactionManager(metadataDataSource());
		}

		@Bean
		DataSource metadataDataSource() {
			return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.addScript("/org/springframework/batch/core/schema-h2.sql")
				.generateUniqueName(true)
				.build();
		}
	}
}
