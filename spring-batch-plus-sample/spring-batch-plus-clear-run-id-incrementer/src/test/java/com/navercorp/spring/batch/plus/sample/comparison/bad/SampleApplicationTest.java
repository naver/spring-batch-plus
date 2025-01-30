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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SampleApplicationTest {
	@Test
	void run() throws Exception {
		ApplicationContext applicationContext = SpringApplication.run(SampleApplicationTest.class);
		JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
		JobExplorer jobExplorer = applicationContext.getBean(JobExplorer.class);
		Job job = applicationContext.getBean(Job.class);

		JobParameters firstJobParameters = new JobParametersBuilder(jobExplorer)
			.addString("stringValue", "1")
			.addString("longValue", "10")
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution firstJobExecution = jobLauncher.run(job, firstJobParameters);

		JobParameters secondJobParameters = new JobParametersBuilder(jobExplorer)
			.addString("longValue", "20")
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution secondJobExecution = jobLauncher.run(job, secondJobParameters);

		// first
		assert BatchStatus.COMPLETED.equals(firstJobExecution.getStatus());
		assert 1L == Objects.requireNonNull(firstJobExecution.getJobParameters().getLong("run.id"));
		assert "1".equals(firstJobExecution.getJobParameters().getString("stringValue"));
		assert "10".equals(firstJobExecution.getJobParameters().getString("longValue"));
		assert 11L == firstJobExecution.getExecutionContext().getLong("result");
		System.out.printf("first: %s, jobParameters: %s, result: %d%n",
			firstJobExecution.getStatus(),
			firstJobExecution.getJobParameters(),
			firstJobExecution.getExecutionContext().getLong("result"));

		// second
		assert BatchStatus.COMPLETED.equals(secondJobExecution.getStatus());
		assert 2L == Objects.requireNonNull(secondJobExecution.getJobParameters().getLong("run.id"));
		assert "1".equals(secondJobExecution.getJobParameters().getString("stringValue"));
		assert "20".equals(secondJobExecution.getJobParameters().getString("longValue"));
		assert 21L == secondJobExecution.getExecutionContext().getLong("result");
		System.out.printf("second: %s, jobParameters: %s, result: %d%n",
			secondJobExecution.getStatus(),
			secondJobExecution.getJobParameters(),
			secondJobExecution.getExecutionContext().getLong("result"));
	}
}
