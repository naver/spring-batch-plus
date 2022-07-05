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

import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.ItemProcessListener
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.core.ItemWriteListener
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.AfterChunkError
import org.springframework.batch.core.annotation.AfterProcess
import org.springframework.batch.core.annotation.AfterRead
import org.springframework.batch.core.annotation.AfterWrite
import org.springframework.batch.core.annotation.BeforeChunk
import org.springframework.batch.core.annotation.BeforeProcess
import org.springframework.batch.core.annotation.BeforeRead
import org.springframework.batch.core.annotation.BeforeWrite
import org.springframework.batch.core.annotation.OnProcessError
import org.springframework.batch.core.annotation.OnReadError
import org.springframework.batch.core.annotation.OnWriteError
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.SimpleStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemStream
import org.springframework.batch.repeat.CompletionPolicy
import org.springframework.batch.repeat.RepeatCallback
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy
import org.springframework.batch.repeat.support.RepeatTemplate
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import org.springframework.transaction.support.TransactionSynchronizationManager
import kotlin.math.ceil

@Suppress("SameParameterValue")
internal class SimpleStepBuilderDslTest {

    private val jobInstance = JobInstance(0L, "testJob")

    private val jobParameters = JobParameters()

    private fun <I : Any, O : Any> simpleStepBuilderDsl(
        chunkSize: Int,
        init: SimpleStepBuilderDsl<I, O>.() -> Unit
    ): Step {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobBuilderFactory = mock(),
            stepBuilderFactory = mock(),
        )
        val stepBuilder = StepBuilder("testStep").apply {
            repository(mock())
            transactionManager(ResourcelessTransactionManager())
        }
        val simpleStepBuilder = stepBuilder.chunk<I, O>(chunkSize)

