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

package com.navecorp.spring.batch.plus.sample.comparison.good;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.navercorp.spring.batch.plus.job.ClearRunIdIncrementer;

@Configuration
public class TestJobConfig {

	@Bean
	public Job testJob(
		JobRepository jobRepository
	) {
		return new JobBuilder("testJob", jobRepository)
			.incrementer(ClearRunIdIncrementer.create()) // use ClearRunIdIncrementer
			.start(
				new StepBuilder("testStep", jobRepository)
					.tasklet(
						testTasklet(null, null),
						new ResourcelessTransactionManager()
					)
					.build()
			)
			.build();
	}

	@StepScope
	@Bean
	public Tasklet testTasklet(
		@Value("#{jobParameters['longValue']}") Long longValue,
		@Value("#{jobParameters['stringValue']}") String stringValue
	) {
		return (contribution, chunkContext) -> {
			Long result;
			if (stringValue != null) {
				result = longValue + Long.parseLong(stringValue);
			} else {
				result = 999L;
			}

			ExecutionContext jobExecutionContext = contribution.getStepExecution()
				.getJobExecution()
				.getExecutionContext();
			jobExecutionContext.putLong("result", result);
			return RepeatStatus.FINISHED;
		};
	}
}
