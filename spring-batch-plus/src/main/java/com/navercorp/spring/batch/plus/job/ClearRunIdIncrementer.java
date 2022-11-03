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

import java.util.Optional;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.lang.NonNull;

/**
 * Alternative to {@link RunIdIncrementer}.
 * RunIdIncrementer returns not only a new run id but also all previous job parameters.
 * It makes unintended job parameters for a job.
 *
 * @since 0.1.0
 */
public class ClearRunIdIncrementer implements JobParametersIncrementer {

	protected static final String DEFAULT_RUN_ID = "run.id";

	/**
	 * Create a new ClearRunIdIncrementer with {@link #DEFAULT_RUN_ID}.
	 * @return a new ClearRunIdIncrementer instance
	 */
	public static JobParametersIncrementer create() {
		return create(DEFAULT_RUN_ID);
	}

	/**
	 * Create a new ClearRunIdIncrementer with custom run id.
	 *
	 * @param runId a run id
	 * @return a new ClearRunIdIncrementer instance
	 */
	public static JobParametersIncrementer create(String runId) {
		return new ClearRunIdIncrementer(runId);
	}

	protected final String runId;

	protected ClearRunIdIncrementer(String runId) {
		this.runId = runId;
	}

	@NonNull
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
