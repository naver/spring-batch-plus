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

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * The DAO counting rows in metadata tables of Spring Batch.
 */
class JobMetadataCountDao {
	private final JdbcTemplate jdbcTemplate;
	private final String tablePrefix;

	JobMetadataCountDao(DataSource dataSource, String tablePrefix) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.tablePrefix = tablePrefix;
	}

	int countJobInstances() {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tablePrefix + "JOB_INSTANCE");
	}

	int countJobExecutions() {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tablePrefix + "JOB_EXECUTION");
	}

	int countJobExecutionParams() {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tablePrefix + "JOB_EXECUTION_PARAMS");
	}

	int countJobExecutionContexts() {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tablePrefix + "JOB_EXECUTION_CONTEXT");
	}

	int countStepExecutions() {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tablePrefix + "STEP_EXECUTION");
	}

	int countStepExecutionContext() {
		return JdbcTestUtils.countRowsInTable(jdbcTemplate, tablePrefix + "STEP_EXECUTION_CONTEXT");
	}
}
