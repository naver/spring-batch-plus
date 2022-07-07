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

package com.navecorp.spring.batch.plus.sample.readerwriter;

import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamWriter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class BatchApplication {

	@Bean
	Job testJob(
		JobBuilderFactory jobBuilderFactory,
		StepBuilderFactory stepBuilderFactory,
		SampleTasklet sampleTasklet
	) {
		return jobBuilderFactory.get("testJob")
			.start(
				stepBuilderFactory.get("testStep")
					.<Integer, Integer>chunk(3)
					.reader(itemStreamReader(sampleTasklet))
					.writer(itemStreamWriter(sampleTasklet))
					.build()
			)
			.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class);
	}
}
