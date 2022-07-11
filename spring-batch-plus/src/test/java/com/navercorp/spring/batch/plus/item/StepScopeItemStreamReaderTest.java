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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;

@SuppressWarnings("NullableProblems")
class StepScopeItemStreamReaderTest {

	@Test
	void testOpen() throws Exception {
		// given
		AtomicInteger openCallCount = new AtomicInteger();
		ItemStreamReader<Integer> delegate = new ItemStreamReader<Integer>() {

			@Override
			public void open(ExecutionContext executionContext) {
				openCallCount.incrementAndGet();
			}

			@Override
			public Integer read() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void update(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}
		};
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemStreamReader.open(new ExecutionContext());
			return null;
		});

		// then
		assertThat(openCallCount.get()).isEqualTo(1);
	}

	@Test
	void testRead() throws Exception {
		// given
		ItemStreamReader<Integer> delegate = new ItemStreamReader<Integer>() {

			@Override
			public void open(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Integer read() {
				return 1;
			}

			@Override
			public void update(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}
		};
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		int actual = StepScopeTestUtils.doInStepScope(stepExecution, itemStreamReader::read);

		// then
		assertThat(actual).isEqualTo(1);
	}

	@Test
	void testUpdate() throws Exception {
		// given
		AtomicInteger updateCallCount = new AtomicInteger();
		ItemStreamReader<Integer> delegate = new ItemStreamReader<Integer>() {

			@Override
			public void open(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Integer read() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void update(ExecutionContext executionContext) {
				updateCallCount.incrementAndGet();
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}
		};
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemStreamReader.update(new ExecutionContext());
			return null;
		});

		// then
		assertThat(updateCallCount.get()).isEqualTo(1);
	}

	@Test
	void testClose() throws Exception {
		// given
		AtomicInteger closeCallCount = new AtomicInteger();
		ItemStreamReader<Integer> delegate = new ItemStreamReader<Integer>() {

			@Override
			public void open(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Integer read() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void update(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				closeCallCount.incrementAndGet();
			}
		};
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemStreamReader.close();
			return null;
		});

		// then
		assertThat(closeCallCount.get()).isEqualTo(1);
	}

	@Test
	void testOpenShouldThrowsExceptionWhenNoStepScope() {
		// given
		AtomicInteger openCallCount = new AtomicInteger();
		ItemStreamReader<Integer> delegate = new ItemStreamReader<Integer>() {

			@Override
			public void open(ExecutionContext executionContext) {
				openCallCount.incrementAndGet();
			}

			@Override
			public Integer read() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void update(ExecutionContext executionContext) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() {
				throw new UnsupportedOperationException();
			}
		};
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		assertThatThrownBy(
			() -> itemStreamReader.open(new ExecutionContext())
		).hasMessageContaining("No step context is set. Make sure if it's invoked in a stepScope.");
	}
}
