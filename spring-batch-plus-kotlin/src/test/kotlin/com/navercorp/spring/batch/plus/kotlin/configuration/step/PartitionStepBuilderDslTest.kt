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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.batch.core.partition.PartitionHandler
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.core.partition.StepExecutionAggregator
import org.springframework.batch.core.partition.StepExecutionSplitter
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.PartitionStepBuilder
import org.springframework.core.task.TaskExecutor
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

/**
 * Unit tests for PartitionStepBuilderDsl's delegation and DSL-owned preconditions.
 */
internal class PartitionStepBuilderDslTest {

    @Test
    fun testPartitionHandler() {
        // given
        val partitionHandler = mockk<PartitionHandler>()
        val splitter = mockk<StepExecutionSplitter>()
        val mockStep = mockk<Step>()
        val partitionStepBuilder =
            mockk<PartitionStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        partitionStepBuilderDsl(partitionStepBuilder) {
            partitionHandler(partitionHandler)
            splitter(splitter)
        }

        // then
        verify(exactly = 1) { partitionStepBuilder.partitionHandler(partitionHandler) }
    }

    @Test
    fun testPartitionHandlerWithInit() {
        // given
        val step = mockk<Step>()
        val taskExecutor = mockk<TaskExecutor>()
        val gridSize = ThreadLocalRandom.current().nextInt(1, 10)
        val splitter = mockk<StepExecutionSplitter>()
        val mockStep = mockk<Step>()
        val partitionStepBuilder =
            mockk<PartitionStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        partitionStepBuilderDsl(partitionStepBuilder) {
            partitionHandler {
                step(step)
                taskExecutor(taskExecutor)
                gridSize(gridSize)
            }
            splitter(splitter)
        }

        // then
        verify(exactly = 1) { partitionStepBuilder.step(step) }
        verify(exactly = 1) { partitionStepBuilder.taskExecutor(taskExecutor) }
        verify(exactly = 1) { partitionStepBuilder.gridSize(gridSize) }
    }

    @Test
    fun testSplitter() {
        // given
        val partitionHandler = mockk<PartitionHandler>()
        val splitter = mockk<StepExecutionSplitter>()
        val mockStep = mockk<Step>()
        val partitionStepBuilder =
            mockk<PartitionStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        partitionStepBuilderDsl(partitionStepBuilder) {
            partitionHandler(partitionHandler)
            splitter(splitter)
        }

        // then
        verify(exactly = 1) { partitionStepBuilder.splitter(splitter) }
    }

    @Test
    fun testSplitterWithPartitioner() {
        // given
        val stepName = UUID.randomUUID().toString()
        val partitionHandler = mockk<PartitionHandler>()
        val partitioner = mockk<Partitioner>()
        val mockStep = mockk<Step>()
        val partitionStepBuilder =
            mockk<PartitionStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        partitionStepBuilderDsl(partitionStepBuilder) {
            partitionHandler(partitionHandler)
            splitter(stepName, partitioner)
        }

        // then
        verify(exactly = 1) { partitionStepBuilder.partitioner(stepName, partitioner) }
    }

    @Test
    fun testAggregator() {
        // given
        val aggregator = mockk<StepExecutionAggregator>()
        val partitionHandler = mockk<PartitionHandler>()
        val splitter = mockk<StepExecutionSplitter>()
        val mockStep = mockk<Step>()
        val partitionStepBuilder =
            mockk<PartitionStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        partitionStepBuilderDsl(partitionStepBuilder) {
            aggregator(aggregator)
            partitionHandler(partitionHandler)
            splitter(splitter)
        }

        // then
        verify(exactly = 1) { partitionStepBuilder.aggregator(aggregator) }
    }

    @Test
    fun testBuild() {
        // given
        val partitionHandler = mockk<PartitionHandler>()
        val splitter = mockk<StepExecutionSplitter>()
        val mockStep = mockk<Step>()
        val partitionStepBuilder =
            mockk<PartitionStepBuilder>(relaxed = true) {
                every { build() } returns mockStep
            }

        // when
        val actual =
            partitionStepBuilderDsl(partitionStepBuilder) {
                partitionHandler(partitionHandler)
                splitter(splitter)
            }

        // then
        assertThat(actual).isEqualTo(mockStep)
    }

    @Test
    fun testPartitionHandlerBuilderRequiresStep() {
        // given
        val taskExecutor = mockk<TaskExecutor>()
        val gridSize = ThreadLocalRandom.current().nextInt(1, 10)
        val partitionStepBuilder = mockk<PartitionStepBuilder>(relaxed = true)

        // when, then
        assertThatThrownBy {
            PartitionStepBuilderDsl(mockk(), partitionStepBuilder)
                .partitionHandler {
                    taskExecutor(taskExecutor)
                    gridSize(gridSize)
                }
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun testWithoutPartitionHandler() {
        // given
        val splitter = mockk<StepExecutionSplitter>()
        val partitionStepBuilder = mockk<PartitionStepBuilder>(relaxed = true)

        // when, then
        assertThatThrownBy {
            PartitionStepBuilderDsl(mockk(), partitionStepBuilder)
                .apply {
                    splitter(splitter)
                }.build()
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun testWithoutSplitter() {
        // given
        val partitionHandler = mockk<PartitionHandler>()
        val partitionStepBuilder = mockk<PartitionStepBuilder>(relaxed = true)

        // when, then
        assertThatThrownBy {
            PartitionStepBuilderDsl(mockk(), partitionStepBuilder)
                .apply {
                    partitionHandler(partitionHandler)
                }.build()
        }.isInstanceOf(IllegalStateException::class.java)
    }

    private fun partitionStepBuilderDsl(
        partitionStepBuilder: PartitionStepBuilder,
        init: PartitionStepBuilderDsl.() -> Unit,
    ): Step =
        PartitionStepBuilderDsl(mockk<DslContext>(), partitionStepBuilder)
            .apply(init)
            .build()
}
