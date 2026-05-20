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

package com.navercorp.spring.batch.plus.sample.comparison.good;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Demonstrates how {@link com.navercorp.spring.batch.plus.job.ClearRunIdIncrementer} drops
 * every previous identifying parameter so each new JobInstance carries only the incremented
 * {@code run.id}.
 *
 * <p>The Spring Batch 6.0 standard flow ({@code JobOperator.start} + {@code startNextInstance})
 * never produces a prev JobExecution whose JobParameters contain keys other than {@code run.id},
 * so the difference between
 * {@link org.springframework.batch.core.job.parameters.RunIdIncrementer} and
 * {@code ClearRunIdIncrementer} is invisible in a clean 6.0 project. The legacy state seeded
 * here reproduces a 5.x &rarr; 6.0 migration: prior executions populated
 * {@code BATCH_JOB_EXECUTION_PARAMS} via the now-removed
 * {@code JobParametersBuilder.getNextJobParameters} pattern, leaving non-runId keys on the
 * latest JobInstance. {@code ClearRunIdIncrementer} cuts the chain so the next JobInstance
 * starts from a minimal identity. The companion {@code bad} sample shows what
 * {@code RunIdIncrementer} does with the same seed.
 */
@SpringBootApplication
public class SampleApplicationTest {
	@Test
	void run() throws Exception {
		ApplicationContext applicationContext = SpringApplication.run(SampleApplicationTest.class);
		JobRepository jobRepository = applicationContext.getBean(JobRepository.class);
		JobOperator jobOperator = applicationContext.getBean(JobOperator.class);
		Job job = applicationContext.getBean(Job.class);

		// Same legacy seed as the "bad" scenario for an apples-to-apples comparison.
		// See the class-level Javadoc for the migration context.
		JobParameters legacyParams = new JobParametersBuilder()
			.addString("stringValue", "1")
			.addString("longValue", "10")
			.addLong("run.id", 5L)
			.toJobParameters();
		JobInstance legacyInstance = jobRepository.createJobInstance(job.getName(), legacyParams);
		JobExecution legacyExecution = jobRepository.createJobExecution(legacyInstance, legacyParams,
			new ExecutionContext());
		legacyExecution.setStatus(BatchStatus.COMPLETED);
		legacyExecution.setExitStatus(ExitStatus.COMPLETED);
		jobRepository.update(legacyExecution);

		JobExecution nextExecution = jobOperator.startNextInstance(job);

		assert BatchStatus.COMPLETED.equals(nextExecution.getStatus());
		JobParameters nextParams = nextExecution.getJobParameters();

		// ClearRunIdIncrementer drops every previous identifying key and keeps only run.id,
		// so legacy keys never propagate into the new JobInstance.
		assert 6L == Objects.requireNonNull(nextParams.getLong("run.id"));
		assert nextParams.getString("stringValue") == null;
		assert nextParams.getString("longValue") == null;
		System.out.printf("good: params=%s%n", nextParams);
	}
}
