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
import org.junit.jupiter.api.Test
import org.springframework.batch.core.listener.ChunkListener
import org.springframework.batch.core.step.builder.TaskletStepBuilder
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.infrastructure.item.ItemStream
import org.springframework.batch.infrastructure.repeat.RepeatOperations
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.interceptor.TransactionAttribute

/**
 * Unit tests for TaskletStepBuilderDsl's delegation to Spring Batch's TaskletStepBuilder.
 */
internal class TaskletStepBuilderDslTest {
    @Test
    fun testChunkListener() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val chunkListener = mockk<ChunkListener<*, *>>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
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
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
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
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
                stream(itemStream)
            }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.stream(itemStream) }
    }

    @Suppress("DEPRECATION")
    @Test
    fun testTaskExecutor() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val taskExecutor = mockk<TaskExecutor>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
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
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
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
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
                stepOperations(repeatOperations)
            }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.stepOperations(repeatOperations) }
    }

    @Test
    fun testTransactionManager() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val transactionManager = mockk<PlatformTransactionManager>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
                transactionManager(transactionManager)
            }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.transactionManager(transactionManager) }
    }

    @Test
    fun testTransactionalAttribute() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when
        val transactionAttribute = mockk<TransactionAttribute>()
        TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
            .apply {
                transactionAttribute(transactionAttribute)
            }.build()

        // then
        verify(exactly = 1) { taskletStepBuilder.transactionAttribute(transactionAttribute) }
    }

    @Test
    fun testBuild() {
        // given
        val mockStep = mockk<TaskletStep>()
        val taskletStepBuilder =
            mockk<TaskletStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        val actual = TaskletStepBuilderDsl(mockk(), taskletStepBuilder).build()

        // then
        assertThat(actual).isEqualTo(mockStep)
    }

    @Suppress("DEPRECATION")
    @Test
    fun testBuildWithSettingStepOperationsAndTaskExecutor() {
        // given
        val taskletStepBuilder = mockk<TaskletStepBuilder>(relaxed = true)

        // when, then
        val repeatOperations = mockk<RepeatOperations>()
        val taskExecutor = mockk<TaskExecutor>()
        assertThatThrownBy {
            TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
                .apply {
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
            TaskletStepBuilderDsl(mockk(), taskletStepBuilder)
                .apply {
                    stepOperations(repeatOperations)
                    exceptionHandler(exceptionHandler)
                }.build()
        }.hasMessageContaining("exceptionHandler is redundant")
    }
}
