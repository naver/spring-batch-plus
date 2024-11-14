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

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import reactor.core.publisher.Flux;

/**
 * A {@link Flux<I>} based adapter for stream reader, processor. It can represent
 * {@link ItemStreamReader}, {@link ItemProcessor} in a single class.
 *
 * @since 1.2.0
 */
public interface ItemStreamFluxReaderProcessor<I, O>
	extends ItemStreamFluxReaderDelegate<I>, ItemProcessorDelegate<I, O> {
}
