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

import java.time.format.DateTimeFormatter;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * A factory to create the job to delete old job metadata.
 *
 * @since 0.2.0
 */
public class DeleteMetadataJobBuilder {
	private final StepBuilderFactory stepBuilderFactory;

	private final JobBuilderFactory jobBuilderFactory;

	private final DataSource dataSource;

	private String name = "deleteMetadataJob";

	private String tablePrefix = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

	private String baseDateParameterName = "baseDate";

	private DateTimeFormatter baseDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	/**
	 *
	 * @param jobRepository the target job repository to delete old metadata.
	 * @param dataSource the data source of the job repository
	 */
	public DeleteMetadataJobBuilder(JobRepository jobRepository, DataSource dataSource) {
		this.dataSource = dataSource;
		this.jobBuilderFactory = new JobBuilderFactory(jobRepository);
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		this.stepBuilderFactory = new StepBuilderFactory(jobRepository, transactionManager);
	}

	/**
	 *
	 * @param name the name of the job to delete metadata. The default value is 'deleteMetaDataJob'.
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 *
	 * @param tablePrefix The prefix of tables for metadata. The default value is 'BATCH_'.
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder tablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
		return this;
	}

	/**
	 *
	 * @param baseDateParameterName the name of the job parameter for base date of not deleting metadata.
	 *                              The default value is "baseDate"
	 *
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder baseDateParameterName(String baseDateParameterName) {
		this.baseDateParameterName = baseDateParameterName;
		return this;
	}

	/**
	 *
	 * @param baseDateFormatter the pattern of for the parameter of LocalDate type.
	 *                          The default value is DateTimeFormatter.ofPattern("yyyy/MM/dd")
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder baseDateFormatter(DateTimeFormatter baseDateFormatter) {
		this.baseDateFormatter = baseDateFormatter;
		return this;
	}

	/**
	 *
	 * @return a job to delete old metadata.
	 */
	public Job build() {

		DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
		validator.setRequiredKeys(new String[] {baseDateParameterName});

		JobMetadataDao dao = new JobMetadataDao(this.dataSource, tablePrefix);
		Step checkStep = this.buildCheckStep(dao);

		return jobBuilderFactory.get(name)
			.validator(validator)
			.start(checkStep)

			.on(CheckMaxJobInstanceIdToDeleteTasklet.EMPTY.getExitCode())
			.end()

			.from(checkStep)
			.next(buildDeleteStep(dao))
			.end()
			.build();
	}

	private Step buildCheckStep(JobMetadataDao dao) {
		CheckMaxJobInstanceIdToDeleteTasklet tasklet = new CheckMaxJobInstanceIdToDeleteTasklet(
			dao,
			this.baseDateParameterName,
			this.baseDateFormatter
		);

		TransactionAttribute noTransaction = new DefaultTransactionAttribute(Propagation.NOT_SUPPORTED.value());
		return stepBuilderFactory.get("checkMaxJobInstanceId")
			.tasklet(tasklet)
			.transactionAttribute(noTransaction)
			.listener(tasklet)
			.build();
	}

	private Step buildDeleteStep(JobMetadataDao dao) {
		DeleteMetadataTasklet tasklet = new DeleteMetadataTasklet(dao);
		return stepBuilderFactory.get("deleteMetadata")
			.tasklet(tasklet)
			.listener(tasklet)
			.build();
	}
}
