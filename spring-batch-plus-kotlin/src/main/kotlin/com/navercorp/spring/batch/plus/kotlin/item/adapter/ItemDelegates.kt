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

package com.navercorp.spring.batch.plus.kotlin.item.adapter

import com.navercorp.spring.batch.plus.item.adapter.AdapterFactory
import com.navercorp.spring.batch.plus.item.adapter.ItemProcessorDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamReaderDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamWriterDelegate
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ItemStreamWriter

/**
 * An extensions to invoke [AdapterFactory.itemStreamReader].
 */
@Deprecated(
    message = "Uses ItemStreamFluxReaderDelegate instead",
    replaceWith = ReplaceWith(""),
)
fun <T : Any> ItemStreamReaderDelegate<T>.asItemStreamReader(): ItemStreamReader<T> =
    AdapterFactory.itemStreamReader(this)

/**
 * An extensions to invoke [AdapterFactory.itemProcessor].
 */
@Deprecated(
    message = "Uses com.navercorp.spring.batch.plus.step.adapter.ItemProcessorDelegate instead",
    replaceWith = ReplaceWith(""),
)
fun <I : Any, O : Any> ItemProcessorDelegate<I, O>.asItemProcessor(): ItemProcessor<I, O> =
    AdapterFactory.itemProcessor(this)

/**
 * An extensions to invoke [AdapterFactory.itemStreamWriter].
 */
@Deprecated(
    message = "Uses com.navercorp.spring.batch.plus.step.adapter.ItemStreamWriterDelegate instead",
    replaceWith = ReplaceWith(
        "AdapterFactory.itemStreamWriter(this)",
        "com.navercorp.spring.batch.plus.step.adapter.AdapterFactory",
    ),
)
fun <T : Any> ItemStreamWriterDelegate<T>.asItemStreamWriter(): ItemStreamWriter<T> =
    AdapterFactory.itemStreamWriter(this)
