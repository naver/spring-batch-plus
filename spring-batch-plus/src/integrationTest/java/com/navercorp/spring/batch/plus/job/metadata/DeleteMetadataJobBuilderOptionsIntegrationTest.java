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
import static org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Covers each DeleteMetadataJobBuilder option taking effect in a real job run.
 */
class DeleteMetadataJobBuilderOptionsIntegrationTest {

	@Test
	void builtJobShouldUseDefaultNameWhenNotConfigured() throws Exception {
		DataSource dataSource = createDataSource(DEFAULT_TABLE_PREFIX);
		JobRepository jobRepository = createJobRepository(dataSource, DEFAULT_TABLE_PREFIX);

		Job actual = new DeleteMetadataJobBuilder(jobRepository, dataSource).build();

		assertThat(actual.getName()).isEqualTo("deleteMetadataJob");
	}

	@Test
	void builtJobShouldUseConfiguredName() throws Exception {
		String jobName = UUID.randomUUID().toString();
		DataSource dataSource = createDataSource(DEFAULT_TABLE_PREFIX);
		JobRepository jobRepository = createJobRepository(dataSource, DEFAULT_TABLE_PREFIX);

		Job actual = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.name(jobName)
			.build();

		assertThat(actual.getName()).isEqualTo(jobName);
	}

