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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

@SuppressWarnings("unchecked")
class ItemStreamIterableReaderAdapterTest {

	@Test
	void openShouldInvokeProperDelegateMethods() {
		ItemStreamIterableReaderDelegate<Integer> delegate = mock(ItemStreamIterableReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIterableReaderAdapter.of(delegate);

		itemStreamReader.open(new ExecutionContext());

		verify(delegate, times(1)).onOpenRead(any());
		verify(delegate, times(1)).readIterable(any());
	}

	@Test
	void readShouldReturnValuesFromDelegate() throws Exception {
		List<Integer> expected = List.of(1, 2, 3);
		ItemStreamIterableReaderDelegate<Integer> delegate = mock(ItemStreamIterableReaderDelegate.class);
		when(delegate.readIterable(any())).thenAnswer($ -> expected);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIterableReaderAdapter.of(delegate);
		itemStreamReader.open(new ExecutionContext());

		List<Integer> items = new ArrayList<>();
		Integer item;
		while ((item = itemStreamReader.read()) != null) {
			items.add(item);
		}

		assertThat(items).isEqualTo(expected);
	}

	@Test
	void readShouldThrowExceptionWhenNoOpenInvoked() {
		ItemStreamIterableReaderDelegate<Integer> delegate = mock(ItemStreamIterableReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIterableReaderAdapter.of(delegate);

		assertThatThrownBy(itemStreamReader::read).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void updateShouldInvokeProperDelegateMethod() {
		ItemStreamIterableReaderDelegate<Integer> delegate = mock(ItemStreamIterableReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIterableReaderAdapter.of(delegate);

		itemStreamReader.update(new ExecutionContext());

		verify(delegate, times(1)).onUpdateRead(any());
	}

	@Test
	void closeShouldInvokeProperDelegateMethod() {
		ItemStreamIterableReaderDelegate<Integer> delegate = mock(ItemStreamIterableReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIterableReaderAdapter.of(delegate);

		itemStreamReader.close();

		verify(delegate, times(1)).onCloseRead();
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
	@Test
	void createShouldThrowExceptionWhenPassingNull() {
		assertThatThrownBy(() -> ItemStreamIterableReaderAdapter.of(null));
	}
}
