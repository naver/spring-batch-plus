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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

@SuppressWarnings("unchecked")
class ItemStreamIteratorReaderAdapterTest {

	@Test
	void testOpen() {
		// given
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		// when
		itemStreamReader.open(new ExecutionContext());

		// then
		verify(delegate, times(1)).onOpenRead(any());
		verify(delegate, times(1)).readIterator(any());
	}

	@Test
	void testRead() throws Exception {
		// given
		List<Integer> expected = List.of(1, 2, 3);
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		when(delegate.readIterator(any())).thenAnswer($ -> expected.iterator());
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		// when
		itemStreamReader.open(new ExecutionContext());
		List<Integer> items = new ArrayList<>();
		Integer item;
		while ((item = itemStreamReader.read()) != null) {
			items.add(item);
		}

		// then
		assertThat(items).isEqualTo(expected);
	}

	@Test
	void testReadWithOpenShouldThrowsException() {
		// given
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		// when, then
		assertThatThrownBy(itemStreamReader::read).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void testUpdate() {
		// given
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		// when
		itemStreamReader.update(new ExecutionContext());

		// then
		verify(delegate, times(1)).onUpdateRead(any());
	}

	@Test
	void testClose() {
		// given
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		// when
		itemStreamReader.close();

		// then
		verify(delegate, times(1)).onCloseRead();
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ItemStreamIteratorReaderAdapter.of(null));
	}
}
