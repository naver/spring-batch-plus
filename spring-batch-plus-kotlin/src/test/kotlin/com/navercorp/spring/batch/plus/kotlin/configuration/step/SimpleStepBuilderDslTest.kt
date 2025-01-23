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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.ItemProcessListener
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.core.ItemWriteListener
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.step.builder.SimpleStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.repeat.RepeatCallback
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.exception.ExceptionHandler
import org.springframework.batch.repeat.support.RepeatTemplate
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.interceptor.TransactionAttribute
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

internal class SimpleStepBuilderDslTest {

    @Test
    fun testReader() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemReader = mockk<ItemReader<Int>>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            reader(itemReader)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.reader(itemReader) }
    }

    @Test
    fun testProcessor() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemProcessor = mockk<ItemProcessor<Int, Int>>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            processor(itemProcessor)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.processor(itemProcessor) }
    }

    @Test
    fun testWriter() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemWriter = mockk<ItemWriter<Int>>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            writer(itemWriter)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.writer(itemWriter) }
    }

    @Test
    fun testReaderIsTransactionalQueue() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            readerIsTransactionalQueue()
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.readerIsTransactionalQueue() }
    }

    @Test
    fun testObjectListener() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        class TestListener

        // when
        val testListener = TestListener()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            listener(testListener)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.listener(testListener) }
    }

    @Test
    fun testReadListener() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemReadListener = mockk<ItemReadListener<Int>>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            listener(itemReadListener)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.listener(itemReadListener) }
    }

    @Test
    fun testWriteListener() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemWriteListener = mockk<ItemWriteListener<Int>>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            listener(itemWriteListener)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.listener(itemWriteListener) }
    }

    @Test
    fun testProcessListener() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemProcessListener = mockk<ItemProcessListener<Int, Int>>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            listener(itemProcessListener)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.listener(itemProcessListener) }
    }

    @Test
    fun testChunkListener() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val chunkListener = mockk<ChunkListener>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            listener(chunkListener)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.listener(chunkListener) }
    }

    @Test
    fun testStream() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val itemStream = mockk<ItemStream>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            stream(itemStream)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.stream(itemStream) }
    }

    @Test
    fun testTaskExecutor() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val taskExecutor = mockk<TaskExecutor>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            taskExecutor(taskExecutor)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.taskExecutor(taskExecutor) }
    }

    @Suppress("DEPRECATION")
    @Test
    fun testThrottleLimit() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val taskExecutor = mockk<TaskExecutor>()
        val limit = ThreadLocalRandom.current().nextInt()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            taskExecutor(taskExecutor)
            throttleLimit(limit)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.throttleLimit(limit) }
    }

    @Test
    fun testExceptionHandler() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val exceptionHandler = mockk<ExceptionHandler>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            exceptionHandler(exceptionHandler)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.exceptionHandler(exceptionHandler) }
    }

    @Test
    fun testStepOperations() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val repeatOperations = mockk<RepeatOperations>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            stepOperations(repeatOperations)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.stepOperations(repeatOperations) }
    }

    @Test
    fun testTransactionAttribute() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when
        val transactionAttribute = mockk<TransactionAttribute>()
        SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
            transactionAttribute(transactionAttribute)
        }.build()

        // then
        verify(exactly = 1) { simpleStepBuilder.transactionAttribute(transactionAttribute) }
    }

    @Test
    fun testBuild() {
        // given
        val mockStep = mockk<TaskletStep>()
        val taskletStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true) {
            every { build() } returns mockStep
        }

        // when
        val actual = SimpleStepBuilderDsl(mockk(), taskletStepBuilder).build()

        // then
        assertThat(actual).isEqualTo(mockStep)
    }

    @Test
    fun testBuildWithSettingStepOperationsAndTaskExecutor() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when, then
        assertThatThrownBy {
            SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
                stepOperations(RepeatTemplate())
                taskExecutor(SyncTaskExecutor())
            }.build()
        }.hasMessageContaining("taskExecutor is redundant")
    }

    @Test
    fun testBuildWithSettingStepOperationsAndExceptionHandler() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when, then
        assertThatThrownBy {
            SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
                stepOperations(RepeatTemplate())
                exceptionHandler { _, e ->
                    throw e
                }
            }.build()
        }.hasMessageContaining("exceptionHandler is redundant")
    }

    @Suppress("DEPRECATION")
    @Test
    fun testBuildWithSettingThrottleLimitWhenNoTaskExecutor() {
        // given
        val simpleStepBuilder = mockk<SimpleStepBuilder<Int, Int>>(relaxed = true)

        // when, then
        assertThatThrownBy {
            SimpleStepBuilderDsl(mockk(), simpleStepBuilder).apply {
                throttleLimit(3)
            }.build()
        }.hasMessageContaining("throttleLimit is redundant")
    }

    @Nested
    inner class RedundancyCheck {

        @Test
        fun testStepOperationsAndRedundantSettings() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var readCallCount = 0
            var stepOperationCallCount = 0
            var taskExecutorCallCount = 0
            var exceptionHandlerCallCount = 0
            val stepBuilder = StepBuilder(UUID.randomUUID().toString(), mockk(relaxed = true))

            // when
            val step = stepBuilder
                .chunk<Int, Int>(chunkSize, ResourcelessTransactionManager())
                .reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                .writer { }
                .stepOperations(
                    object : RepeatTemplate() {
                        override fun iterate(callback: RepeatCallback): RepeatStatus {
                            ++stepOperationCallCount
                            return super.iterate(callback)
                        }
                    },
                )
                // redundant
                .taskExecutor { task ->
                    ++taskExecutorCallCount
                    task.run()
                }
                .exceptionHandler { _, e ->
                    ++exceptionHandlerCallCount
                    throw e
                }
                .build()
            val jobInstance = JobInstance(ThreadLocalRandom.current().nextLong(), UUID.randomUUID().toString())
            val jobExecution = JobExecution(jobInstance, JobParameters())
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(stepOperationCallCount).isEqualTo(1)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(taskExecutorCallCount).isEqualTo(0)
            assertThat(exceptionHandlerCallCount).isEqualTo(0)
        }

        @Suppress("DEPRECATION")
        @Test
        fun testThrottleLimitAndRedundantSettings() {
            // given
            val readLimit = 10000
            val chunkSize = 1
            var readCallCount = 0
            var processCallCount = 0
            var writeCallCount = 0
            val stepBuilder = StepBuilder(UUID.randomUUID().toString(), mockk(relaxed = true))

            // when
            val step = stepBuilder
                .chunk<Int, Int>(chunkSize, ResourcelessTransactionManager())
                .reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                .processor {
                    ++processCallCount
                    it
                }
                .writer {
                    ++writeCallCount
                }
                .throttleLimit(100)
                .build()
            val jobInstance = JobInstance(ThreadLocalRandom.current().nextLong(), UUID.randomUUID().toString())
            val jobExecution = JobExecution(jobInstance, JobParameters())
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            // if throttleLimit is applied to executor, it should be different by race condition
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(processCallCount).isEqualTo(readLimit)
            assertThat(writeCallCount).isEqualTo(10000)
        }
    }
}
