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

/**
 * The class to provide SQL statements for {@link JobMetadataDao}
 */
final class MetadataSql {

	static final String SELECT_MAX_JOB_INSTANCE_ID =
		"SELECT MAX(JOB_INSTANCE_ID) FROM %PREFIX%JOB_EXECUTION\n"
			+ "WHERE CREATE_TIME < ?";

	static final String SELECT_MIN_JOB_INSTANCE_ID = "SELECT MIN(JOB_INSTANCE_ID) FROM %PREFIX%JOB_INSTANCE";

	static final String DELETE_JOB_INSTANCES = "DELETE FROM %PREFIX%JOB_INSTANCE\n"
		+ "WHERE JOB_INSTANCE_ID BETWEEN ? AND ?";

	static final String DELETE_JOB_EXECUTIONS = "DELETE FROM %PREFIX%JOB_EXECUTION\n"
		+ "WHERE JOB_INSTANCE_ID BETWEEN ? AND ?";

	static final String DELETE_JOB_EXECUTION_PARAMS = "DELETE FROM %PREFIX%JOB_EXECUTION_PARAMS\n"
		+ "WHERE JOB_EXECUTION_ID IN (\n"
		+ "\tSELECT jobExec.JOB_EXECUTION_ID\n"
		+ "\tFROM %PREFIX%JOB_EXECUTION jobExec\n"
		+ "\tWHERE jobExec.JOB_INSTANCE_ID BETWEEN ? AND ?"
		+ ")";

	static final String DELETE_JOB_EXECUTION_CONTEXTS = "DELETE FROM %PREFIX%JOB_EXECUTION_CONTEXT\n"
		+ "WHERE JOB_EXECUTION_ID IN (\n"
		+ "\tSELECT jobExec.JOB_EXECUTION_ID\n"
		+ "\tFROM %PREFIX%JOB_EXECUTION jobExec\n"
		+ "\tWHERE jobExec.JOB_INSTANCE_ID BETWEEN ? AND ?"
		+ ")";

	static final String DELETE_STEP_EXECUTIONS = "DELETE FROM %PREFIX%STEP_EXECUTION\n"
		+ "WHERE JOB_EXECUTION_ID IN (\n"
		+ "\tSELECT jobExec.JOB_EXECUTION_ID\n"
		+ "\tFROM %PREFIX%JOB_EXECUTION jobExec\n"
		+ "\tWHERE jobExec.JOB_INSTANCE_ID BETWEEN ? AND ?"
		+ ")";

	static final String DELETE_STEP_EXECUTION_CONTEXTS = "DELETE FROM %PREFIX%STEP_EXECUTION_CONTEXT\n"
		+ "WHERE STEP_EXECUTION_ID IN (\n"
		+ "\tSELECT stepExec.STEP_EXECUTION_ID\n"
		+ "\tFROM %PREFIX%STEP_EXECUTION stepExec\n"
		+ "\t\tJOIN %PREFIX%JOB_EXECUTION jobExec ON jobExec.JOB_EXECUTION_ID = stepExec.JOB_EXECUTION_ID\n"
		+ "\tWHERE jobExec.JOB_INSTANCE_ID BETWEEN ? AND ?"
		+ ")";

	private MetadataSql() {
	}
}
