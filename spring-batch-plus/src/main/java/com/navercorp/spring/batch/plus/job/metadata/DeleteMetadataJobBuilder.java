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
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
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
	private final JobRepository jobRepository;

	private final DataSource dataSource;

	private String name = "deleteMetadataJob";

	private String tablePrefix = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

	private String baseDateParameterName = "baseDate";

	private DateTimeFormatter baseDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	private String dryRunParameterName = "dryRun";

	/**
	 * @param jobRepository the target job repository to delete old metadata.
	 * @param dataSource    the data source of the job repository
	 */
	public DeleteMetadataJobBuilder(@NonNull JobRepository jobRepository, @NonNull DataSource dataSource) {
		Objects.requireNonNull(jobRepository, "JobRepository must not be null");
		Objects.requireNonNull(dataSource, "DataSource must not be null");

		this.dataSource = dataSource;
		this.jobRepository = jobRepository;
	}

	/**
	 * @param name the name of the job to delete metadata. The default value is 'deleteMetaDataJob'.
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder name(@NonNull String name) {
		this.name = Objects.requireNonNull(name, "Job name must not be null");
		return this;
	}

	/**
	 * @param tablePrefix The prefix of tables for metadata. The default value is 'BATCH_'.
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder tablePrefix(@NonNull String tablePrefix) {
		this.tablePrefix = Objects.requireNonNull(tablePrefix, "Metadata table prefix must not be null");
		return this;
	}

	/**
	 * @param baseDateParameterName the name of the job parameter for base date of not deleting metadata.
	 *                              The default value is "baseDate"
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder baseDateParameterName(@NonNull String baseDateParameterName) {
		this.baseDateParameterName = Objects.requireNonNull(baseDateParameterName,
			"BaseDate parameter name must not be null");
		return this;
	}

	/**
	 * @param baseDateFormatter the pattern of for the parameter of LocalDate type.
	 *                          The default value is DateTimeFormatter.ofPattern("yyyy/MM/dd")
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder baseDateFormatter(@NonNull DateTimeFormatter baseDateFormatter) {
		this.baseDateFormatter = Objects.requireNonNull(baseDateFormatter, "BaseDate formatter must not be null");
		return this;
	}

	/**
	 * @param dryRunParameterName the name of the job parameter to trigger dry-run.
	 *                            The default value is "dryRun"
	 * @return The current instance of the builder for method chaining
	 */
	public DeleteMetadataJobBuilder dryRunParameterName(@NonNull String dryRunParameterName) {
		this.dryRunParameterName = Objects.requireNonNull(dryRunParameterName,
			"DryRun parameter name must not be null");
		return this;
	}

	/**
	 * @return a job to delete old metadata.
	 */
	public Job build() {

		DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
		validator.setRequiredKeys(new String[] {this.baseDateParameterName});

		JobMetadataDao dao = new JobMetadataDao(this.dataSource, this.tablePrefix);
		Step checkStep = this.buildCheckStep(dao);

		return new JobBuilder(this.name, this.jobRepository)
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
		return new StepBuilder("checkMaxJobInstanceId", this.jobRepository)
			.tasklet(tasklet, new ResourcelessTransactionManager())
			.transactionAttribute(noTransaction)
			.listener(tasklet)
			.build();
	}

	private Step buildDeleteStep(JobMetadataDao dao) {
		DeleteMetadataTasklet tasklet = new DeleteMetadataTasklet(
			dao,
			this.dryRunParameterName
		);
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		return new StepBuilder("deleteMetadata", this.jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(tasklet)
			.build();
	}
}
