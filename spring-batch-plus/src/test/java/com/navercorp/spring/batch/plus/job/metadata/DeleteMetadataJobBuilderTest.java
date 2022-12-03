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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.format.DateTimeFormatter;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.jdbc.BadSqlGrammarException;

class DeleteMetadataJobBuilderTest {
	TestJobRepositoryConfig config = new TestJobRepositoryConfig();

	@Test
	void testBuildAndExecuteJobWithDefaults() throws Exception {
		// given
		String tablePrefix = "BATCH_";
		DataSource dataSource = config.dataSource(tablePrefix);
		JobRepository jobRepository = config.jobRepository(dataSource, tablePrefix);

		JobParameters jobParams = new JobParametersBuilder()
			.addString("baseDate", "2022/03/14")
			.toJobParameters();

		// when
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource).build();
		JobExecution jobExecution = jobRepository.createJobExecution("deleteMetadataJob", jobParams);
		job.execute(jobExecution);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(job.getName()).isEqualTo("deleteMetadataJob");
	}

	@Test
	void testBuildAndExecuteJobWithCustomConfigs() throws Exception {
		// given
		String tablePrefix = "BAT_";
		DataSource dataSource = config.dataSource(tablePrefix);
		JobRepository jobRepository = config.jobRepository(dataSource, tablePrefix);

		JobParameters jobParams = new JobParametersBuilder()
			.addString("keepingBaseDate", "2022-03-14")
			.toJobParameters();

		// when
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.name("testJob")
			.tablePrefix(tablePrefix)
			.baseDateParameterName("keepingBaseDate")
			.baseDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
			.build();

		JobExecution jobExecution = jobRepository.createJobExecution("testJob", jobParams);
		job.execute(jobExecution);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(job.getName()).isEqualTo("testJob");
	}

	@Test
	void testBuildAndExecuteJobWithWrongTablePrefix() throws Exception {
		// given
		String tablePrefix = "RIGHT_";
		DataSource dataSource = config.dataSource(tablePrefix);
		JobRepository jobRepository = config.jobRepository(dataSource, tablePrefix);

		JobParameters jobParams = new JobParametersBuilder()
			.addString("baseDate", "2022/03/14")
			.toJobParameters();

		// when
		Job job = new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.tablePrefix("WRONG_")
			.build();

		JobExecution jobExecution = jobRepository.createJobExecution("testJob", jobParams);
		job.execute(jobExecution);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
		Throwable exception = jobExecution.getAllFailureExceptions().get(0);
		assertThat(exception).isInstanceOf(BadSqlGrammarException.class);
		assertThat(exception.getMessage()).contains(
			"bad SQL grammar [SELECT MAX(JOB_INSTANCE_ID) FROM WRONG_JOB_EXECUTION"
		);
	}

	@Test
	void testNullCheck() throws Exception {
		// given
		String tablePrefix = "RIGHT_";
		DataSource dataSource = config.dataSource(tablePrefix);
		JobRepository jobRepository = config.jobRepository(dataSource, tablePrefix);

		// when, then
		assertThatThrownBy(() -> new DeleteMetadataJobBuilder(null, dataSource));
		assertThatThrownBy(() -> new DeleteMetadataJobBuilder(jobRepository, null));
		assertThatThrownBy(() ->
			new DeleteMetadataJobBuilder(jobRepository, dataSource)
				.name(null)
				.build()
		);
		assertThatThrownBy(() ->
			new DeleteMetadataJobBuilder(jobRepository, dataSource)
				.name(null)
				.build()
		);
		assertThatThrownBy(() ->
			new DeleteMetadataJobBuilder(jobRepository, dataSource)
				.tablePrefix(null)
				.build()
		);
		assertThatThrownBy(() ->
			new DeleteMetadataJobBuilder(jobRepository, dataSource)
				.baseDateParameterName(null)
				.build()
		);
		assertThatThrownBy(() ->
			new DeleteMetadataJobBuilder(jobRepository, dataSource)
				.baseDateFormatter(null)
				.build()
		);
	}
}
