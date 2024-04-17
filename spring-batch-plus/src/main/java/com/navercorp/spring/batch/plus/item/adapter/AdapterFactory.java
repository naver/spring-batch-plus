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

import java.util.Objects;

import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.lang.NonNull;

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamFluxReaderDelegate;

/**
 * An adapter factory for {@link ItemStreamReaderDelegate}, {@link ItemProcessorDelegate},
 * {@link ItemStreamWriterDelegate}.
 *
 * @since 0.1.0
 * @deprecated use {@link com.navercorp.spring.batch.plus.step.adapter.AdapterFactory} instead.
 */
@Deprecated
public final class AdapterFactory {

	/**
	 * Create an adapter which adapt {@link ItemStreamReaderDelegate} to {@link ItemStreamReader}
	 * with {@link StepScope} bound proxy implementation. It creates new instance for every {@link StepScope}.
	 *
	 * @deprecated use {@link com.navercorp.spring.batch.plus.step.adapter.AdapterFactory#itemStreamReader(
	 *ItemStreamFluxReaderDelegate)} instead.
	 *
	 * @param <T> a read item type
	 * @param delegate a delegate
	 * @return an adapted ItemStreamReader
	 */
	@Deprecated
	public static <T> ItemStreamReader<T> itemStreamReader(@NonNull ItemStreamReaderDelegate<T> delegate) {
		Objects.requireNonNull(delegate, "ItemStreamReader delegate is null");
		return StepScopeItemStreamReader.of(() -> ItemStreamReaderAdapter.of(delegate));
	}

	/**
	 * Create an adapter which adapt {@link ItemProcessorDelegate} to {@link ItemProcessor}.
	 *
	 * @deprecated use
	 * {@link com.navercorp.spring.batch.plus.step.adapter.AdapterFactory#itemProcessor(
	 *com.navercorp.spring.batch.plus.step.adapter.ItemProcessorDelegate)} instead.
	 *
	 * @param <I> an item type to process
	 * @param <O> a processed item type
	 * @param delegate a delegate
	 * @return an adapted ItemProcessor
	 */
	@Deprecated
	public static <I, O> ItemProcessor<I, O> itemProcessor(@NonNull ItemProcessorDelegate<I, O> delegate) {
		return ItemProcessorAdapter.of(delegate);
	}

	/**
	 * Create an adapter which adapt {@link ItemStreamWriterDelegate} to {@link ItemStreamWriter}.
	 *
	 * @deprecated use {@link com.navercorp.spring.batch.plus.step.adapter.AdapterFactory#itemStreamWriter(
	 *com.navercorp.spring.batch.plus.step.adapter.ItemStreamWriterDelegate)} instead.
	 *
	 * @param <T> an item type to write
	 * @param delegate a delegate
	 * @return an adapted ItemStreamWriter
	 */
	@Deprecated
	public static <T> ItemStreamWriter<T> itemStreamWriter(@NonNull ItemStreamWriterDelegate<T> delegate) {
		return ItemStreamWriterAdapter.of(delegate);
	}

	private AdapterFactory() {
	}
}
