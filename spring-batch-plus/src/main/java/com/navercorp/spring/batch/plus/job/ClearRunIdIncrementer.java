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

package com.navercorp.spring.batch.plus.job;

import java.util.Objects;
import java.util.Optional;

import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;

/**
 * A {@link JobParametersIncrementer} that increments a run-id parameter while
 * discarding every other parameter from the supplied {@link JobParameters}.
 *
 * <p>Use this instead of {@link RunIdIncrementer} when parameters from the previous job
 * execution must not propagate to the next job instance.
 *
 * @since 0.1.0
 */
public class ClearRunIdIncrementer implements JobParametersIncrementer {

	protected static final String DEFAULT_RUN_ID = "run.id";

	/**
	 * Creates an incrementer using {@link #DEFAULT_RUN_ID} as the run-id parameter name.
	 * @return a new incrementer
	 */
	public static JobParametersIncrementer create() {
		return create(DEFAULT_RUN_ID);
	}

	/**
	 * Creates an incrementer using the given run-id parameter name.
	 *
	 * @param runId the run-id parameter name
	 * @return a new incrementer
	 */
	public static JobParametersIncrementer create(String runId) {
		return new ClearRunIdIncrementer(runId);
	}

	protected final String runId;

	protected ClearRunIdIncrementer(String runId) {
		this.runId = Objects.requireNonNull(runId, "Run id must not be null");
	}

	@Override
	public JobParameters getNext(JobParameters parameters) {
		long nextId = Optional.ofNullable(parameters)
			.map(it -> it.getLong(runId))
			.map(it -> it + 1)
			.orElse(1L);

		return new JobParametersBuilder()
			.addLong(runId, nextId)
			.toJobParameters();
	}
}
