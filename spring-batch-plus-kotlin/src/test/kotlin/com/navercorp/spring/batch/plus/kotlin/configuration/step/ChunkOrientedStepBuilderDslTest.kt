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

package com.navercorp.spring.batch.plus.kotlin.configuration.step

import io.micrometer.observation.ObservationRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.core.listener.StepListener
import org.springframework.batch.core.step.StepInterruptionPolicy
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder
import org.springframework.batch.core.step.item.ChunkOrientedStep
import org.springframework.batch.core.step.skip.SkipPolicy
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemStream
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.core.retry.RetryListener
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.interceptor.TransactionAttribute
import java.util.concurrent.ThreadLocalRandom

/**
 * Unit tests for ChunkOrientedStepBuilderDsl's delegation to Spring Batch's ChunkOrientedStepBuilder.
 */
internal class ChunkOrientedStepBuilderDslTest {
    @Test
    fun testReader() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemReader = mockk<ItemReader<Int>>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                reader(itemReader)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.reader(itemReader) }
    }

    @Test
    fun testProcessor() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemProcessor = mockk<ItemProcessor<Int, Int>>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                processor(itemProcessor)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.processor(itemProcessor) }
    }

    @Test
    fun testWriter() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemWriter = mockk<ItemWriter<Int>>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                writer(itemWriter)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.writer(itemWriter) }
    }

    @Test
    fun testTransactionManager() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val transactionManager = mockk<PlatformTransactionManager>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                transactionManager(transactionManager)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.transactionManager(transactionManager) }
    }

    @Test
    fun testTransactionAttribute() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val transactionAttribute = mockk<TransactionAttribute>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                transactionAttribute(transactionAttribute)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.transactionAttribute(transactionAttribute) }
    }

    @Test
    fun testStream() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemStream = mockk<ItemStream>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                stream(itemStream)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.stream(itemStream) }
    }

    @Test
    fun testStepListener() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val stepListener = mockk<StepListener>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                listener(stepListener)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.listener(stepListener) }
    }

    @Test
    fun testObjectListener() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        class TestListener

        // when
        val testListener = TestListener()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                listener(testListener)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.listener(testListener) }
    }

    @Test
    fun testInterruptionPolicy() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val interruptionPolicy = mockk<StepInterruptionPolicy>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                interruptionPolicy(interruptionPolicy)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.interruptionPolicy(interruptionPolicy) }
    }

    @Test
    fun testFaultTolerant() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                faultTolerant()
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.faultTolerant() }
    }

    @Test
    fun testRetryPolicy() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val retryPolicy = mockk<RetryPolicy>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                retryPolicy(retryPolicy)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.retryPolicy(retryPolicy) }
    }

    @Test
    fun testRetryListener() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val retryListener = mockk<RetryListener>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                retryListener(retryListener)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.retryListener(retryListener) }
    }

    @Test
    fun testRetryWithReified() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                retry<RuntimeException>()
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.retry(RuntimeException::class.java) }
    }

    @Test
    fun testRetryWithKClass() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                retry(RuntimeException::class)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.retry(RuntimeException::class.java) }
    }

    @Test
    fun testRetryLimit() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val retryLimit = ThreadLocalRandom.current().nextLong()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                retryLimit(retryLimit)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.retryLimit(retryLimit) }
    }

    @Test
    fun testSkipPolicy() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val skipPolicy = mockk<SkipPolicy>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                skipPolicy(skipPolicy)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.skipPolicy(skipPolicy) }
    }

    @Test
    fun testSkipListener() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val skipListener = mockk<SkipListener<Int, Int>>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                skipListener(skipListener)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.skipListener(skipListener) }
    }

    @Test
    fun testSkipWithReified() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                skip<RuntimeException>()
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.skip(RuntimeException::class.java) }
    }

    @Test
    fun testSkipWithKClass() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                skip(RuntimeException::class)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.skip(RuntimeException::class.java) }
    }

    @Test
    fun testSkipLimit() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val skipLimit = ThreadLocalRandom.current().nextLong()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                skipLimit(skipLimit)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.skipLimit(skipLimit) }
    }

    @Test
    fun testTaskExecutor() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val taskExecutor = mockk<AsyncTaskExecutor>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                taskExecutor(taskExecutor)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.taskExecutor(taskExecutor) }
    }

    @Test
    fun testObservationRegistry() {
        // given
        val chunkOrientedStepBuilder = mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true)

        // when
        val observationRegistry = mockk<ObservationRegistry>()
        ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder)
            .apply {
                observationRegistry(observationRegistry)
            }.build()

        // then
        verify(exactly = 1) { chunkOrientedStepBuilder.observationRegistry(observationRegistry) }
    }

    @Test
    fun testBuild() {
        // given
        val mockStep = mockk<ChunkOrientedStep<Int, Int>>()
        val chunkOrientedStepBuilder =
            mockk<ChunkOrientedStepBuilder<Int, Int>>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        val actual = ChunkOrientedStepBuilderDsl(mockk(), chunkOrientedStepBuilder).build()

        // then
        assertThat(actual).isEqualTo(mockStep)
    }
}
