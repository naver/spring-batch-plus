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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;

import reactor.core.publisher.Flux;

class AdapterFactoryTest {

	@Test
	void itemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithFluxDelegate() {
		ItemStreamFluxReaderDelegate<Integer> delegate = executionContext -> Flux.empty();
		ItemStreamReader<Integer> actual = AdapterFactory.itemStreamReader(delegate);

		assertThat(actual).isInstanceOf(StepScopeItemStreamReader.class);
	}

	@Test
	void itemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithIterableDelegate() {
		ItemStreamIterableReaderDelegate<Integer> delegate = executionContext -> List.of();
		ItemStreamReader<Integer> actual = AdapterFactory.itemStreamReader(delegate);

		assertThat(actual).isInstanceOf(StepScopeItemStreamReader.class);
	}

	@Test
	void itemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithIteratorDelegate() {
		ItemStreamIteratorReaderDelegate<Integer> delegate = executionContext -> Collections.emptyIterator();
		ItemStreamReader<Integer> actual = AdapterFactory.itemStreamReader(delegate);

		assertThat(actual).isInstanceOf(StepScopeItemStreamReader.class);
	}

	@Test
	void itemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithSimpleDelegate() {
		ItemStreamSimpleReaderDelegate<Integer> delegate = () -> null;
		ItemStreamReader<Integer> actual = AdapterFactory.itemStreamReader(delegate);

		assertThat(actual).isInstanceOf(StepScopeItemStreamReader.class);
	}

	@Test
	void itemProcessorShouldReturnAdapterWhenPassingProcessorDelegate() {
		ItemProcessorDelegate<Integer, Integer> delegate = item -> null;
		ItemProcessor<Integer, Integer> actual = AdapterFactory.itemProcessor(delegate);

		assertThat(actual).isInstanceOf(ItemProcessorAdapter.class);
	}

	@Test
	void itemStreamWriterShouldReturnAdapterWhenPassingWriterDelegate() {
		ItemStreamWriterDelegate<Integer> delegate = items -> {
		};
		ItemStreamWriter<Integer> actual = AdapterFactory.itemStreamWriter(delegate);

		assertThat(actual).isInstanceOf(ItemStreamWriterAdapter.class);
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void createShouldThrowExceptionWhenPassingNull() {
		assertThatThrownBy(() -> AdapterFactory.itemStreamReader((ItemStreamFluxReaderDelegate<?>)null));
		assertThatThrownBy(() -> AdapterFactory.itemStreamReader((ItemStreamIterableReaderDelegate<?>)null));
		assertThatThrownBy(() -> AdapterFactory.itemStreamReader((ItemStreamIteratorReaderDelegate<?>)null));
		assertThatThrownBy(() -> AdapterFactory.itemStreamReader((ItemStreamSimpleReaderDelegate<?>)null));
		assertThatThrownBy(() -> AdapterFactory.itemProcessor(null));
		assertThatThrownBy(() -> AdapterFactory.itemStreamWriter(null));
	}
}
