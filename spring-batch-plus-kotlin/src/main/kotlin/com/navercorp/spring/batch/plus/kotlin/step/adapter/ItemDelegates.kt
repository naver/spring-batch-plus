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

package com.navercorp.spring.batch.plus.kotlin.step.adapter

import com.navercorp.spring.batch.plus.step.adapter.AdapterFactory
import com.navercorp.spring.batch.plus.step.adapter.ItemProcessorDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamFluxReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIteratorReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamSimpleReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamWriterDelegate
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ItemStreamWriter

/**
 * An extensions to invoke [AdapterFactory.itemStreamReader].
 */
fun <T : Any> ItemStreamFluxReaderDelegate<T>.asItemStreamReader(): ItemStreamReader<T> =
    AdapterFactory.itemStreamReader(this)

/**
 * An extensions to invoke [AdapterFactory.itemStreamReader].
 */
fun <T : Any> ItemStreamIterableReaderDelegate<T>.asItemStreamReader(): ItemStreamReader<T> =
    AdapterFactory.itemStreamReader(this)

/**
 * An extensions to invoke [AdapterFactory.itemStreamReader].
 */
fun <T : Any> ItemStreamIteratorReaderDelegate<T>.asItemStreamReader(): ItemStreamReader<T> =
    AdapterFactory.itemStreamReader(this)

/**
 * An extensions to invoke [AdapterFactory.itemStreamReader].
 */
fun <T : Any> ItemStreamSimpleReaderDelegate<T>.asItemStreamReader(): ItemStreamReader<T> =
    AdapterFactory.itemStreamReader(this)

/**
 * An extensions to invoke [AdapterFactory.itemProcessor].
 */
fun <I : Any, O : Any> ItemProcessorDelegate<I, O>.asItemProcessor(): ItemProcessor<I, O> =
    AdapterFactory.itemProcessor(this)

/**
 * An extensions to invoke [AdapterFactory.itemStreamWriter].
 */
fun <T : Any> ItemStreamWriterDelegate<T>.asItemStreamWriter(): ItemStreamWriter<T> =
    AdapterFactory.itemStreamWriter(this)
