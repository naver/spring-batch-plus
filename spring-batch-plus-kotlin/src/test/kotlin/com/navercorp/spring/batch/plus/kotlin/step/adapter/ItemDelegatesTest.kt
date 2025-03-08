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

import com.navercorp.spring.batch.plus.step.adapter.ItemProcessorAdapter
import com.navercorp.spring.batch.plus.step.adapter.ItemProcessorDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamFluxReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIteratorReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamSimpleReaderDelegate
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamWriterAdapter
import com.navercorp.spring.batch.plus.step.adapter.ItemStreamWriterDelegate
import com.navercorp.spring.batch.plus.step.adapter.StepScopeItemStreamReader
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ItemDelegatesTest {

    @Test
    fun asItemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithFluxDelegate() {
        val delegate = mockk<ItemStreamFluxReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun asItemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithIterableDelegate() {
        val delegate = mockk<ItemStreamIterableReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun asItemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithIteratorDelegate() {
        val delegate = mockk<ItemStreamIteratorReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun asItemStreamReaderShouldReturnStepScopedOneWhenPassingItemReaderWithSimpleDelegate() {
        val delegate = mockk<ItemStreamSimpleReaderDelegate<*>>()
        val itemStreamReader = delegate.asItemStreamReader()

        assertThat(itemStreamReader).isInstanceOf(StepScopeItemStreamReader::class.java)
    }

    @Test
    fun asItemProcessorShouldReturnAdapterWhenPassingProcessorDelegate() {
        val delegate = mockk<ItemProcessorDelegate<*, *>>()
        val itemProcessor = delegate.asItemProcessor()

        assertThat(itemProcessor).isInstanceOf(ItemProcessorAdapter::class.java)
    }

    @Test
    fun asItemStreamWriterShouldReturnAdapterWhenPassingWriterDelegate() {
        val delegate = mockk<ItemStreamWriterDelegate<*>>()
        val itemStreamWriter = delegate.asItemStreamWriter()

        assertThat(itemStreamWriter).isInstanceOf(ItemStreamWriterAdapter::class.java)
    }
}
