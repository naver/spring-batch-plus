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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;

@SuppressWarnings("unchecked")
class ItemStreamWriterAdapterTest {

	@Test
	void openShouldInvokeProperDelegateMethod() {
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		itemStreamWriterAdaptor.open(new ExecutionContext());

		verify(delegate, times(1)).onOpenWrite(any());
	}

	@Test
	void writeShouldInvokeProperDelegateMethod() throws Exception {
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		itemStreamWriterAdaptor.write(Chunk.of());

		verify(delegate, times(1)).write(any());
	}

	@Test
	void updateShouldInvokeProperDelegateMethod() {
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		itemStreamWriterAdaptor.update(new ExecutionContext());

		verify(delegate, times(1)).onUpdateWrite(any());
	}

	@Test
	void closeShouldInvokeProperDelegateMethod() {
		ItemStreamWriterDelegate<Integer> delegate = mock(ItemStreamWriterDelegate.class);
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(delegate);

		itemStreamWriterAdaptor.close();

		verify(delegate, times(1)).onCloseWrite();
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void createShouldThrowExceptionWhenPassingNull() {
		assertThatThrownBy(() -> ItemStreamWriterAdapter.of(null));
	}
}
