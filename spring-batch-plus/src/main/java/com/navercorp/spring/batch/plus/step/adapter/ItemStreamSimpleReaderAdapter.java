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

import java.util.Objects;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.lang.NonNull;

/**
 * An adapter which adapt {@link ItemStreamSimpleReaderDelegate} to {@link ItemStreamReader}.
 *
 * @since 1.1.0
 */
public class ItemStreamSimpleReaderAdapter<T> implements ItemStreamReader<T> {

	/**
	 * Create an adapter which adapt {@link ItemStreamSimpleReaderDelegate} to {@link ItemStreamReader}.
	 *
	 * @param delegate a delegate
	 * @return an adapted ItemStreamReader
	 * @param <T> a read item type
	 */
	public static <T> ItemStreamReader<T> of(@NonNull ItemStreamSimpleReaderDelegate<T> delegate) {
		return new ItemStreamSimpleReaderAdapter<>(delegate);
	}

	protected final ItemStreamSimpleReaderDelegate<T> delegate;

	protected ItemStreamSimpleReaderAdapter(ItemStreamSimpleReaderDelegate<T> delegate) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate reader must not be null");
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) {
		this.delegate.onOpenRead(executionContext);
	}

	@Override
	public T read() {
		return this.delegate.read();
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
