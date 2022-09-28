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

import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A dao accessing metadata tables of Spring Batch.
 */
class JobMetadataDao extends AbstractJdbcBatchMetadataDao {

	private final String selectMaxJobInstanceId;

	private final String selectMinJobInstanceId;

	private final String deleteJobInstances;

	private final String deleteJobExecutions;

	private final String deleteJobExecutionParams;

	private final String deleteJobExecutionContexts;

	private final String deleteStepExecutions;

	private final String deleteStepExecutionContexts;

	JobMetadataDao(DataSource dataSource, String tablePrefix) {
		setJdbcTemplate(new JdbcTemplate(dataSource));
		setTablePrefix(tablePrefix);

		this.selectMaxJobInstanceId = getQuery(MetadataSql.SELECT_MAX_JOB_INSTANCE_ID);
		this.selectMinJobInstanceId = getQuery(MetadataSql.SELECT_MIN_JOB_INSTANCE_ID);
		this.deleteJobInstances = getQuery(MetadataSql.DELETE_JOB_INSTANCES);
		this.deleteJobExecutions = getQuery(MetadataSql.DELETE_JOB_EXECUTIONS);
		this.deleteJobExecutionParams = getQuery(MetadataSql.DELETE_JOB_EXECUTION_PARAMS);
		this.deleteJobExecutionContexts = getQuery(MetadataSql.DELETE_JOB_EXECUTION_CONTEXTS);
		this.deleteStepExecutions = getQuery(MetadataSql.DELETE_STEP_EXECUTIONS);
		this.deleteStepExecutionContexts = getQuery(MetadataSql.DELETE_STEP_EXECUTION_CONTEXTS);
	}

	Optional<Long> selectMaxJobInstanceIdLessThanCreateTime(LocalDate createTime) {
		return Optional.ofNullable(
			getJdbcTemplate().queryForObject(selectMaxJobInstanceId, Long.class, createTime)
		);
	}

	Long selectMinJobInstanceId() {
		return getJdbcTemplate().queryForObject(selectMinJobInstanceId, Long.class);
	}

	int deleteJobInstancesByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return getJdbcTemplate().update(deleteJobInstances, lowJobInstanceId, highJobInstanceId);
	}

	int deleteJobExecutionsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return getJdbcTemplate().update(deleteJobExecutions, lowJobInstanceId, highJobInstanceId);
	}

	int deleteJobExecutionParamsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return getJdbcTemplate().update(deleteJobExecutionParams, lowJobInstanceId, highJobInstanceId);
	}

	int deleteJobExecutionContextsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return getJdbcTemplate().update(deleteJobExecutionContexts, lowJobInstanceId, highJobInstanceId);
	}

	int deleteStepExecutionsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return getJdbcTemplate().update(deleteStepExecutions, lowJobInstanceId, highJobInstanceId);
	}

	int deleteStepExecutionContextsByJobInstanceIdRange(long lowJobInstanceId, long highJobInstanceId) {
		return getJdbcTemplate().update(deleteStepExecutionContexts, lowJobInstanceId, highJobInstanceId);
	}
}
