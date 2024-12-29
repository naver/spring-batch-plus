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

package com.navercorp.spring.batch.plus.step.adapter;

import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemProcessor;
import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemStreamReader;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionManager;

@SuppressWarnings("unchecked")
class ItemStreamIterableReaderProcessorIntegrationTest {

	private static final int TEST_REPEAT_COUNT = 5;

	private static final Logger logger = LoggerFactory.getLogger(
		ItemStreamIterableReaderProcessorIntegrationTest.class);

	private static int onOpenReadCallCount = 0;
	private static int readContextCallCount = 0;
	private static int onUpdateReadCallCount = 0;
	private static int onCloseReadCallCount = 0;

	private static int processCallCount = 0;

	private static int itemCount = 0;
	private static int chunkCount = 0;

	@BeforeEach
	void beforeEach() {
		onOpenReadCallCount = 0;
		readContextCallCount = 0;
		onUpdateReadCallCount = 0;
		onCloseReadCallCount = 0;

		processCallCount = 0;

		itemCount = ThreadLocalRandom.current().nextInt(10, 100);
		chunkCount = ThreadLocalRandom.current().nextInt(1, 10);

		logger.debug("itemCount: {}, chunkCount: {}", itemCount, chunkCount);
	}

	@RepeatedTest(TEST_REPEAT_COUNT)
	void testReaderProcessor() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		ItemStreamIterableReaderProcessor<Integer, Integer> testTasklet = context.getBean(
			"testTasklet",
			ItemStreamIterableReaderProcessor.class);
		ItemStreamWriter<Integer> emptyItemStreamWriter = context.getBean(
			"emptyItemStreamWriter",
			ItemStreamWriter.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, Integer>chunk(chunkCount, new ResourcelessTransactionManager())
					.reader(itemStreamReader(testTasklet))
					.processor(itemProcessor(testTasklet))
					.writer(emptyItemStreamWriter)
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		int beforeRepeatCount = ThreadLocalRandom.current().nextInt(0, 3);
		for (int i = 0; i < beforeRepeatCount; ++i) {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				.toJobParameters();
			jobLauncher.run(job, jobParameters);
		}
		logger.debug("beforeRepeatCount: {}", beforeRepeatCount);

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(beforeRepeatCount + 1);
		assertThat(readContextCallCount).isEqualTo(beforeRepeatCount + 1);
		assertThat(onUpdateReadCallCount).isGreaterThanOrEqualTo(beforeRepeatCount + 1);
		assertThat(onCloseReadCallCount).isEqualTo(beforeRepeatCount + 1);
	}

	@RepeatedTest(TEST_REPEAT_COUNT)
	void testReaderProcessorWithSameTaskletShouldKeepContext() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		ItemStreamIterableReaderProcessor<Integer, Integer> testTasklet = context.getBean(
			"testTasklet",
			ItemStreamIterableReaderProcessor.class);
		ItemStreamWriter<Integer> emptyItemStreamWriter = context.getBean(
			"emptyItemStreamWriter",
			ItemStreamWriter.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, Integer>chunk(chunkCount, new ResourcelessTransactionManager())
					.reader(itemStreamReader(testTasklet))
					.processor(itemProcessor(testTasklet))
					.writer(emptyItemStreamWriter)
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		int beforeRepeatCount = ThreadLocalRandom.current().nextInt(0, 3);
		for (int i = 0; i < beforeRepeatCount; ++i) {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				.toJobParameters();
			jobLauncher.run(job, jobParameters);
		}
		logger.debug("beforeRepeatCount: {}", beforeRepeatCount);

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		// it's not changed since it keeps 'count' in a bean
		assertThat(processCallCount).isEqualTo(itemCount);
	}

	@RepeatedTest(TEST_REPEAT_COUNT)
	void testStepScopeReaderProcessor() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		ItemStreamIterableReaderProcessor<Integer, Integer> testTasklet = context.getBean(
			"stepScopeTestTasklet",
			ItemStreamIterableReaderProcessor.class);
		ItemStreamWriter<Integer> emptyItemStreamWriter = context.getBean(
			"emptyItemStreamWriter",
			ItemStreamWriter.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, Integer>chunk(chunkCount, new ResourcelessTransactionManager())
					.reader(itemStreamReader(testTasklet))
					.processor(itemProcessor(testTasklet))
					.writer(emptyItemStreamWriter)
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		int beforeRepeatCount = ThreadLocalRandom.current().nextInt(0, 3);
		for (int i = 0; i < beforeRepeatCount; ++i) {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				.toJobParameters();
			jobLauncher.run(job, jobParameters);
		}
		logger.debug("beforeRepeatCount: {}", beforeRepeatCount);

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(beforeRepeatCount + 1);
		assertThat(readContextCallCount).isEqualTo(beforeRepeatCount + 1);
		assertThat(onUpdateReadCallCount).isGreaterThanOrEqualTo(beforeRepeatCount + 1);
		assertThat(onCloseReadCallCount).isEqualTo(beforeRepeatCount + 1);
	}

