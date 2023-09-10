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
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.builder.TaskletStepBuilder
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemStream
import org.springframework.batch.repeat.RepeatCallback
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.exception.ExceptionHandler
import org.springframework.batch.repeat.support.RepeatTemplate
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.interceptor.TransactionAttribute
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

internal class TaskletStepBuilderDslTest {

    @Test
    fun testChunkListener() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val chunkListener = mockk<ChunkListener>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            listener(chunkListener)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.listener(chunkListener) }
    }

    @Test
    fun testObjectListener() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        class TestListener

        // when
        val testListener = TestListener()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            listener(testListener)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.listener(testListener) }
    }

    @Test
    fun testStream() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val itemStream = mockk<ItemStream>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            stream(itemStream)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.stream(itemStream) }
    }

    @Test
    fun testTaskExecutor() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val taskExecutor = mockk<TaskExecutor>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            taskExecutor(taskExecutor)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.taskExecutor(taskExecutor) }
    }

    @Test
    fun testExceptionHandler() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val exceptionHandler = mockk<ExceptionHandler>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            exceptionHandler(exceptionHandler)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.exceptionHandler(exceptionHandler) }
    }

    @Test
    fun testStepOperations() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val repeatOperations = mockk<RepeatOperations>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            stepOperations(repeatOperations)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.stepOperations(repeatOperations) }
    }

    @Test
    fun testTransactionalAttribute() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val transactionAttribute = mockk<TransactionAttribute>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
            transactionAttribute(transactionAttribute)
        }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.transactionAttribute(transactionAttribute) }
    }

    @Test
    fun testBuild() {
        // given
        val mockStep = mockk<TaskletStep>()
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true) {
            every { build() } returns mockStep
        }

        // when
        val actual = TaskletStepBuilderDsl(mockk(), taskletStepBuilder).build()

        // then
        assertThat(actual).isEqualTo(mockStep)
    }

    @Test
    fun testBuildWithSettingStepOperationsAndTaskExecutor() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when, then
        val repeatOperations = mockk<RepeatOperations>()
        val taskExecutor = mockk<TaskExecutor>()
        assertThatThrownBy {
            TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
                stepOperations(repeatOperations)
                taskExecutor(taskExecutor)
            }.build()
        }.hasMessageContaining("taskExecutor is redundant")
    }

    @Test
    fun testBuildWithSettingStepOperationsAndExceptionHandler() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when, then
        val repeatOperations = mockk<RepeatOperations>()
        val exceptionHandler = mockk<ExceptionHandler>()
        assertThatThrownBy {
            TaskletStepBuilderDsl(mockk(), taskletStepBuilder).apply {
                stepOperations(repeatOperations)
                exceptionHandler(exceptionHandler)
            }.build()
        }.hasMessageContaining("exceptionHandler is redundant")
    }

    @Nested
    inner class RedundancyCheck {

        @Test
        fun testStepOperationsAndRedundantSettings() {
            // given
            var iterateCount = 0
            var taskExecutorCallCount = 0
            var exceptionHandlerCallCount = 0
            val stepBuilder = StepBuilder(UUID.randomUUID().toString(), mockk(relaxed = true))

            // when
            val step = stepBuilder
                .tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                .stepOperations(
                    object : RepeatTemplate() {
                        override fun iterate(callback: RepeatCallback): RepeatStatus {
                            ++iterateCount
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
            val jobInstance = JobInstance(ThreadLocalRandom.current().nextLong(), UUID.randomUUID().toString())
            val jobExecution = JobExecution(jobInstance, JobParameters())
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(iterateCount).isEqualTo(1)
            assertThat(taskExecutorCallCount).isEqualTo(0)
            assertThat(exceptionHandlerCallCount).isEqualTo(0)
        }
    }
}
