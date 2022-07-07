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

import java.util.Iterator;
import java.util.Objects;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

import reactor.core.publisher.Flux;

class ItemStreamReaderAdaptor<I> implements ItemStreamReader<I> {

	static <I> ItemStreamReader<I> withDelegate(ItemStreamReaderDelegate<I> delegate) {
		return new ItemStreamReaderAdaptor<>(delegate);
	}

	private static final int DEFAULT_BATCH_SIZE = 1;

	private final ItemStreamReaderDelegate<I> delegate;

	private Flux<I> flux = null;

	private Iterator<I> iterator = null;

	private ItemStreamReaderAdaptor(ItemStreamReaderDelegate<I> delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) {
		this.delegate.onOpenRead(executionContext);
		this.flux = this.delegate.readFlux(executionContext);
	}

	@Override
	public I read() {
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
