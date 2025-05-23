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

package com.navercorp.spring.batch.plus.sample.deletemetadata.plain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SampleApplicationTest {
	@Test
	void run() throws Exception {
		ApplicationContext applicationContext = SpringApplication.run(SampleApplicationTest.class);
		JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
		JobRepository jobRepository = applicationContext.getBean(JobRepository.class);

		// prepare job instances
		Job testJob = applicationContext.getBean("testJob", Job.class);
		List<JobParameters> testJobParameterList = LongStream.range(0L, 250L).boxed()
			.map(it -> new JobParametersBuilder()
				.addLong("longValue", it)
				.toJobParameters()
			).toList();
		for (JobParameters testJobParameters : testJobParameterList) {
			// change create time date for test
			JobExecution jobExecution = jobLauncher.run(testJob, testJobParameters);
			jobExecution.setCreateTime(jobExecution.getCreateTime().minusDays(1));
			jobRepository.update(jobExecution);
		}

		// launch deleteMetadataJob
		Job removeJob = applicationContext.getBean("deleteMetadataJob", Job.class);
		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", now.format(formatter))
			.toJobParameters();
		jobLauncher.run(removeJob, jobParameters);

		// all instances are removed
		for (JobParameters testJobParameters : testJobParameterList) {
			JobInstance jobInstance = jobRepository.getJobInstance("testJob", testJobParameters);
			assert null == jobInstance;
		}
	}
}
