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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A delegate for {@link ItemProcessor}.
 *
 * @deprecated Use {@link com.navercorp.spring.batch.plus.item.adapter.ItemProcessorDelegate} instead.
 * @since 0.1.0
 */
@Deprecated
public interface ItemProcessorDelegate<I, O> {

	/**
	 * A delegate method for {@link ItemProcessor#process(Object)}.
	 *
	 * @param item an item to process
	 * @return processed item
	 */
	@Nullable
	O process(@NonNull I item);
}
