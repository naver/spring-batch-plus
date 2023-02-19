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

package com.navecorp.spring.batch.plus.sample.clear;

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
public class BatchApplication {

	public static void main(String[] args) throws Exception {
		ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
		JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
		JobExplorer jobExplorer = applicationContext.getBean(JobExplorer.class);
		Job job = applicationContext.getBean(Job.class);

		JobParameters firstJobParameter = new JobParametersBuilder(jobExplorer)
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution firstJobExecution = jobLauncher.run(job, firstJobParameter);

		JobParameters secondJobParameter = new JobParametersBuilder(jobExplorer)
			.getNextJobParameters(job)
			.toJobParameters();
		JobExecution secondJobExecution = jobLauncher.run(job, secondJobParameter);

		// first: COMPLETED, jobParameters: {run.id=1}
		System.out.printf("first: %s, jobParameters: %s%n", firstJobExecution.getExitStatus().getExitCode(),
			firstJobExecution.getJobParameters());

		// second: COMPLETED, jobParameters: {run.id=2}
		System.out.printf("second: %s, jobParameters: %s%n", secondJobExecution.getExitStatus().getExitCode(),
			secondJobExecution.getJobParameters());
	}
}
