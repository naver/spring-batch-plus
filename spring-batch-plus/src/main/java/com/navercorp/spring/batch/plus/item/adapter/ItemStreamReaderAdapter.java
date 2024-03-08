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

import java.util.Iterator;
import java.util.Objects;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.lang.NonNull;

import reactor.core.publisher.Flux;

/**
 * An adapter which adapt {@link ItemStreamReaderDelegate} to {@link ItemStreamReader}.
 *
 * @since 0.1.0
 */
public class ItemStreamReaderAdapter<T> implements ItemStreamReader<T> {

	/**
	 * Create an adapter which adapt {@link ItemStreamReaderDelegate} to {@link ItemStreamReader}.
	 *
	 * @param delegate a delegate
	 * @return an adapted ItemStreamReader
	 * @param <T> a read item type
	 */
	public static <T> ItemStreamReader<T> of(@NonNull ItemStreamReaderDelegate<T> delegate) {
		return new ItemStreamReaderAdapter<>(delegate);
	}

	protected static final int DEFAULT_BATCH_SIZE = 1;

	protected final ItemStreamReaderDelegate<T> delegate;

	protected Flux<T> flux = null;

	protected Iterator<T> iterator = null;

	protected ItemStreamReaderAdapter(ItemStreamReaderDelegate<T> delegate) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate reader must not be null");
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) {
		this.delegate.onOpenRead(executionContext);
		this.flux = this.delegate.readFlux(executionContext);
	}

	@Override
	public T read() {
		if (this.iterator == null) {
			Objects.requireNonNull(this.flux, "Flux isn't set. Call 'open' first.");
			this.iterator = this.flux.toIterable(DEFAULT_BATCH_SIZE).iterator();
		}

		if (this.iterator.hasNext()) {
			return this.iterator.next();
		} else {
			return null;
		}
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void update(ExecutionContext executionContext) {
		this.delegate.onUpdateRead(executionContext);
	}

	@Override
	public void close() {
		this.delegate.onCloseRead();
	}
}
