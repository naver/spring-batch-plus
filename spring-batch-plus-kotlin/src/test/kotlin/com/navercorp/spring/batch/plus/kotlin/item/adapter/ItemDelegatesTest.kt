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

@file:Suppress("DEPRECATION")

package com.navercorp.spring.batch.plus.kotlin.item.adapter

import com.navercorp.spring.batch.plus.item.adapter.ItemProcessorAdapter
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamReaderProcessorWriter
import com.navercorp.spring.batch.plus.item.adapter.ItemStreamWriterAdapter
import com.navercorp.spring.batch.plus.item.adapter.StepScopeItemStreamReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import reactor.core.publisher.Flux

internal class ItemDelegatesTest {

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
