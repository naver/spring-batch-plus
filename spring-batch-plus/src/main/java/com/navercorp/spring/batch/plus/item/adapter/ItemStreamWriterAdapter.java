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

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.lang.NonNull;

/**
 * An adaptor which adapt {@link ItemStreamWriterDelegate} to {@link ItemStreamWriter}.
 *
 * @since 0.1.0
 */
public class ItemStreamWriterAdapter<T> implements ItemStreamWriter<T> {

	/**
	 * Create an adaptor which adapt {@link ItemStreamWriterDelegate} to {@link ItemStreamWriter}.
	 *
	 * @param delegate a delegate
	 * @return an adapted ItemStreamWriter
	 * @param <T> an item type to write
	 */
	public static <T> ItemStreamWriter<T> of(@NonNull ItemStreamWriterDelegate<T> delegate) {
		return new ItemStreamWriterAdapter<>(delegate);
	}

	protected final ItemStreamWriterDelegate<T> delegate;

	protected ItemStreamWriterAdapter(ItemStreamWriterDelegate<T> delegate) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate writer must not be null");
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) {
		this.delegate.onOpenWrite(executionContext);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void write(Chunk<? extends T> chunk) {
		this.delegate.write(chunk);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void update(ExecutionContext executionContext) {
		this.delegate.onUpdateWrite(executionContext);
	}

	@Override
	public void close() {
		this.delegate.onCloseWrite();
	}
}
