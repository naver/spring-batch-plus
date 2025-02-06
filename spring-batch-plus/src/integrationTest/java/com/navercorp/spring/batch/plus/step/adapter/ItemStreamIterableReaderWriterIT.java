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

import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.step.adapter.AdapterFactory.itemStreamWriter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.junit.jupiter.api.RepeatedTest;
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
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionManager;

@SuppressWarnings({"unchecked", "unused"})
class ItemStreamIterableReaderWriterIT {

	private static final int TEST_REPEAT_COUNT = 5;

	@RepeatedTest(TEST_REPEAT_COUNT)
	void iterableReaderWriterShouldNotKeepCountWhenStepScoped() throws Exception {
		int itemCount = ThreadLocalRandom.current().nextInt(10, 100);
		int chunkCount = ThreadLocalRandom.current().nextInt(1, 10);
		InvokeCountContext invokeCountContext = new InvokeCountContext();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean("itemCount", Integer.class, () -> itemCount);
		context.registerBean("invokeCountContext", InvokeCountContext.class, () -> invokeCountContext);
		context.register(StepScopedConfiguration.class);
		context.refresh();
		ItemStreamIterableReaderWriter<Integer> testTasklet = context.getBean("testTasklet",
			ItemStreamIterableReaderWriter.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, Integer>chunk(chunkCount, new ResourcelessTransactionManager())
					.reader(itemStreamReader(testTasklet))
					.writer(itemStreamWriter(testTasklet))
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		int repeatCount = ThreadLocalRandom.current().nextInt(1, 5);
		List<JobExecution> jobExecutions = new ArrayList<>();
		for (int i = 0; i < repeatCount; ++i) {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				.toJobParameters();
			JobExecution jobExecution = jobLauncher.run(job, jobParameters);
			jobExecutions.add(jobExecution);
		}

		assertThat(jobExecutions).allSatisfy(it -> assertThat(it.getStatus()).isEqualTo(BatchStatus.COMPLETED));
		// read context should be invoked
		assertThat(invokeCountContext.readContextCallCount).isEqualTo(repeatCount);
		// stream callback should be invoked
		assertThat(invokeCountContext.onOpenReadCallCount).isEqualTo(repeatCount);
		assertThat(invokeCountContext.onUpdateReadCallCount).isGreaterThanOrEqualTo(repeatCount);
		assertThat(invokeCountContext.onCloseReadCallCount).isEqualTo(repeatCount);
		assertThat(invokeCountContext.onOpenWriteCallCount).isEqualTo(repeatCount);
		assertThat(invokeCountContext.onUpdateWriteCallCount).isGreaterThanOrEqualTo(repeatCount);
		assertThat(invokeCountContext.onCloseWriteCallCount).isEqualTo(repeatCount);
		// 'count' field is isolated per job instances since it is step scoped. so count is 0 for all job instances
		int writeCountPerIteration = (int)Math.ceil((double)itemCount / (double)chunkCount);
		assertThat(invokeCountContext.writeCallCount).isEqualTo(repeatCount * writeCountPerIteration);
	}

	@RepeatedTest(TEST_REPEAT_COUNT)
	void iterableReaderWriterShouldKeepCountWhenNotStepScoped() throws Exception {
		int itemCount = ThreadLocalRandom.current().nextInt(10, 100);
		int chunkCount = ThreadLocalRandom.current().nextInt(1, 10);
		InvokeCountContext invokeCountContext = new InvokeCountContext();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean("itemCount", Integer.class, () -> itemCount);
		context.registerBean("invokeCountContext", InvokeCountContext.class, () -> invokeCountContext);
		context.register(NotStepScopedConfiguration.class);
		context.refresh();
		ItemStreamIterableReaderWriter<Integer> testTasklet = context.getBean("testTasklet",
			ItemStreamIterableReaderWriter.class);
		JobRepository jobRepository = context.getBean(JobRepository.class);
		Job job = new JobBuilder("testJob", jobRepository)
			.start(
				new StepBuilder("testStep", jobRepository)
					.<Integer, Integer>chunk(chunkCount, new ResourcelessTransactionManager())
					.reader(itemStreamReader(testTasklet))
					.writer(itemStreamWriter(testTasklet))
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		int repeatCount = ThreadLocalRandom.current().nextInt(1, 5);
		List<JobExecution> jobExecutions = new ArrayList<>();
		for (int i = 0; i < repeatCount; ++i) {
			JobParameters jobParameters = new JobParametersBuilder()
				.addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				.toJobParameters();
			JobExecution jobExecution = jobLauncher.run(job, jobParameters);
			jobExecutions.add(jobExecution);
		}

		assertThat(jobExecutions).allSatisfy(it -> assertThat(it.getStatus()).isEqualTo(BatchStatus.COMPLETED));
		// read context should be invoked
		assertThat(invokeCountContext.readContextCallCount).isEqualTo(repeatCount);
		// stream callback should be invoked
		assertThat(invokeCountContext.onOpenReadCallCount).isEqualTo(repeatCount);
		assertThat(invokeCountContext.onUpdateReadCallCount).isGreaterThanOrEqualTo(repeatCount);
		assertThat(invokeCountContext.onCloseReadCallCount).isEqualTo(repeatCount);
		assertThat(invokeCountContext.onOpenWriteCallCount).isEqualTo(repeatCount);
		assertThat(invokeCountContext.onUpdateWriteCallCount).isGreaterThanOrEqualTo(repeatCount);
		assertThat(invokeCountContext.onCloseWriteCallCount).isEqualTo(repeatCount);
		// process, write should be invoked only once per iteration
		int writeCountPerIteration = (int)Math.ceil((double)itemCount / (double)chunkCount);
		assertThat(invokeCountContext.writeCallCount).isEqualTo(writeCountPerIteration);
	}

	@EnableBatchProcessing(
		dataSourceRef = "metadataDataSource",
		transactionManagerRef = "metadataTransactionManager"
	)
	private static class StepScopedConfiguration {

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

		@StepScope
		@Bean
		TestTasklet testTasklet(
			InvokeCountContext invokeCountContext, int itemCount) {
			return new TestTasklet(invokeCountContext, itemCount);
		}
	}

	@EnableBatchProcessing(
		dataSourceRef = "metadataDataSource",
		transactionManagerRef = "metadataTransactionManager"
	)
	private static class NotStepScopedConfiguration {

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
		TestTasklet testTasklet(
			InvokeCountContext invokeCountContext, int itemCount) {
			return new TestTasklet(invokeCountContext, itemCount);
		}
	}

	private static class TestTasklet implements ItemStreamIterableReaderWriter<Integer> {

		private int count = 0;
		private final InvokeCountContext invokeCountContext;
		private final int itemCount;

		public TestTasklet(InvokeCountContext invokeCountContext, int itemCount) {
			this.invokeCountContext = invokeCountContext;
			this.itemCount = itemCount;
		}

		@Override
		public void onOpenRead(@NonNull ExecutionContext executionContext) {
			this.invokeCountContext.onOpenReadCallCount++;
		}

		@NonNull
		@Override
		public Iterable<? extends Integer> readIterable(@NonNull ExecutionContext executionContext) {
			this.invokeCountContext.readContextCallCount++;
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
			this.invokeCountContext.onUpdateReadCallCount++;
		}

		@Override
		public void onCloseRead() {
			this.invokeCountContext.onCloseReadCallCount++;
		}

		@Override
		public void onOpenWrite(@NonNull ExecutionContext executionContext) {
			this.invokeCountContext.onOpenWriteCallCount++;
		}

		@Override
		public void write(@NonNull Chunk<? extends Integer> chunk) {
			this.invokeCountContext.writeCallCount++;
		}

		@Override
		public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
			this.invokeCountContext.onUpdateWriteCallCount++;
		}

		@Override
		public void onCloseWrite() {
			this.invokeCountContext.onCloseWriteCallCount++;
		}
	}

	private static class InvokeCountContext {
		int onOpenReadCallCount = 0;
		int readContextCallCount = 0;
		int onUpdateReadCallCount = 0;
		int onCloseReadCallCount = 0;
		int onOpenWriteCallCount = 0;
		int writeCallCount = 0;
		int onUpdateWriteCallCount = 0;
		int onCloseWriteCallCount = 0;
	}
}
