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

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;

import reactor.core.publisher.Flux;

// note: it's deprecated. Do not change it.
@SuppressWarnings("deprecation")
class AdapterFactoryTest {

	@Test
	void testItemReader() {
		// when
		ItemStreamReaderDelegate<Integer> delegate = executionContext -> Flux.empty();
		ItemStreamReader<Integer> actual = AdapterFactory.itemStreamReader(delegate);

		// then
		assertThat(actual).isInstanceOf(StepScopeItemStreamReader.class);
	}

	@Test
	void testItemProcessor() {
		// when
		ItemProcessorDelegate<Integer, Integer> delegate = item -> null;
		ItemProcessor<Integer, Integer> actual = AdapterFactory.itemProcessor(delegate);

		// then
		assertThat(actual).isInstanceOf(ItemProcessorAdapter.class);
	}

	@Test
	void testItemWriter() {
		// when
		ItemStreamWriterDelegate<Integer> delegate = items -> {
		};
		ItemStreamWriter<Integer> actual = AdapterFactory.itemStreamWriter(delegate);

		// then
		assertThat(actual).isInstanceOf(ItemStreamWriterAdapter.class);
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void testPassingNull() {
		assertThatThrownBy(() -> AdapterFactory.itemStreamReader((ItemStreamReaderDelegate<?>)null));
		assertThatThrownBy(() -> AdapterFactory.itemProcessor(null));
		assertThatThrownBy(() -> AdapterFactory.itemStreamWriter(null));
	}
}
