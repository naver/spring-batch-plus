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

package com.navercorp.spring.batch.plus.sample.iterable.callback;

import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemProcessor;
import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemStreamWriter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TestJobConfig {

	@Bean
	public Job testJob(
		SampleTasklet sampleTasklet,
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager
	) {
		return new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, String>chunk(3, transactionManager)
					.reader(itemStreamReader(sampleTasklet))
					.processor(itemProcessor(sampleTasklet))
					.writer(itemStreamWriter(sampleTasklet))
					.build()
			)
			.build();
	}
}
