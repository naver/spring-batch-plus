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

package com.navercorp.spring.batch.plus.sample.deletemetadata.prefixfromvariable;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.stream.LongStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BatchApplication implements ApplicationRunner {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRepository jobRepository;

	@Qualifier("testJob")
	@Autowired
	private Job testJob;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// run testJob 350 times
		List<JobExecution> jobExecutions = LongStream.range(0L, 350L).boxed()
			.map(it -> {
				try {
					JobParameters jobParameters = new JobParametersBuilder()
						.addLong("longValue", it)
						.toJobParameters();
					return jobLauncher.run(testJob, jobParameters);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			})
			.collect(toList());

		// change previous execution date for test
		jobExecutions.forEach(jobExecution -> {
			LocalDateTime createTime = jobExecution.getCreateTime();
			LocalDateTime startTime = jobExecution.getStartTime();
			LocalDateTime endTime = jobExecution.getEndTime();

			LocalDateTime updateCreateTime = createTime.minus(1, ChronoUnit.DAYS);
			LocalDateTime updateStartTime = startTime.minus(1, ChronoUnit.DAYS);
			LocalDateTime updateEndTime = endTime.minus(1, ChronoUnit.DAYS);

			JobExecution updateJobExecution = new JobExecution(jobExecution);
			updateJobExecution.setCreateTime(updateCreateTime);
			updateJobExecution.setStartTime(updateStartTime);
			updateJobExecution.setEndTime(updateEndTime);
			jobRepository.update(updateJobExecution);
		});
	}

	public static void main(String[] args) throws Exception {
		// launch with custom prefix
		SpringApplication application = new SpringApplication(BatchApplication.class);
		Properties properties = new Properties();
		properties.put("spring.batch.jdbc.table-prefix", "CUSTOM_");
		application.setDefaultProperties(properties);
		ApplicationContext applicationContext = application.run(args);
		JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

		// launch removeJob
		Job removeJob = applicationContext.getBean("removeJob", Job.class);
		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("baseDate", now.format(formatter))
			.toJobParameters();
		jobLauncher.run(removeJob, jobParameters);
	}
}