	@Test
	void builtJobShouldDeleteMetadataWithConfiguredTablePrefix() throws Exception {
		String tablePrefix = "BAT_";
		int expiredMetadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.tablePrefix(tablePrefix)
			.build();
		for (int i = 0; i < expiredMetadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 14, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.toJobParameters();
		JobOperator jobOperator = createJobOperator(jobRepository);

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(countRows(dataSource, tablePrefix, "JOB_INSTANCE")).isEqualTo(1);
		assertThat(countRows(dataSource, tablePrefix, "JOB_EXECUTION")).isEqualTo(1);
		assertThat(countRows(dataSource, tablePrefix, "JOB_EXECUTION_CONTEXT")).isEqualTo(1);
		assertThat(countRows(dataSource, tablePrefix, "JOB_EXECUTION_PARAMS")).isEqualTo(1);
		assertThat(countRows(dataSource, tablePrefix, "STEP_EXECUTION")).isEqualTo(2);
		assertThat(countRows(dataSource, tablePrefix, "STEP_EXECUTION_CONTEXT")).isEqualTo(2);
	}

	@Test
	void builtJobShouldDeleteMetadataWithConfiguredBaseDateParameterName() throws Exception {
		String baseDateParameterName = UUID.randomUUID().toString();
		int expiredMetadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		DataSource dataSource = createDataSource(DEFAULT_TABLE_PREFIX);
		JobRepository jobRepository = createJobRepository(dataSource, DEFAULT_TABLE_PREFIX);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.baseDateParameterName(baseDateParameterName)
			.build();
		for (int i = 0; i < expiredMetadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 14, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(baseDateParameterName, "2022/03/15")
			.toJobParameters();
		JobOperator jobOperator = createJobOperator(jobRepository);

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_INSTANCE")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION_CONTEXT")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION_PARAMS")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "STEP_EXECUTION")).isEqualTo(2);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "STEP_EXECUTION_CONTEXT")).isEqualTo(2);
	}

	@Test
	void builtJobShouldDeleteMetadataWithConfiguredBaseDateFormatter() throws Exception {
		DateTimeFormatter baseDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		int expiredMetadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		DataSource dataSource = createDataSource(DEFAULT_TABLE_PREFIX);
		JobRepository jobRepository = createJobRepository(dataSource, DEFAULT_TABLE_PREFIX);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.baseDateFormatter(baseDateFormatter)
			.build();
		for (int i = 0; i < expiredMetadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 14, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022-03-15")
			.toJobParameters();
		JobOperator jobOperator = createJobOperator(jobRepository);

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_INSTANCE")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION_CONTEXT")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION_PARAMS")).isEqualTo(1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "STEP_EXECUTION")).isEqualTo(2);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "STEP_EXECUTION_CONTEXT")).isEqualTo(2);
	}

	@Test
	void builtJobShouldPreserveMetadataWithConfiguredDryRunParameterName() throws Exception {
		String dryRunParameterName = UUID.randomUUID().toString();
		int expiredMetadataCount = ThreadLocalRandom.current().nextInt(10, 51);
		DataSource dataSource = createDataSource(DEFAULT_TABLE_PREFIX);
		JobRepository jobRepository = createJobRepository(dataSource, DEFAULT_TABLE_PREFIX);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.dryRunParameterName(dryRunParameterName)
			.build();
		for (int i = 0; i < expiredMetadataCount; i++) {
			createJobExecution(jobRepository, LocalDateTime.of(2022, 3, 14, 0, 0));
		}
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/15")
			.addString(dryRunParameterName, "true")
			.toJobParameters();
		JobOperator jobOperator = createJobOperator(jobRepository);

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_INSTANCE"))
			.isEqualTo(expiredMetadataCount + 1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION"))
			.isEqualTo(expiredMetadataCount + 1);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION_CONTEXT"))
			.isEqualTo(expiredMetadataCount + 1);
		// One row per job parameter, and this run passes two.
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "JOB_EXECUTION_PARAMS"))
			.isEqualTo(expiredMetadataCount + 2);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "STEP_EXECUTION"))
			.isEqualTo(expiredMetadataCount + 2);
		assertThat(countRows(dataSource, DEFAULT_TABLE_PREFIX, "STEP_EXECUTION_CONTEXT"))
			.isEqualTo(expiredMetadataCount + 2);
	}

	@Test
	void builtJobShouldFailWhenConfiguredTablePrefixDoesNotMatchSchema() throws Exception {
		String tablePrefix = "RIGHT_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.tablePrefix("WRONG_")
			.build();
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", "2022/03/14")
			.toJobParameters();
		JobOperator jobOperator = createJobOperator(jobRepository);

		JobExecution actual = jobOperator.start(job, jobParameters);

		assertThat(actual.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(actual.getAllFailureExceptions())
			.first()
			.isInstanceOf(BadSqlGrammarException.class);
	}

	private DataSource createDataSource(String tablePrefix) throws IOException {
		String originalDdl;
		ClassPathResource schema = new ClassPathResource("org/springframework/batch/core/schema-h2.sql");
		try (InputStream inputStream = schema.getInputStream()) {
			originalDdl = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
		}
		String customDdl = StringUtils.replace(originalDdl, DEFAULT_TABLE_PREFIX, tablePrefix);
		DataSource dataSource = new EmbeddedDatabaseBuilder()
			.generateUniqueName(true)
			.setType(EmbeddedDatabaseType.H2)
			.build();
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
			new ByteArrayResource(customDdl.getBytes(StandardCharsets.UTF_8))
		);
		DatabasePopulatorUtils.execute(populator, dataSource);

		return dataSource;
	}

	private JobRepository createJobRepository(DataSource dataSource, String tablePrefix) throws Exception {
		JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(new DataSourceTransactionManager(dataSource));
		factory.setTablePrefix(tablePrefix);
		factory.afterPropertiesSet();

		return factory.getObject();
	}

	private JobOperator createJobOperator(JobRepository jobRepository) throws Exception {
		JobOperatorFactoryBean factory = new JobOperatorFactoryBean();
		factory.setJobRepository(jobRepository);
		factory.setJobRegistry(new MapJobRegistry());
		factory.afterPropertiesSet();

		return factory.getObject();
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

	private int countRows(DataSource dataSource, String tablePrefix, String tableName) {
		return JdbcTestUtils.countRowsInTable(new JdbcTemplate(dataSource), tablePrefix + tableName);
	}
}
