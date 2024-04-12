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

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

/**
 * An adapter which adapt {@link ItemProcessorDelegate} to {@link ItemProcessor}.
 *
 * @since 0.1.0
 * @deprecated use {@link com.navercorp.spring.batch.plus.step.adapter.ItemProcessorAdapter} instead.
 */
@Deprecated
public class ItemProcessorAdapter<I, O> implements ItemProcessor<I, O> {

	/**
	 * Create an adapter which adapt {@link ItemProcessorDelegate} to {@link ItemProcessor}.
	 *
	 * @param delegate a delegate
	 * @return an adapted ItemProcessor
	 * @param <I> an item type to process
	 * @param <O> a processed item type
	 */
	public static <I, O> ItemProcessor<I, O> of(@NonNull ItemProcessorDelegate<I, O> delegate) {
		return new ItemProcessorAdapter<>(delegate);
	}

	protected final ItemProcessorDelegate<I, O> delegate;

	protected ItemProcessorAdapter(ItemProcessorDelegate<I, O> delegate) {
		this.delegate = Objects.requireNonNull(delegate, "Delegate processor must not be null");
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public O process(I item) {
		return this.delegate.process(item);
	}
}
