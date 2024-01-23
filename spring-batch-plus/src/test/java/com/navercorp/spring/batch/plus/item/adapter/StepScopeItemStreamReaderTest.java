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

package com.navercorp.spring.batch.plus.item.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;

@SuppressWarnings("unchecked")
class StepScopeItemStreamReaderTest {

	@Test
	void testOpen() throws Exception {
		// given
		ItemStreamReader<Integer> delegate = mock(ItemStreamReader.class);
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemStreamReader.open(new ExecutionContext());
			return null;
		});

		// then
		verify(delegate, times(1)).open(any());
	}

	@Test
	void testRead() throws Exception {
		// given
		Integer expected = ThreadLocalRandom.current().nextInt();
		ItemStreamReader<Integer> delegate = mock(ItemStreamReader.class);
		when(delegate.read()).thenReturn(expected);
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		Integer actual = StepScopeTestUtils.doInStepScope(stepExecution, itemStreamReader::read);

		// then
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void testUpdate() throws Exception {
		// given
		ItemStreamReader<Integer> delegate = mock(ItemStreamReader.class);
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemStreamReader.update(new ExecutionContext());
			return null;
		});

		// then
		verify(delegate, times(1)).update(any());
	}

	@Test
	void testClose() throws Exception {
		// given
		ItemStreamReader<Integer> delegate = mock(ItemStreamReader.class);
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemStreamReader.close();
			return null;
		});

		// then
		verify(delegate, times(1)).close();
	}

	@Test
	void testInvokeShouldThrowsExceptionWhenNoStepScope() {
		// given
		ItemStreamReader<Integer> delegate = mock(ItemStreamReader.class);
		ItemStreamReader<Integer> itemStreamReader = StepScopeItemStreamReader.of(() -> delegate);

		// when
		assertThatThrownBy(
			() -> itemStreamReader.open(new ExecutionContext())
		).hasMessageContaining("No step context is set. Make sure if it's invoked in a stepScope.");
		assertThatThrownBy(
			itemStreamReader::read
		).hasMessageContaining("No step context is set. Make sure if it's invoked in a stepScope.");
		assertThatThrownBy(
			() -> itemStreamReader.update(new ExecutionContext())
		).hasMessageContaining("No step context is set. Make sure if it's invoked in a stepScope.");
		assertThatThrownBy(
			itemStreamReader::close
		).hasMessageContaining("No step context is set. Make sure if it's invoked in a stepScope.");
	}

	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> StepScopeItemStreamReader.of(null));
	}
}
