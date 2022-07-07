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

import org.springframework.batch.item.ItemProcessor;

class ItemProcessorAdaptor<I, O> implements ItemProcessor<I, O> {

	static <I, O> ItemProcessor<I, O> withDelegate(ItemProcessorDelegate<I, O> delegate) {
		return new ItemProcessorAdaptor<>(delegate);
	}

	private final ItemProcessorDelegate<I, O> delegate;

	private ItemProcessorAdaptor(ItemProcessorDelegate<I, O> delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public O process(I item) {
		return this.delegate.process(item);
	}
}
