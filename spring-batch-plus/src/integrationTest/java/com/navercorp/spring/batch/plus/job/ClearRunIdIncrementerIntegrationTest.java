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

package com.navercorp.spring.batch.plus.job;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.TransactionManager;

/**
 * Covers the next-instance parameter propagation difference between the standard and clearing incrementers.
 */
class ClearRunIdIncrementerIntegrationTest {

	@Test
	void startNextInstanceShouldCarryPreviousNonRunIdParametersForwardWhenRunIdIncrementerIsUsed() {
		long previousRunId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		String previousParameterName = UUID.randomUUID().toString();
		String previousParameterValue = UUID.randomUUID().toString();
		JobParametersIncrementer incrementer = new RunIdIncrementer();

		JobExecution jobExecution = startNextInstanceAfterCompletedExecution(incrementer, previousRunId,
			previousParameterName, previousParameterValue);

		JobParameters actual = jobExecution.getJobParameters();

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(actual.getLong("run.id")).isEqualTo(previousRunId + 1);
		assertThat(actual.getString(previousParameterName)).isEqualTo(previousParameterValue);
		assertThat(actual.parameters()).hasSize(2);
	}

	@Test
	void startNextInstanceShouldDiscardPreviousNonRunIdParametersWhenClearRunIdIncrementerIsUsed() {
		long previousRunId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		String previousParameterName = UUID.randomUUID().toString();
		String previousParameterValue = UUID.randomUUID().toString();
		JobParametersIncrementer incrementer = ClearRunIdIncrementer.create();

		JobExecution jobExecution = startNextInstanceAfterCompletedExecution(incrementer, previousRunId,
			previousParameterName, previousParameterValue);

		JobParameters actual = jobExecution.getJobParameters();

		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(actual.getLong("run.id")).isEqualTo(previousRunId + 1);
		assertThat(actual.parameters()).hasSize(1);
	}

	private JobExecution startNextInstanceAfterCompletedExecution(
		JobParametersIncrementer incrementer,
		long previousRunId,
		String previousParameterName,
		String previousParameterValue
	) {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.register(BatchConfiguration.class);
			context.refresh();
			JobRepository jobRepository = context.getBean(JobRepository.class);
			Step step = new StepBuilder("testStep", jobRepository)
				.tasklet(
					(contribution, chunkContext) -> RepeatStatus.FINISHED,
					new ResourcelessTransactionManager()
				)
				.build();
			Job job = new JobBuilder("testJob", jobRepository)
				.incrementer(incrementer)
				.start(step)
				.build();
			JobOperator jobOperator = context.getBean(JobOperator.class);
			JobParameters previousParameters = new JobParametersBuilder()
				.addLong("run.id", previousRunId)
				.addString(previousParameterName, previousParameterValue)
				.toJobParameters();
			seedCompletedJobExecution(jobRepository, job, previousParameters);

			return jobOperator.startNextInstance(job);
		}
	}

	private void seedCompletedJobExecution(
		JobRepository jobRepository,
		Job job,
		JobParameters jobParameters
	) {
		JobInstance jobInstance = jobRepository.createJobInstance(job.getName(), jobParameters);
		JobExecution jobExecution = jobRepository.createJobExecution(
			jobInstance,
			jobParameters,
			new ExecutionContext()
		);
		jobExecution.setStatus(BatchStatus.COMPLETED);
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobRepository.update(jobExecution);
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
