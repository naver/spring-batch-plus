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

package com.navercorp.spring.batch.plus.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;

import reactor.core.publisher.Flux;

class AdaptorFactoryTest {

	@Test
	void testItemReader() {
		// when
		ItemStreamReaderDelegate<Integer> itemStreamReaderDelegate = executionContext -> Flux.empty();
		ItemStreamReader<Integer> actual = AdaptorFactory.itemStreamReader(itemStreamReaderDelegate);

		// then
		assertThat(actual).isInstanceOf(StepScopeItemStreamReader.class);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	void testItemReaderThrowExceptionWhenPassNull() {
		assertThatThrownBy(
			() -> AdaptorFactory.itemStreamReader(null)
		).hasMessageContaining("ItemStreamReader delegate is null");
	}

	@Test
	void testItemProcessor() {
		// when
		ItemProcessorDelegate<Integer, Integer> itemProcessorDelegate = item -> null;
		ItemProcessor<Integer, Integer> actual = AdaptorFactory.itemProcessor(itemProcessorDelegate);

		// then
		assertThat(actual).isInstanceOf(ItemProcessorAdaptor.class);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	void testItemProcessorThrowExceptionWhenPassNull() {
		assertThatThrownBy(
			() -> AdaptorFactory.itemProcessor(null)
		).hasMessageContaining("ItemProcessor delegate is null");
	}

	@Test
	void testItemWriter() {
		// when
		ItemStreamWriterDelegate<Integer> itemStreamWriterDelegate = items -> {
		};
		ItemStreamWriter<Integer> actual = AdaptorFactory.itemStreamWriter(itemStreamWriterDelegate);

		// then
		assertThat(actual).isInstanceOf(ItemStreamWriterAdaptor.class);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	void testItemWriterThrowExceptionWhenPassNull() {
		assertThatThrownBy(
			() -> AdaptorFactory.itemStreamWriter(null)
		).hasMessageContaining("ItemStreamWriter delegate is null");
	}
}
