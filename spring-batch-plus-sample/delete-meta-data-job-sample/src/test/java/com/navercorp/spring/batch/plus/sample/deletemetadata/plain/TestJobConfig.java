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

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.navercorp.spring.batch.plus.job.metadata.DeleteMetadataJobBuilder;

@Configuration
public class TestJobConfig {

	@Bean
	public Job deleteMetadataJob(
		@BatchDataSource DataSource dataSource,
		JobRepository jobRepository
	) {
		return new DeleteMetadataJobBuilder(jobRepository, dataSource)
			.build();
	}

	@Bean
	public Job testJob(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager
	) {
		return new JobBuilder("testJob", jobRepository)
			.incrementer(new RunIdIncrementer())
			.start(
				new StepBuilder("testStep", jobRepository)
					.tasklet(
						(contribution, chunkContext) -> RepeatStatus.FINISHED,
						transactionManager
					)
					.build()
			)
			.build();
	}
}
