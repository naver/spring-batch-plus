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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * Covers metadata range queries and deletions against the Spring Batch JDBC schema.
 *
 * <p>Runs on an embedded H2 database, since the contract under test is the SQL itself.
 * Each test builds its own database, so no test observes another's rows.
 */
class JobMetadataDaoTest {

	@Test
	void selectMaxJobInstanceIdLessThanCreateTimeShouldReturnMaximumEligibleIdWhenExecutionExists() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		JobExecution execution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		execution1.setCreateTime(dateTo(2022, 3, 14));
		jobRepository.update(execution1);
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		execution2.setCreateTime(dateTo(2022, 3, 14));
		jobRepository.update(execution2);
		JobExecution execution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		execution3.setCreateTime(dateFrom(2022, 3, 15));
		jobRepository.update(execution3);

		Optional<Long> actual = dao.selectMaxJobInstanceIdLessThanCreateTime(LocalDate.of(2022, 3, 15));

		assertThat(actual).hasValue(execution2.getJobInstanceId());
	}

	@Test
	void selectMaxJobInstanceIdLessThanCreateTimeShouldReturnEmptyWhenNoExecutionPredatesCreateTime() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		JobExecution execution = createJobExecution(jobRepository, "testJob1", buildJobParams());
		execution.setCreateTime(dateFrom(2022, 3, 14));
		jobRepository.update(execution);

		Optional<Long> actual = dao.selectMaxJobInstanceIdLessThanCreateTime(LocalDate.of(2022, 3, 14));

		assertThat(actual).isNotPresent();
	}

	@Test
	void selectMinJobInstanceIdShouldReturnMinimumIdWhenJobInstanceExists() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		JobInstance jobInstance1 = jobRepository.createJobInstance("testJob1", buildJobParams());
		jobRepository.createJobInstance("testJob2", buildJobParams());
		jobRepository.createJobInstance("testJob3", buildJobParams());

		Optional<Long> actual = dao.selectMinJobInstanceId();

		assertThat(actual).hasValue(jobInstance1.getId());
	}

	@Test
	void selectMinJobInstanceIdShouldReturnEmptyWhenJobInstanceDoesNotExist() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);

		Optional<Long> actual = dao.selectMinJobInstanceId();

		assertThat(actual).isEmpty();
	}

	@Test
	void deleteJobInstancesByJobInstanceIdRangeShouldReturnDeletedRowCount() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		jobRepository.createJobInstance("testJob1", buildJobParams());
		JobInstance instance2 = jobRepository.createJobInstance("testJob2", buildJobParams());
		JobInstance instance3 = jobRepository.createJobInstance("testJob3", buildJobParams());
		jobRepository.createJobInstance("testJob4", buildJobParams());

		int actual = dao.deleteJobInstancesByJobInstanceIdRange(instance2.getId(), instance3.getId());

		assertThat(actual).isEqualTo(2);
	}

	@Test
	void deleteJobExecutionParamsByJobInstanceIdRangeShouldReturnDeletedRowCount() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		createJobExecution(jobRepository, "testJob1", buildJobParams());
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		JobExecution execution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		createJobExecution(jobRepository, "testJob4", buildJobParams());
		long lowJobInstanceId = execution2.getJobInstanceId();
		long highJobInstanceId = execution3.getJobInstanceId();

		int actual = dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(actual).isEqualTo(2);
	}

	@Test
	void deleteJobExecutionContextsByJobInstanceIdRangeShouldReturnDeletedRowCount() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		createJobExecution(jobRepository, "testJob1", buildJobParams());
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		JobExecution execution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		createJobExecution(jobRepository, "testJob4", buildJobParams());
		long lowJobInstanceId = execution2.getJobInstanceId();
		long highJobInstanceId = execution3.getJobInstanceId();

		int actual = dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(actual).isEqualTo(2);
	}

	@Test
	void deleteJobExecutionsByJobInstanceIdRangeShouldReturnDeletedRowCount() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		createJobExecution(jobRepository, "testJob1", buildJobParams());
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		JobExecution execution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		createJobExecution(jobRepository, "testJob4", buildJobParams());
		long lowJobInstanceId = execution2.getJobInstanceId();
		long highJobInstanceId = execution3.getJobInstanceId();
		dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		int actual = dao.deleteJobExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(actual).isEqualTo(2);
	}

	@Test
	void deleteStepExecutionContextsByJobInstanceIdRangeShouldReturnDeletedRowCount() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		JobExecution jobExecution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		jobRepository.createStepExecution("testStep1", jobExecution1);
		JobExecution jobExecution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		jobRepository.createStepExecution("testStep2", jobExecution2);
		jobRepository.createStepExecution("testStep3", jobExecution2);
		JobExecution jobExecution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		jobRepository.createStepExecution("testStep4", jobExecution3);
		JobExecution jobExecution4 = createJobExecution(jobRepository, "testJob4", buildJobParams());
		jobRepository.createStepExecution("testStep5", jobExecution4);
		long lowJobInstanceId = jobExecution2.getJobInstanceId();
		long highJobInstanceId = jobExecution3.getJobInstanceId();

		int actual = dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(actual).isEqualTo(3);
	}

	@Test
	void deleteStepExecutionsByJobInstanceIdRangeShouldReturnDeletedRowCount() throws Exception {
		String tablePrefix = "BATCH" + ThreadLocalRandom.current().nextInt(1000) + "_";
		DataSource dataSource = createDataSource(tablePrefix);
		JobRepository jobRepository = createJobRepository(dataSource, tablePrefix);
		JobMetadataDao dao = new JobMetadataDao(dataSource, tablePrefix);
		JobExecution jobExecution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		jobRepository.createStepExecution("testStep1", jobExecution1);
		JobExecution jobExecution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		jobRepository.createStepExecution("testStep2", jobExecution2);
		jobRepository.createStepExecution("testStep3", jobExecution2);
		JobExecution jobExecution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		jobRepository.createStepExecution("testStep4", jobExecution3);
		JobExecution jobExecution4 = createJobExecution(jobRepository, "testJob4", buildJobParams());
		jobRepository.createStepExecution("testStep5", jobExecution4);
		long lowJobInstanceId = jobExecution2.getJobInstanceId();
		long highJobInstanceId = jobExecution3.getJobInstanceId();
		dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		int actual = dao.deleteStepExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(actual).isEqualTo(3);
	}

	private DataSource createDataSource(String tablePrefix) throws IOException {
		String originalDdl = readFromClassPath("org/springframework/batch/core/schema-h2.sql");
		String customDdl = StringUtils.replace(originalDdl, DEFAULT_TABLE_PREFIX, tablePrefix);
		File customDdlFile = File.createTempFile("schema-", ".sql");
		FileCopyUtils.copy(customDdl.getBytes(), customDdlFile);

		return new EmbeddedDatabaseBuilder()
			.generateUniqueName(true)
			.setType(EmbeddedDatabaseType.H2)
			.addScript("file:" + customDdlFile.getAbsolutePath())
			.build();
	}

	private String readFromClassPath(String path) throws IOException {
		Resource originalScript = new ClassPathResource(path);
		try (InputStream scriptStream = originalScript.getInputStream()) {
			return StreamUtils.copyToString(scriptStream, StandardCharsets.UTF_8);
		}
	}

	private JobRepository createJobRepository(DataSource dataSource, String tablePrefix) throws Exception {
		JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(new DataSourceTransactionManager(dataSource));
		factory.setTablePrefix(tablePrefix);
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	private JobExecution createJobExecution(JobRepository jobRepository, String jobName, JobParameters parameters) {
		JobInstance jobInstance = jobRepository.createJobInstance(jobName, parameters);
		return jobRepository.createJobExecution(jobInstance, parameters, new ExecutionContext());
	}

	private JobParameters buildJobParams() {
		return new JobParametersBuilder()
			.addLong("timestamp", Instant.now().toEpochMilli())
			.toJobParameters();
	}

	private LocalDateTime dateTo(int year, int month, int day) {
		LocalDate to = LocalDate.of(year, month, day);
		return dateBetween(to.minusDays(10), to);
	}

	private LocalDateTime dateFrom(int year, int month, int day) {
		LocalDate from = LocalDate.of(year, month, day);
		return dateBetween(from, from.plusDays(10));
	}

	private LocalDateTime dateBetween(LocalDate from, LocalDate to) {
		long gap = to.toEpochDay() - from.toEpochDay();
		return from.plusDays(ThreadLocalRandom.current().nextLong(0L, gap + 1L)).atStartOfDay();
	}
}