	@RepeatedTest(TEST_REPEAT_COUNT)
	void testStepScopeReaderProcessorWithSameTaskletShouldNotKeepCountContext() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		ItemStreamIterableReaderProcessor<Integer, Integer> testTasklet = context.getBean(
			"stepScopeTestTasklet",
			ItemStreamIterableReaderProcessor.class);
		ItemStreamWriter<Integer> emptyItemStreamWriter = context.getBean(
			"emptyItemStreamWriter",
			ItemStreamWriter.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, Integer>chunk(chunkCount, new ResourcelessTransactionManager())
					.reader(itemStreamReader(testTasklet))
					.processor(itemProcessor(testTasklet))
					.writer(emptyItemStreamWriter)
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		int beforeRepeatCount = ThreadLocalRandom.current().nextInt(0, 3);
		for (int i = 0; i < beforeRepeatCount; ++i) {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				.toJobParameters();
			jobLauncher.run(job, jobParameters);
		}
		logger.debug("beforeRepeatCount: {}", beforeRepeatCount);

		// when
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		// 'count' field is isolated per job instances since it is step scoped. so count is 0 for all job instances
		assertThat(processCallCount).isEqualTo(beforeRepeatCount * itemCount + itemCount);
	}

	@SuppressWarnings("unused")
	@EnableBatchProcessing(
		dataSourceRef = "metadataDataSource",
		transactionManagerRef = "metadataTransactionManager"
	)
	static class TestConfiguration {

		@Bean
		TransactionManager metadataTransactionManager() {
			return new DataSourceTransactionManager(metadataDataSource());
		}

		@Bean
		DataSource metadataDataSource() {
			return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.addScript("/org/springframework/batch/core/schema-h2.sql")
				.generateUniqueName(true)
				.build();
		}

		@Bean
		ItemStreamIterableReaderProcessor<Integer, Integer> testTasklet() {
			return new ItemStreamIterableReaderProcessor<>() {

				private int count = 0;

				@Override
				public void onOpenRead(@NonNull ExecutionContext executionContext) {
					++onOpenReadCallCount;
				}

				@NonNull
				@Override
				public Iterable<? extends Integer> readIterable(@NonNull ExecutionContext executionContext) {
					++readContextCallCount;
					return () -> new Iterator<>() {
						@Override
						public boolean hasNext() {
							return count < itemCount;
						}

						@Override
						public Integer next() {
							if (count < itemCount) {
								return count++;
							} else {
								return null;
							}
						}
					};
				}

				@Override
				public void onUpdateRead(@NonNull ExecutionContext executionContext) {
					++onUpdateReadCallCount;
				}

				@Override
				public void onCloseRead() {
					++onCloseReadCallCount;
				}

				@Override
				public Integer process(@NonNull Integer item) {
					++processCallCount;
					return item;
				}
			};
		}

		@Bean
		@StepScope
		ItemStreamIterableReaderProcessor<Integer, Integer> stepScopeTestTasklet() {
			return new ItemStreamIterableReaderProcessor<>() {

				private int count = 0;

				@Override
				public void onOpenRead(@NonNull ExecutionContext executionContext) {
					++onOpenReadCallCount;
				}

				@NonNull
				@Override
				public Iterable<? extends Integer> readIterable(@NonNull ExecutionContext executionContext) {
					++readContextCallCount;
					return () -> new Iterator<>() {
						@Override
						public boolean hasNext() {
							return count < itemCount;
						}

						@Override
						public Integer next() {
							if (count < itemCount) {
								return count++;
							} else {
								return null;
							}
						}
					};
				}

				@Override
				public void onUpdateRead(@NonNull ExecutionContext executionContext) {
					++onUpdateReadCallCount;
				}

				@Override
				public void onCloseRead() {
					++onCloseReadCallCount;
				}

				@Override
				public Integer process(@NonNull Integer item) {
					++processCallCount;
					return item;
				}
			};
		}

		@Bean
		@StepScope
		ItemStreamWriter<Integer> emptyItemStreamWriter() {
			return chunk -> { /* noop */ };
		}
	}
}