        return SimpleStepBuilderDsl(dslContext, simpleStepBuilder).apply(init).build()
    }

    private fun <I : Any, O : Any> simpleStepBuilderDsl(
        completionPolicy: CompletionPolicy,
        init: SimpleStepBuilderDsl<I, O>.() -> Unit
    ): Step {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobBuilderFactory = mock(),
            stepBuilderFactory = mock(),
        )
        val stepBuilder = StepBuilder("testStep").apply {
            repository(mock())
            transactionManager(ResourcelessTransactionManager())
        }
        val simpleStepBuilder = stepBuilder.chunk<I, O>(completionPolicy)

        return SimpleStepBuilderDsl(dslContext, simpleStepBuilder).apply(init).build()
    }

    private fun <I : Any, O : Any> simpleStepBuilderDsl(
        repeatOperations: RepeatOperations,
        init: SimpleStepBuilderDsl<I, O>.() -> Unit
    ): Step {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobBuilderFactory = mock(),
            stepBuilderFactory = mock(),
        )
        val stepBuilder = StepBuilder("testStep").apply {
            repository(mock())
            transactionManager(ResourcelessTransactionManager())
        }
        val simpleStepBuilder =
            SimpleStepBuilder<I, O>(stepBuilder).chunkOperations(repeatOperations)

        return SimpleStepBuilderDsl(dslContext, simpleStepBuilder).apply(init).build()
    }

    private fun calculateWriteSize(readLimit: Int, chunkSize: Int) =
        ceil(readLimit.toDouble() / chunkSize.toDouble()).toInt()

    @Test
    fun testWithChunkSize() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var processCallCount = 0
        var writeCallCount = 0

        // when
        val step = simpleStepBuilderDsl(chunkSize) {
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor {
                ++processCallCount
                it
            }
            writer {
                ++writeCallCount
            }
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(processCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testWithCompletionPolicy() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var processCallCount = 0
        var writeCallCount = 0

        // when
        val step = simpleStepBuilderDsl(SimpleCompletionPolicy(chunkSize)) {
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor {
                ++processCallCount
                it
            }
            writer {
                ++writeCallCount
            }
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(processCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testWithRepeatOperations() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var processCallCount = 0
        var writeCallCount = 0

        // when
        val step = simpleStepBuilderDsl(
            RepeatTemplate().apply {
                setCompletionPolicy(SimpleCompletionPolicy(chunkSize))
            }
        ) {
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor {
                ++processCallCount
                it
            }
            writer {
                ++writeCallCount
            }
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(processCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testReaderWriterProcessor() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var processCallCount = 0
        var writeCallCount = 0

        // when
        val step = simpleStepBuilderDsl(chunkSize) {
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor {
                ++processCallCount
                it
            }
            writer {
                ++writeCallCount
            }
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)
        println(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(processCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testReaderIsTransactionalQueue() {
        // given
        val chunkSize = 5
        val readLimit = 3
        val retryLimit = 2
        var readCallCount = 0
        var readCounter = 0
        var tryCount = 1

        // when
        val step = simpleStepBuilderDsl<Int, Int>(chunkSize) {
            reader {
                if (readCounter < readLimit) {
                    ++readCallCount
                    ++readCounter
                    1
                } else {
                    null
                }
            }
            writer {
                println("$tryCount")
                if (tryCount < retryLimit) {
                    ++tryCount
                    readCounter = 0
                    throw IllegalStateException("Error")
                }
                println("no error")
            }
            readerIsTransactionalQueue()
            faultTolerant {
                retryLimit(retryLimit)
                retry<IllegalStateException>()
            }
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        // without readerIsTransactionalQueue, it would be same as readLimit since read result is cached.
        assertThat(readCallCount).isEqualTo(readLimit * tryCount)
    }

    @Test
    fun testObjectListener() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var beforeChunkCallCount = 0
        var afterChunkCallCount = 0
        var beforeReadCallCount = 0
        var afterReadCallCount = 0
        var beforeProcessCallCount = 0
        var afterProcessCallCount = 0
        var beforeWriteCallCount = 0
        var afterWriteCallCount = 0

        @Suppress("unused")
        class TestListener {
            @BeforeChunk
            fun beforeChunk() {
                ++beforeChunkCallCount
            }

            @AfterChunk
            fun afterChunk() {
                ++afterChunkCallCount
            }

            @AfterChunkError
            fun afterChunkError() {
                // no need to test. we are just testing if listener is invoked
            }

            @BeforeRead
            fun beforeRead() {
                ++beforeReadCallCount
            }

            @AfterRead
            fun afterRead() {
                ++afterReadCallCount
            }

            @OnReadError
            fun onReadError() {
                // no need to test. we are just testing if listener is invoked
            }

            @BeforeProcess
            fun beforeProcess() {
                ++beforeProcessCallCount
            }

            @AfterProcess
            fun afterProcess() {
                ++afterProcessCallCount
            }

            @OnProcessError
            fun onProcessError() {
                // no need to test. we are just testing if listener is invoked
            }

            @BeforeWrite
            fun beforeWrite() {
                ++beforeWriteCallCount
            }

            @AfterWrite
            fun afterWrite() {
                ++afterWriteCallCount
            }

            @OnWriteError
            fun onWriteError() {
                // no need to test. we are just testing if listener is invoked
            }
        }

        // when
        val step = simpleStepBuilderDsl(chunkSize) {
            listener(TestListener())
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor { it }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(beforeChunkCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
        assertThat(afterChunkCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
        assertThat(beforeReadCallCount).isEqualTo(readLimit + 1)
        assertThat(afterReadCallCount).isEqualTo(readLimit)
        assertThat(beforeProcessCallCount).isEqualTo(readLimit)
        assertThat(afterProcessCallCount).isEqualTo(readLimit)
        assertThat(beforeWriteCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
        assertThat(afterWriteCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testReadListener() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var beforeReadCallCount = 0
        var afterReadCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(chunkSize) {
            listener(
                object : ItemReadListener<Number> {
                    override fun beforeRead() {
                        ++beforeReadCallCount
                    }

                    override fun afterRead(item: Number) {
                        ++afterReadCallCount
                    }

                    override fun onReadError(ex: Exception) {
                        // no need to test. we are just testing if listener is invoked
                    }
                }
            )
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(beforeReadCallCount).isEqualTo(readLimit + 1)
        assertThat(afterReadCallCount).isEqualTo(readLimit)
    }

    @Test
    fun testWriteListener() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var beforeWriteCallCount = 0
        var afterWriteCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(chunkSize) {
            listener(
                object : ItemWriteListener<Number> {
                    override fun beforeWrite(items: MutableList<out Number>) {
                        ++beforeWriteCallCount
                    }

                    override fun afterWrite(items: MutableList<out Number>) {
                        ++afterWriteCallCount
                    }

                    override fun onWriteError(ex: Exception, items: MutableList<out Number>) {
                        // no need to test. we are just testing if listener is invoked
                    }
                }
            )
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(beforeWriteCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
        assertThat(afterWriteCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testProcessListener() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var beforeProcessCallCount = 0
        var afterProcessCallCount = 0

        // when
        val step = simpleStepBuilderDsl(chunkSize) {
            listener(
                object : ItemProcessListener<Number, Number> {
                    override fun beforeProcess(item: Number) {
                        ++beforeProcessCallCount
                    }

                    override fun afterProcess(item: Number, result: Number?) {
                        ++afterProcessCallCount
                    }

                    override fun onProcessError(item: Number, ex: Exception) {
                        // no need to test. we are just testing if listener is invoked
                    }
                }
            )
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor { it }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(beforeProcessCallCount).isEqualTo(readLimit)
        assertThat(afterProcessCallCount).isEqualTo(readLimit)
    }

    @Test
    fun testChunkListener() {
        // given
        var beforeChunkCallCount = 0
        var afterChunkCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(3) {
            listener(
                object : ChunkListener {
                    override fun beforeChunk(context: ChunkContext) {
                        ++beforeChunkCallCount
                    }

                    override fun afterChunk(context: ChunkContext) {
                        ++afterChunkCallCount
                    }

                    override fun afterChunkError(context: ChunkContext) {
                        // no need to test. we are just testing if listener is invoked
                    }
                }
            )
            reader { null }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(beforeChunkCallCount).isEqualTo(1)
        assertThat(afterChunkCallCount).isEqualTo(1)
    }

    @Test
    fun testStream() {
        // given
        var openCallCount = 0
        var updateCallCount = 0
        var closeCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(3) {
            stream(
                object : ItemStream {
                    override fun open(executionContext: ExecutionContext) {
                        ++openCallCount
                    }

                    override fun update(executionContext: ExecutionContext) {
                        ++updateCallCount
                    }

                    override fun close() {
                        ++closeCallCount
                    }
                }
            )
            reader { null }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(openCallCount).isEqualTo(1)
        assertThat(updateCallCount).isGreaterThan(1)
        assertThat(closeCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskExecutor() {
        // given
        var taskExecutorCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(1) {
            taskExecutor { task ->
                ++taskExecutorCallCount
                task.run()
            }
            reader { null }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskExecutorCallCount).isEqualTo(1)
    }

    @RepeatedTest(10)
    fun testThrottleLimit() {
        // given
        val readLimit = 10000
        val chunkSize = 1
        var readCallCount = 0
        var processCallCount = 0
        var writeCallCount = 0
        val taskExecutor = ThreadPoolTaskExecutor().apply {
            corePoolSize = Runtime.getRuntime().availableProcessors()
            initialize()
        }

        // when
        val step = simpleStepBuilderDsl(chunkSize) {
            taskExecutor(taskExecutor)
            throttleLimit(1)
            reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            processor {
                ++processCallCount
                it
            }
            writer {
                ++writeCallCount
            }
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        // since throttleLimit is applied, all chunk run in single thread
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(processCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testExceptionHandler() {
        // given
        var exceptionHandlerCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(1) {
            exceptionHandler { _, e ->
                ++exceptionHandlerCallCount
                throw e
            }
            reader { throw RuntimeException("error") }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(exceptionHandlerCallCount).isEqualTo(1)
    }

    @Test
    fun testStepOperations() {
        // given
        var stepOperationCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(1) {
            stepOperations(
                object : RepeatTemplate() {
                    override fun iterate(callback: RepeatCallback): RepeatStatus {
                        ++stepOperationCallCount
                        return super.iterate(callback)
                    }
                }
            )
            reader { null }
            writer {}
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepOperationCallCount).isEqualTo(1)
    }

    @Test
    fun testTransactionalAttribute() {
        // given
        var readCallCount = 0

        // when
        val step = simpleStepBuilderDsl<Int, Int>(1) {
            reader {
                if (readCallCount < 1) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            writer {
                val actual = TransactionSynchronizationManager.getCurrentTransactionName()
                assertThat(actual).isEqualTo("some_tx")
            }
            transactionAttribute(
                DefaultTransactionAttribute().apply {
                    setName("some_tx")
                }
            )
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(1)
    }

    @Test
    fun testStepOperationsAndRedundantSettings() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0
        var stepOperationCallCount = 0
        var taskExecutorCallCount = 0
        var exceptionHandlerCallCount = 0
        val stepBuilder = StepBuilder("testStep").apply {
            repository(mock())
            transactionManager(ResourcelessTransactionManager())
        }

        // when
        val step = stepBuilder
            .chunk<Int, Int>(chunkSize)
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
                }
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
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepOperationCallCount).isEqualTo(1)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(taskExecutorCallCount).isEqualTo(0)
        assertThat(exceptionHandlerCallCount).isEqualTo(0)
    }

    @Test
    fun testStepOperationsAndSetTaskExecutor() {
        assertThatThrownBy {
            simpleStepBuilderDsl<Int, Int>(1) {
                stepOperations(RepeatTemplate())
                taskExecutor(SyncTaskExecutor())
                reader { null }
                writer {}
            }
        }.hasMessageContaining("taskExecutor is redundant")
    }

    @Test
    fun testStepOperationsAndSetExceptionHandler() {
        assertThatThrownBy {
            simpleStepBuilderDsl<Int, Int>(1) {
                stepOperations(RepeatTemplate())
                exceptionHandler { _, e ->
                    throw e
                }
                reader { null }
                writer {}
            }
        }.hasMessageContaining("exceptionHandler is redundant")
    }

    @RepeatedTest(10)
    fun testThrottleLimitAndRedundantSettings() {
        // given
        val readLimit = 10000
        val chunkSize = 1
        var readCallCount = 0
        var processCallCount = 0
        var writeCallCount = 0
        val stepBuilder = StepBuilder("testStep").apply {
            repository(mock())
            transactionManager(ResourcelessTransactionManager())
        }

        // when
        val step = stepBuilder
            .chunk<Int, Int>(chunkSize)
            .reader {
                if (readCallCount < readLimit) {
                    ++readCallCount
                    1
                } else {
                    null
                }
            }
            .processor(
                ItemProcessor {
                    ++processCallCount
                    it
                }
            )
            .writer {
                ++writeCallCount
            }
            .throttleLimit(100)
            .build()
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        // if throttleLimit is applied to executor, it should be different by race condition
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(processCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(calculateWriteSize(readLimit, chunkSize))
    }

    @Test
    fun testThrottleLimitSetWhenTaskExecutorIsNotSet() {
        assertThatThrownBy {
            simpleStepBuilderDsl<Int, Int>(1) {
                reader { null }
                writer {}
                throttleLimit(3)
            }
        }.hasMessageContaining("throttleLimit is redundant")
    }
}
