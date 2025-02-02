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

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

@SuppressWarnings("unchecked")
class ItemStreamSimpleReaderAdapterTest {

	@Test
	void openShouldInvokeProperDelegateMethod() {
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		itemStreamReader.open(new ExecutionContext());

		verify(delegate, times(1)).onOpenRead(any());
	}

	@Test
	void readShouldReturnValuesFromDelegate() throws Exception {
		Integer expected = ThreadLocalRandom.current().nextInt();
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		when(delegate.read()).thenReturn(expected);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		Integer actual = itemStreamReader.read();

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void updateShouldInvokeProperDelegateMethod() {
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		itemStreamReader.update(new ExecutionContext());

		verify(delegate, times(1)).onUpdateRead(any());
	}

	@Test
	void closeShouldInvokeProperDelegateMethod() {
		ItemStreamSimpleReaderDelegate<Integer> delegate = mock(ItemStreamSimpleReaderDelegate.class);
		ItemStreamReader<Integer> itemStreamReader = ItemStreamSimpleReaderAdapter.of(delegate);

		itemStreamReader.close();

		verify(delegate, times(1)).onCloseRead();
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void createShouldThrowExceptionWhenPassingNull() {
		assertThatThrownBy(() -> ItemStreamSimpleReaderAdapter.of(null));
	}
}
