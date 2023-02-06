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
import org.springframework.lang.NonNull;

/**
 * A delegate for {@link ItemStreamWriter}.
 *
 * @deprecated Use {@link com.navercorp.spring.batch.plus.item.adapter.ItemStreamWriterDelegate} instead.
 * @since 0.1.0
 */
@Deprecated
public interface ItemStreamWriterDelegate<T> {

	/**
	 * A delegate method for {@link ItemStreamWriter#open(ExecutionContext)}.
	 * @param executionContext an execution context
	 */
	default void onOpenWrite(@NonNull ExecutionContext executionContext) {
	}

	/**
	 * A delegate method for {@link ItemStreamWriter#write(List)}.
	 * @param items items to write
	 */
	void write(@NonNull List<? extends T> items);

	/**
	 * A delegate method for {@link ItemStreamWriter#update(ExecutionContext)}.
	 * @param executionContext an execution context
	 */
	default void onUpdateWrite(@NonNull ExecutionContext executionContext) {
	}

	/**
	 * A delegate method for {@link ItemStreamWriter#close()}.
	 */
	default void onCloseWrite() {
	}
}
