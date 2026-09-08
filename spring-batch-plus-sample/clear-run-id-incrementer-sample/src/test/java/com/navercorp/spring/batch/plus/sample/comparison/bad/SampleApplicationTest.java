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

package com.navercorp.spring.batch.plus.sample.comparison.bad;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Shows RunIdIncrementer carrying the previous run's parameters into the next job instance.
 */
@SpringBootApplication
public class SampleApplicationTest {
	@Test
	void run() throws Exception {
		ApplicationContext applicationContext = SpringApplication.run(SampleApplicationTest.class);
		JobRepository jobRepository = applicationContext.getBean(JobRepository.class);
		PlatformTransactionManager transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
		JobOperator jobOperator = applicationContext.getBean(JobOperator.class);

		Step testStep = new StepBuilder("testStep", jobRepository)
			.tasklet(
				(contribution, chunkContext) -> RepeatStatus.FINISHED,
				transactionManager
			)
			.build();

		Job jobBeforeIncrementer = new JobBuilder("testJob", jobRepository)
			.start(testStep)
			.build();
		JobParameters parameters = new JobParametersBuilder()
			.addString("stringValue", "1")
			.addString("longValue", "10")
			.toJobParameters();
		jobOperator.start(jobBeforeIncrementer, parameters);

		Job jobWithIncrementer = new JobBuilder("testJob", jobRepository)
			.incrementer(new RunIdIncrementer())
			.start(testStep)
			.build();
		JobExecution actual = jobOperator.startNextInstance(jobWithIncrementer);

		assert BatchStatus.COMPLETED.equals(actual.getStatus());
		JobParameters actualParameters = actual.getJobParameters();
		assert 1L == Objects.requireNonNull(actualParameters.getLong("run.id"));
		assert "1".equals(actualParameters.getString("stringValue"));
		assert "10".equals(actualParameters.getString("longValue"));
		System.out.printf("bad: params=%s%n", actualParameters);
	}
}
