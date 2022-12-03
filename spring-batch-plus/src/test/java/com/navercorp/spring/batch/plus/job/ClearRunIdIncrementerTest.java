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

import static com.navercorp.spring.batch.plus.job.ClearRunIdIncrementer.DEFAULT_RUN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

class ClearRunIdIncrementerTest {

	@Test
	void testGetNextReturnsOneWhenNoPreviousOne() {
		// given
		JobParametersIncrementer clearRunIdIncrementer = ClearRunIdIncrementer.create();

		// when
		JobParameters jobParameters = clearRunIdIncrementer.getNext(new JobParameters());

		// then
		assertThat(jobParameters.getLong(DEFAULT_RUN_ID)).isEqualTo(1L);
	}

	@Test
	void testGetNextReturnsNextValue() {
		// given
		JobParametersIncrementer clearRunIdIncrementer = ClearRunIdIncrementer.create();

		// when
		long previousId = ThreadLocalRandom.current().nextLong();
		JobParameters parameters = new JobParametersBuilder()
			.addLong(DEFAULT_RUN_ID, previousId)
			.toJobParameters();
		JobParameters jobParameters = clearRunIdIncrementer.getNext(parameters);

		// then
		assertThat(jobParameters.getLong(DEFAULT_RUN_ID)).isEqualTo(previousId + 1);
	}

	@Test
	void testCustomRunId() {
		// given
		String runId = UUID.randomUUID().toString();
		JobParametersIncrementer clearRunIdIncrementer = ClearRunIdIncrementer.create(runId);

		// when
		long previousId = ThreadLocalRandom.current().nextLong();
		JobParameters parameters = new JobParametersBuilder()
			.addLong(runId, previousId)
			.toJobParameters();
		JobParameters jobParameters = clearRunIdIncrementer.getNext(parameters);

		// then
		assertThat(jobParameters.getLong(runId)).isEqualTo(previousId + 1);
		assertThat(jobParameters.getLong(DEFAULT_RUN_ID)).isNull();
	}

	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ClearRunIdIncrementer.create(null));
	}
}
