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

import com.navercorp.spring.batch.plus.item.adapter.ItemProcessorAdapter
import com.navercorp.spring.batch.plus.item.adapter.ItemProcessorDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamFluxReaderDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamIterableReaderDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamIteratorReaderDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamReaderProcessorWriter
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamSimpleReaderDelegate
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamWriterAdapter
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamWriterDelegate
import com.navercorp.spring.batch.plus.item.adapter.StepScopeItemStreamReader
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import reactor.core.publisher.Flux

internal class ItemDelegatesTest {

    @Test
    fun testAsItemStreamReaderOnItemStreamFluxReaderDelegate() {
        // when
        val delegate = mockk<ItemStreamFluxReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        // then
        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun testAsItemStreamReaderOnItemStreamIterableReaderDelegate() {
        // when
        val delegate = mockk<ItemStreamIterableReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        // then
        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun testAsItemStreamReaderOnItemStreamIteratorReaderDelegate() {
        // when
        val delegate = mockk<ItemStreamIteratorReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        // then
        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun testAsItemStreamReaderOnItemStreamSimpleReaderDelegate() {
        // when
        val delegate = mockk<ItemStreamSimpleReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        // then
        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun testAsItemProcessor() {
        // when
        val delegate = mockk<ItemProcessorDelegate<*, *>>()
        val itemProcessor = delegate.asItemProcessor()

        // then
        assertThat(itemProcessor).isInstanceOf(ItemProcessorAdapter::class.java)
    }

    @Test
    fun testAsItemStreamWriter() {
        // when
        val delegate = mockk<ItemStreamWriterDelegate<*>>()
        val itemStreamWriter = delegate.asItemStreamWriter()

        // then
        assertThat(itemStreamWriter).isInstanceOf(ItemStreamWriterAdapter::class.java)
    }

    // TODO: delete after ItemStreamReaderDelegate is removed
    @Test
    fun testExtensions() {
        // when
        val testClass = TestClass()
        val itemStreamReader = testClass.asItemStreamReader()
        val itemProcessor = testClass.asItemProcessor()
        val itemStreamWriter = testClass.asItemStreamWriter()

        // then
        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
        assertThat(itemProcessor).isInstanceOf(ItemProcessorAdapter::class.java)
        assertThat(itemStreamWriter).isInstanceOf(ItemStreamWriterAdapter::class.java)
    }

    internal open class TestClass : ItemStreamReaderProcessorWriter<Int, String> {

        override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
            return Flux.empty()
        }

        override fun process(item: Int): String {
            return item.toString()
        }

        override fun write(chunk: Chunk<out String>) {
        }
    }
}
