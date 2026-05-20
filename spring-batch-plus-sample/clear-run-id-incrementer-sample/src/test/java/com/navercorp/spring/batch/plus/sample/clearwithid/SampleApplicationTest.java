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

package com.navercorp.spring.batch.plus.sample.clearwithid;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SampleApplicationTest {
	@Test
	void run() throws Exception {
		ApplicationContext applicationContext = SpringApplication.run(SampleApplicationTest.class);
		JobOperator jobOperator = applicationContext.getBean(JobOperator.class);
		Job job = applicationContext.getBean(Job.class);

		JobExecution firstJobExecution = jobOperator.startNextInstance(job);
		JobExecution secondJobExecution = jobOperator.startNextInstance(job);

		// first
		assert BatchStatus.COMPLETED.equals(firstJobExecution.getStatus());
		assert 1L == Objects.requireNonNull(firstJobExecution.getJobParameters().getLong("testId"));
		System.out.printf("first: %s, jobParameters: %s%n", firstJobExecution.getStatus(),
			firstJobExecution.getJobParameters());

		// second
		assert BatchStatus.COMPLETED.equals(secondJobExecution.getStatus());
		assert 2L == Objects.requireNonNull(secondJobExecution.getJobParameters().getLong("testId"));
		System.out.printf("second: %s, jobParameters: %s%n", secondJobExecution.getStatus(),
			secondJobExecution.getJobParameters());
	}
}
