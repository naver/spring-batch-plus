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

package com.navercorp.spring.batch.plus.item;

import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamWriter;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Flux;

class ItemStreamReaderWriterIntegrationTest {

	private static int onOpenReadCallCount = 0;
	private static int readFluxCallCount = 0;
	private static int onUpdateReadCallCount = 0;
	private static int onCloseReadCallCount = 0;

	private static int onOpenWriteCallCount = 0;
	private static int writeCallCount = 0;
	private static int onUpdateWriteCallCount = 0;
	private static int onCloseWriteCallCount = 0;

	@BeforeEach
	void beforeEach() {
		onOpenReadCallCount = 0;
		readFluxCallCount = 0;
		onUpdateReadCallCount = 0;
		onCloseReadCallCount = 0;

		onOpenWriteCallCount = 0;
		writeCallCount = 0;
		onUpdateWriteCallCount = 0;
		onCloseWriteCallCount = 0;
	}

	@SuppressWarnings("unchecked")
	@Test
	void testReaderWriter() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobBuilderFactory jobBuilderFactory = context.getBean(JobBuilderFactory.class);
		StepBuilderFactory stepBuilderFactory = context.getBean(StepBuilderFactory.class);
		ItemStreamReaderWriter<Integer> testTasklet = context.getBean("testTasklet",
			ItemStreamReaderWriter.class);
		Job job = jobBuilderFactory.get("testJob")
			.start(
				stepBuilderFactory.get("testStep")
					.<Integer, Integer>chunk(3)
					.reader(itemStreamReader(testTasklet))
					.writer(itemStreamWriter(testTasklet))
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		// when, then
		JobParameters jobParameters1 = new JobParametersBuilder()
			.addString("test", UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution1 = jobLauncher.run(job, jobParameters1);
		assertThat(jobExecution1.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(1);
		assertThat(readFluxCallCount).isEqualTo(1);
		assertThat(onUpdateReadCallCount).isEqualTo(8);
		assertThat(onCloseReadCallCount).isEqualTo(1);
		assertThat(onOpenWriteCallCount).isEqualTo(1);
		assertThat(writeCallCount).isEqualTo(7); // ceil(20/3)
		assertThat(onUpdateWriteCallCount).isEqualTo(8);
		assertThat(onCloseWriteCallCount).isEqualTo(1);

		// when, then
		JobParameters jobParameters2 = new JobParametersBuilder()
			.addString("test", UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution2 = jobLauncher.run(job, jobParameters2);
		assertThat(jobExecution2.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(1 + 1);
		assertThat(readFluxCallCount).isEqualTo(1 + 1);
		assertThat(onUpdateReadCallCount).isEqualTo(8 + 2);
		assertThat(onCloseReadCallCount).isEqualTo(1 + 1);
		assertThat(onOpenWriteCallCount).isEqualTo(1 + 1);
		assertThat(writeCallCount).isEqualTo(7); // same as previous since it's not step scoped
		assertThat(onUpdateWriteCallCount).isEqualTo(8 + 2);
		assertThat(onCloseWriteCallCount).isEqualTo(1 + 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testStepScopeReaderWriter() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobBuilderFactory jobBuilderFactory = context.getBean(JobBuilderFactory.class);
		StepBuilderFactory stepBuilderFactory = context.getBean(StepBuilderFactory.class);
		ItemStreamReaderWriter<Integer> testTasklet = context.getBean("stepScopeTestTasklet",
			ItemStreamReaderWriter.class);
		Job job = jobBuilderFactory.get("testJob")
			.start(
				stepBuilderFactory.get("testStep")
					.<Integer, Integer>chunk(3)
					.reader(itemStreamReader(testTasklet))
					.writer(itemStreamWriter(testTasklet))
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		// when, then
		JobParameters jobParameters1 = new JobParametersBuilder()
			.addString("test", UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution1 = jobLauncher.run(job, jobParameters1);
		assertThat(jobExecution1.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(1);
		assertThat(readFluxCallCount).isEqualTo(1);
		assertThat(onUpdateReadCallCount).isEqualTo(8);
		assertThat(onCloseReadCallCount).isEqualTo(1);
		assertThat(onOpenWriteCallCount).isEqualTo(1);
		assertThat(writeCallCount).isEqualTo(7); // ceil(20/3)
		assertThat(onUpdateWriteCallCount).isEqualTo(8);
		assertThat(onCloseWriteCallCount).isEqualTo(1);

		// when, then
		JobParameters jobParameters2 = new JobParametersBuilder()
			.addString("test", UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution2 = jobLauncher.run(job, jobParameters2);
		assertThat(jobExecution2.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(1 + 1);
		assertThat(readFluxCallCount).isEqualTo(1 + 1);
		assertThat(onUpdateReadCallCount).isEqualTo(8 + 8);
		assertThat(onCloseReadCallCount).isEqualTo(1 + 1);
		assertThat(onOpenWriteCallCount).isEqualTo(1 + 1);
		assertThat(writeCallCount).isEqualTo(7 + 7); // ceil(20/3) * 2
		assertThat(onUpdateWriteCallCount).isEqualTo(8 + 8);
		assertThat(onCloseWriteCallCount).isEqualTo(1 + 1);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testReaderWriterWithRequiredMethodsOnly() throws Exception {
		// given
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		JobBuilderFactory jobBuilderFactory = context.getBean(JobBuilderFactory.class);
		StepBuilderFactory stepBuilderFactory = context.getBean(StepBuilderFactory.class);
		ItemStreamReaderWriter<Integer> testTasklet = context.getBean(
			"testTaskletWithOnlyRequiredMethodsOnly", ItemStreamReaderWriter.class);
		Job job = jobBuilderFactory.get("testJob")
			.start(
				stepBuilderFactory.get("testStep")
					.<Integer, Integer>chunk(3)
					.reader(itemStreamReader(testTasklet))
					.writer(itemStreamWriter(testTasklet))
					.build()
			)
			.build();
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);

		// when, then
		JobParameters jobParameters1 = new JobParametersBuilder()
			.addString("test", UUID.randomUUID().toString())
			.toJobParameters();
		JobExecution jobExecution1 = jobLauncher.run(job, jobParameters1);
		assertThat(jobExecution1.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(onOpenReadCallCount).isEqualTo(0);
		assertThat(readFluxCallCount).isEqualTo(1);
		assertThat(onUpdateReadCallCount).isEqualTo(0);
		assertThat(onCloseReadCallCount).isEqualTo(0);
		assertThat(onOpenWriteCallCount).isEqualTo(0);
		assertThat(writeCallCount).isEqualTo(7); // ceil(20/3)
		assertThat(onUpdateWriteCallCount).isEqualTo(0);
		assertThat(onCloseWriteCallCount).isEqualTo(0);
	}

	@SuppressWarnings("unused")
	@EnableBatchProcessing
	static class TestConfiguration {

		@Bean
		ItemStreamReaderWriter<Integer> testTasklet() {
			return new ItemStreamReaderWriter<Integer>() {

				private int count = 0;

				@Override
				public void onOpenRead(ExecutionContext executionContext) {
					++onOpenReadCallCount;
				}

				@NotNull
				@Override
				public Flux<Integer> readFlux(ExecutionContext executionContext) {
					++readFluxCallCount;
					return Flux.generate(sink -> {
						if (count < 20) {
							sink.next(count);
							++count;
						} else {
							sink.complete();
						}
					});
				}

				@Override
				public void onUpdateRead(ExecutionContext executionContext) {
					++onUpdateReadCallCount;
				}

				@Override
				public void onCloseRead() {
					++onCloseReadCallCount;
				}

				@Override
				public void onOpenWrite(ExecutionContext executionContext) {
					++onOpenWriteCallCount;
				}

				@Override
				public void write(List<? extends Integer> items) {
					++writeCallCount;
				}

				@Override
				public void onUpdateWrite(ExecutionContext executionContext) {
					++onUpdateWriteCallCount;
				}

				@Override
				public void onCloseWrite() {
					++onCloseWriteCallCount;
				}
			};
		}

		@Bean
		@StepScope
		ItemStreamReaderWriter<Integer> stepScopeTestTasklet() {
			return new ItemStreamReaderWriter<Integer>() {

				private int count = 0;

				@Override
				public void onOpenRead(ExecutionContext executionContext) {
					++onOpenReadCallCount;
				}

				@NotNull
				@Override
				public Flux<Integer> readFlux(ExecutionContext executionContext) {
					++readFluxCallCount;
					return Flux.generate(sink -> {
						if (count < 20) {
							sink.next(count);
							++count;
						} else {
							sink.complete();
						}
					});
				}

				@Override
				public void onUpdateRead(ExecutionContext executionContext) {
					++onUpdateReadCallCount;
				}

				@Override
				public void onCloseRead() {
					++onCloseReadCallCount;
				}

				@Override
				public void onOpenWrite(ExecutionContext executionContext) {
					++onOpenWriteCallCount;
				}

				@Override
				public void write(List<? extends Integer> items) {
					++writeCallCount;
				}

				@Override
				public void onUpdateWrite(ExecutionContext executionContext) {
					++onUpdateWriteCallCount;
				}

				@Override
				public void onCloseWrite() {
					++onCloseWriteCallCount;
				}
			};
		}

		@Bean
		ItemStreamReaderWriter<Integer> testTaskletWithOnlyRequiredMethodsOnly() {
			return new ItemStreamReaderWriter<Integer>() {

				private int count = 0;

				@NotNull
				@Override
				public Flux<Integer> readFlux(ExecutionContext executionContext) {
					++readFluxCallCount;
					return Flux.generate(sink -> {
						if (count < 20) {
							sink.next(count);
							++count;
						} else {
							sink.complete();
						}
					});
				}

				@Override
				public void write(List<? extends Integer> items) {
					++writeCallCount;
				}
			};
		}
	}
}
