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

import java.time.LocalDate;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A dao accessing metadata tables of Spring Batch.
 */
class JobMetadataDao {

	private static final String TABLE_PREFIX_PLACEHOLDER = "%PREFIX%";

	protected final JdbcOperations jdbcOperations;

	protected final String selectMaxJobInstanceId;

	protected final String selectMinJobInstanceId;

	protected final String deleteJobInstances;

	protected final String deleteJobExecutions;

	protected final String deleteJobExecutionParams;

	protected final String deleteJobExecutionContexts;

	protected final String deleteStepExecutions;

	protected final String deleteStepExecutionContexts;

	JobMetadataDao(DataSource dataSource, String tablePrefix) {
		this.jdbcOperations = new JdbcTemplate(dataSource);

		this.selectMaxJobInstanceId = getQuery(MetadataSql.SELECT_MAX_JOB_INSTANCE_ID, tablePrefix);
		this.selectMinJobInstanceId = getQuery(MetadataSql.SELECT_MIN_JOB_INSTANCE_ID, tablePrefix);
		this.deleteJobInstances = getQuery(MetadataSql.DELETE_JOB_INSTANCES, tablePrefix);
		this.deleteJobExecutions = getQuery(MetadataSql.DELETE_JOB_EXECUTIONS, tablePrefix);
		this.deleteJobExecutionParams = getQuery(MetadataSql.DELETE_JOB_EXECUTION_PARAMS, tablePrefix);
		this.deleteJobExecutionContexts = getQuery(MetadataSql.DELETE_JOB_EXECUTION_CONTEXTS, tablePrefix);
		this.deleteStepExecutions = getQuery(MetadataSql.DELETE_STEP_EXECUTIONS, tablePrefix);
		this.deleteStepExecutionContexts = getQuery(MetadataSql.DELETE_STEP_EXECUTION_CONTEXTS, tablePrefix);
	}

	Optional<Long> selectMaxJobInstanceIdLessThanCreateTime(LocalDate createTime) {
		return Optional.ofNullable(jdbcOperations.queryForObject(selectMaxJobInstanceId, Long.class, createTime));
	}

	Optional<Long> selectMinJobInstanceId() {
		return Optional.ofNullable(jdbcOperations.queryForObject(selectMinJobInstanceId, Long.class));
	}

	int deleteJobInstancesByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return jdbcOperations.update(deleteJobInstances, lowJobInstanceId, highJobInstanceId);
	}

	int deleteJobExecutionsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return jdbcOperations.update(deleteJobExecutions, lowJobInstanceId, highJobInstanceId);
	}

	int deleteJobExecutionParamsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return jdbcOperations.update(deleteJobExecutionParams, lowJobInstanceId, highJobInstanceId);
	}

	int deleteJobExecutionContextsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return jdbcOperations.update(deleteJobExecutionContexts, lowJobInstanceId, highJobInstanceId);
	}

	int deleteStepExecutionsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return jdbcOperations.update(deleteStepExecutions, lowJobInstanceId, highJobInstanceId);
	}

	int deleteStepExecutionContextsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return jdbcOperations.update(deleteStepExecutionContexts, lowJobInstanceId, highJobInstanceId);
	}

	private String getQuery(String sql, String tablePrefix) {
		return sql.replace(TABLE_PREFIX_PLACEHOLDER, tablePrefix);
	}
}
