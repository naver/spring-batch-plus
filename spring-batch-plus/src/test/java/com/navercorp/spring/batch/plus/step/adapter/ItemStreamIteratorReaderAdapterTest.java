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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;

/**
 * Covers the blocking reader contract over an Iterator-backed stream delegate.
 */
@SuppressWarnings("unchecked")
class ItemStreamIteratorReaderAdapterTest {

	@Test
	void openShouldInitializeFromDelegate() {
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		itemStreamReader.open(new ExecutionContext());

		verify(delegate, times(1)).onOpenRead(any());
		verify(delegate, times(1)).readIterator(any());
	}

	@Test
	void readShouldReturnAllSourceItems() throws Exception {
		List<Integer> expected = List.of(1, 2, 3);
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		when(delegate.readIterator(any())).thenAnswer($ -> expected.iterator());
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);
		itemStreamReader.open(new ExecutionContext());

		List<Integer> items = new ArrayList<>();
		Integer item;
		while ((item = itemStreamReader.read()) != null) {
			items.add(item);
		}

		assertThat(items).isEqualTo(expected);
	}

	@Test
	void readShouldFailWhenAdapterIsNotOpened() {
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		assertThatThrownBy(itemStreamReader::read).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void updateShouldForwardExecutionContextToDelegate() {
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		itemStreamReader.update(new ExecutionContext());

		verify(delegate, times(1)).onUpdateRead(any());
	}

	@Test
	void closeShouldNotifyDelegate() {
		ItemStreamIteratorReaderDelegate<Integer> delegate = mock(ItemStreamIteratorReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);

		itemStreamReader.close();

		verify(delegate, times(1)).onCloseRead();
	}

	@Test
	void lifecycleShouldBeOptional() {
		ItemStreamIteratorReaderDelegate<Integer> delegate = $ -> List.<Integer>of().iterator();
		ItemStreamReader<Integer> itemStreamReader = ItemStreamIteratorReaderAdapter.of(delegate);
		ExecutionContext executionContext = new ExecutionContext();

		assertThatCode(() -> {
			itemStreamReader.open(executionContext);
			itemStreamReader.update(executionContext);
			itemStreamReader.close();
		}).doesNotThrowAnyException();
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void ofShouldRejectNullDelegate() {
		assertThatThrownBy(() -> ItemStreamIteratorReaderAdapter.of(null));
	}
}
