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

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;

/**
 * An adaptor which adapt {@link ItemStreamWriterDelegate} to {@link ItemStreamWriter}.
 *
 * @since 0.1.0
 */
public class ItemStreamWriterAdaptor<T> implements ItemStreamWriter<T> {

	/**
	 * Create an adaptor which adapt {@link ItemStreamWriterDelegate} to {@link ItemStreamWriter}.
	 *
	 * @param delegate a delegate
	 * @return an adapted ItemStreamWriter
	 * @param <T> an item type to write
	 */
	public static <T> ItemStreamWriter<T> of(ItemStreamWriterDelegate<T> delegate) {
		return new ItemStreamWriterAdaptor<>(delegate);
	}

	private final ItemStreamWriterDelegate<T> delegate;

	private ItemStreamWriterAdaptor(ItemStreamWriterDelegate<T> delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) {
		this.delegate.onOpenWrite(executionContext);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void write(List<? extends T> items) {
		this.delegate.write(items);
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
