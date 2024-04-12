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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
class ItemStreamSimpleReaderAdapterTest {

	@Test
	void testOpen() {
		// given
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		// when
		itemStreamReader.open(new ExecutionContext());

		// then
		verify(delegate, times(1)).onOpenRead(any());
	}

	@Test
	void testRead() throws Exception {
		// given
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		when(delegate.read()).thenReturn(1, 2, 3, null);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		// when
		itemStreamReader.open(new ExecutionContext());
		List<Integer> items = new ArrayList<>();
		Integer item;
		while ((item = itemStreamReader.read()) != null) {
			items.add(item);
		}

		// then
		assertThat(items).isEqualTo(List.of(1, 2, 3));
	}

	@Test
	void testUpdate() {
		// given
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		// when
		itemStreamReader.update(new ExecutionContext());

		// then
		verify(delegate, times(1)).onUpdateRead(any());
	}

	@Test
	void testClose() {
		// given
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		// when
		itemStreamReader.close();

		// then
		verify(delegate, times(1)).onCloseRead();
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ItemStreamSimpleReaderAdapter.of(null));
	}
}
