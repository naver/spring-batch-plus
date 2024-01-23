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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import reactor.core.publisher.Flux;

@SuppressWarnings("unchecked")
class ItemStreamReaderAdapterTest {


	@Test
	void testOpen() {
		// given
		ItemStreamReaderDelegate<Integer> itemStreamReaderDelegate = mock(ItemStreamReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReaderAdaptor = ItemStreamReaderAdapter.of(itemStreamReaderDelegate);

		// when
		itemStreamReaderAdaptor.open(new ExecutionContext());

		// then
		verify(itemStreamReaderDelegate, times(1)).onOpenRead(any());
		verify(itemStreamReaderDelegate, times(1)).readFlux(any());
	}

	@Test
	void testRead() throws Exception {
		// given
		List<Integer> expected = List.of(1, 2, 3);
		ItemStreamReaderDelegate<Integer> itemStreamReaderDelegate = mock(ItemStreamReaderDelegate.class);
		when(itemStreamReaderDelegate.readFlux(any())).thenReturn(Flux.fromIterable(expected));
		ItemStreamReader<Integer> itemStreamReaderAdaptor = ItemStreamReaderAdapter.of(itemStreamReaderDelegate);

		// when
		itemStreamReaderAdaptor.open(new ExecutionContext());
		List<Integer> items = new ArrayList<>();
		Integer item;
		while ((item = itemStreamReaderAdaptor.read()) != null) {
			items.add(item);
		}

		// then
		assertThat(items).isEqualTo(expected);
	}

	@Test
	void testReadWithOpenShouldThrowsException() {
		// given
		ItemStreamReaderDelegate<Integer> itemStreamReaderDelegate = mock(ItemStreamReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReaderAdaptor = ItemStreamReaderAdapter.of(itemStreamReaderDelegate);

		// when, then
		assertThatThrownBy(itemStreamReaderAdaptor::read)
			.hasMessageContaining("Flux isn't set. Call 'open' first.");
	}

	@Test
	void testUpdate() {
		// given
		ItemStreamReaderDelegate<Integer> itemStreamReaderDelegate = mock(ItemStreamReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReaderAdaptor = ItemStreamReaderAdapter.of(itemStreamReaderDelegate);

		// when
		itemStreamReaderAdaptor.update(new ExecutionContext());

		// then
		verify(itemStreamReaderDelegate, times(1)).onUpdateRead(any());
	}

	@Test
	void testClose() {
		// given
		ItemStreamReaderDelegate<Integer> itemStreamReaderDelegate = mock(ItemStreamReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReaderAdaptor = ItemStreamReaderAdapter.of(itemStreamReaderDelegate);

		// when
		itemStreamReaderAdaptor.close();

		// then
		verify(itemStreamReaderDelegate, times(1)).onCloseRead();
	}

	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ItemStreamReaderAdapter.of(null));
	}
}
