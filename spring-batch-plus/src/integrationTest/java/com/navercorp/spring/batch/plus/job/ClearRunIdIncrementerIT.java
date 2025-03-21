package com.navercorp.spring.batch.plus.job;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.TransactionManager;

public class ClearRunIdIncrementerIT {

	@Test
	void runTwiceShouldBeExecutedWhenUsingClearRunIdIncrementer() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class);
		context.refresh();
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.incrementer(ClearRunIdIncrementer.create())
			.start(
				new StepBuilder("testStep", jobRepository)
					.tasklet(
						(contribution, chunkContext) -> RepeatStatus.FINISHED,
						new ResourcelessTransactionManager()
					)
					.build()
			)
			.build();
		JobExplorer jobExplorer = context.getBean(JobExplorer.class);
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		JobParameters firstJobParameters = new JobParametersBuilder(jobExplorer)
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution firstJobExecution = jobLauncher.run(job, firstJobParameters);
		JobParameters secondJobParameters = new JobParametersBuilder(jobExplorer)
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution secondJobExecution = jobLauncher.run(job, secondJobParameters);

		assertThat(firstJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(firstJobExecution.getJobParameters().getLong("run.id")).isEqualTo(1L);
		assertThat(secondJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(secondJobExecution.getJobParameters().getLong("run.id")).isEqualTo(2L);
	}

	@Test
	void runTwiceShouldBeExecutedWhenUsingClearRunIdIncrementerWithCustom() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(BatchConfiguration.class);
		context.refresh();
		String customRunId = UUID.randomUUID().toString();
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.incrementer(ClearRunIdIncrementer.create(customRunId))
			.start(
				new StepBuilder("testStep", jobRepository)
					.tasklet(
						(contribution, chunkContext) -> RepeatStatus.FINISHED,
						new ResourcelessTransactionManager()
					)
					.build()
			)
			.build();
		JobExplorer jobExplorer = context.getBean(JobExplorer.class);
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		JobParameters firstJobParameters = new JobParametersBuilder(jobExplorer)
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution firstJobExecution = jobLauncher.run(job, firstJobParameters);
		JobParameters secondJobParameters = new JobParametersBuilder(jobExplorer)
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution secondJobExecution = jobLauncher.run(job, secondJobParameters);

		assertThat(firstJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(firstJobExecution.getJobParameters().getLong(customRunId)).isEqualTo(1L);
		assertThat(secondJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(secondJobExecution.getJobParameters().getLong(customRunId)).isEqualTo(2L);
	}

	@EnableBatchProcessing(
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
