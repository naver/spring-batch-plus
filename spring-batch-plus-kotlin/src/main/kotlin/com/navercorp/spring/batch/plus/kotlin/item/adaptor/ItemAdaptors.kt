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

package com.navercorp.spring.batch.plus.kotlin.item.adaptor

import com.navercorp.spring.batch.plus.item.adaptor.AdaptorFactory
import com.navercorp.spring.batch.plus.item.adaptor.ItemProcessorDelegate
import com.navercorp.spring.batch.plus.item.adaptor.ItemStreamReaderDelegate
import com.navercorp.spring.batch.plus.item.adaptor.ItemStreamWriterDelegate
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ItemStreamWriter

/**
 * A extensions to invoke [AdaptorFactory.itemStreamReader].
 */
fun <T : Any> ItemStreamReaderDelegate<T>.asItemStreamReader(): ItemStreamReader<T> =
    AdaptorFactory.itemStreamReader(this)

/**
 * A extensions to invoke [AdaptorFactory.itemProcessor].
 */
fun <I : Any, O : Any> ItemProcessorDelegate<I, O>.asItemProcessor(): ItemProcessor<I, O> =
    AdaptorFactory.itemProcessor(this)

/**
 * A extensions to invoke [AdaptorFactory.itemStreamWriter].
 */
fun <T : Any> ItemStreamWriterDelegate<T>.asItemStreamWriter(): ItemStreamWriter<T> =
    AdaptorFactory.itemStreamWriter(this)
