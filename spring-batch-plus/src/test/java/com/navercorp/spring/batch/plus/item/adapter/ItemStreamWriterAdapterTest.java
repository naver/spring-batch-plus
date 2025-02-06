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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;

// note: it's deprecated. Do not change it.
@SuppressWarnings({"unchecked", "deprecation"})
class ItemStreamWriterAdapterTest {

	@Test
	void testOpen() {
		// given
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		// when
		itemStreamWriterAdaptor.open(new ExecutionContext());

		// then
		verify(delegate, times(1)).onOpenWrite(any());
	}

	@Test
	void testWrite() throws Exception {
		// given
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		// when
		itemStreamWriterAdaptor.write(Chunk.of());

		// then
		verify(delegate, times(1)).write(any());
	}

	@Test
	void testUpdate() {
		// given
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		// when
		itemStreamWriterAdaptor.update(new ExecutionContext());

		// then
		verify(delegate, times(1)).onUpdateWrite(any());
	}

	@Test
	void testClose() {
		// given
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		// when
		itemStreamWriterAdaptor.close();

		// then
		verify(delegate, times(1)).onCloseWrite();
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ItemStreamWriterAdapter.of(null));
	}
}
