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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.partition.PartitionHandler
import org.springframework.batch.core.partition.StepExecutionSplitter
import org.springframework.batch.core.step.builder.PartitionStepBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.support.transaction.ResourcelessTransactionManager

internal class PartitionStepBuilderDslTest {

    private val jobInstance = JobInstance(0L, "testJob")

    private val jobParameters = JobParameters()

    private fun partitionStepBuilderDsl(init: PartitionStepBuilderDsl.() -> Unit): Step {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobBuilderFactory = mock(),
            stepBuilderFactory = mock(),
        )
        val stepBuilder = StepBuilder("testStep").apply {
            repository(mock())
            transactionManager(ResourcelessTransactionManager())
        }

        return PartitionStepBuilderDsl(dslContext, PartitionStepBuilder(stepBuilder)).apply(init).build()
    }

    @Nested
    inner class PartitionHandlerTest {

        @Test
        fun testPartitionHandlerAndDummySettings() {
            // given
            var partitionHandlerCallCount = 0
            var taskExecutorCallCount = 0
            val stepBuilder = StepBuilder("testStep").apply {
                repository(mock())
                transactionManager(ResourcelessTransactionManager())
            }
            val partitionStepBuilder = PartitionStepBuilder(stepBuilder)

            // when
            val step = partitionStepBuilder
                .partitionHandler { _, _ ->
                    ++partitionHandlerCallCount
                    listOf()
                }
                // dummy
                .step(
                    object : Step {
                        override fun getName(): String {
                            throw RuntimeException("Should not be called")
                        }

                        override fun isAllowStartIfComplete(): Boolean {
                            throw RuntimeException("Should not be called")
                        }

                        override fun getStartLimit(): Int {
                            throw RuntimeException("Should not be called")
                        }

                        override fun execute(stepExecution: StepExecution) {
                            throw RuntimeException("Should not be called")
                        }
                    }
                )
                .taskExecutor { task ->
                    ++taskExecutorCallCount
                    task.run()
                }
                .gridSize(3)
                .build()
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(partitionHandlerCallCount).isEqualTo(1)
            assertThat(taskExecutorCallCount).isEqualTo(0)
        }

        @Test
        fun testPartitionHandlerWithDirectOne() {
            // given
            val gridSize = 4
            var partitionHandlerCallCount = 0
            val splitter = object : StepExecutionSplitter {
                override fun getStepName(): String = "splitStep"

                override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                    val jobExecution = stepExecution.jobExecution
                    return (0 until gridSize)
                        .map {
                            jobExecution.createStepExecution("${stepName}$it")
                        }
                        .toSet()
                }
            }

            // when
            val step = partitionStepBuilderDsl {
                partitionHandler { splitter, stepExecution ->
                    ++partitionHandlerCallCount
                    splitter.split(stepExecution, gridSize)
                        .map {
                            it.apply {
                                exitStatus = ExitStatus.COMPLETED
                                status = BatchStatus.COMPLETED
                            }
                        }
                }
                splitter(splitter)
            }
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(partitionHandlerCallCount).isEqualTo(1)
            assertThat(jobExecution.stepExecutions).hasSize(gridSize + 1)
            assertThat(jobExecution.stepExecutions).allSatisfy {
                assertThat(it.status).isEqualTo(BatchStatus.COMPLETED)
            }
            assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
            (0 until gridSize).forEach { gridNumber ->
                assertThat(jobExecution.stepExecutions.find { it.stepName == "splitStep$gridNumber" }).isNotNull
            }
        }

        @Test
        fun testPartitionHandlerWithTaskExecutorPartitionHandler() {
            // given
            var stepExecuteCallCount = 0
            var taskExecutorCallCount = 0
            val gridSize = 4
            val splitter = object : StepExecutionSplitter {
                override fun getStepName(): String = "splitStep"

                override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                    val jobExecution = stepExecution.jobExecution
                    return (0 until gridSize)
                        .map {
                            jobExecution.createStepExecution("${stepName}$it")
                        }
                        .toSet()
                }
            }

            // when
            val step = partitionStepBuilderDsl {
                partitionHandler {
                    step(
                        object : Step {
                            override fun getName(): String {
                                throw RuntimeException("Should not be called")
                            }

                            override fun isAllowStartIfComplete(): Boolean {
                                throw RuntimeException("Should not be called")
                            }

                            override fun getStartLimit(): Int {
                                throw RuntimeException("Should not be called")
                            }

                            override fun execute(stepExecution: StepExecution) {
                                ++stepExecuteCallCount
                                stepExecution.apply {
                                    status = BatchStatus.COMPLETED
                                    exitStatus = ExitStatus.COMPLETED
                                }
                            }
                        }
                    )
                    taskExecutor { task ->
                        ++taskExecutorCallCount
                        task.run()
                    }
                    gridSize(gridSize)
                }
                splitter(splitter)
            }
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecuteCallCount).isEqualTo(gridSize)
            assertThat(taskExecutorCallCount).isEqualTo(gridSize)
            assertThat(jobExecution.stepExecutions).hasSize(gridSize + 1)
            assertThat(jobExecution.stepExecutions).allSatisfy {
                assertThat(it.status).isEqualTo(BatchStatus.COMPLETED)
            }
            assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
            (0 until gridSize).forEach { gridNumber ->
                assertThat(jobExecution.stepExecutions.find { it.stepName == "splitStep$gridNumber" }).isNotNull
            }
        }

        @Test
        fun testPartitionHandlerWithTaskExecutorPartitionHandlerWithoutTaskExecutor() {
            // given
            var stepExecuteCallCount = 0
            val gridSize = 4
            val splitter = object : StepExecutionSplitter {
                override fun getStepName(): String = "splitStep"

                override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                    val jobExecution = stepExecution.jobExecution
                    return (0 until gridSize)
                        .map {
                            jobExecution.createStepExecution("${stepName}$it")
                        }
                        .toSet()
                }
            }

            // when
            val step = partitionStepBuilderDsl {
                partitionHandler {
                    step(
                        object : Step {
                            override fun getName(): String {
                                throw RuntimeException("Should not be called")
                            }

                            override fun isAllowStartIfComplete(): Boolean {
                                throw RuntimeException("Should not be called")
                            }

                            override fun getStartLimit(): Int {
                                throw RuntimeException("Should not be called")
                            }

                            override fun execute(stepExecution: StepExecution) {
                                ++stepExecuteCallCount
                                stepExecution.apply {
                                    status = BatchStatus.COMPLETED
                                    exitStatus = ExitStatus.COMPLETED
                                }
                            }
                        }
                    )
                    gridSize(gridSize)
                }
                splitter(splitter)
            }
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecuteCallCount).isEqualTo(gridSize)
            assertThat(jobExecution.stepExecutions).hasSize(gridSize + 1)
            assertThat(jobExecution.stepExecutions).allSatisfy {
                assertThat(it.status).isEqualTo(BatchStatus.COMPLETED)
            }
            assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
            (0 until gridSize).forEach { gridNumber ->
                assertThat(jobExecution.stepExecutions.find { it.stepName == "splitStep$gridNumber" }).isNotNull
            }
        }

        @Test
        fun testPartitionHandlerWithTaskExecutorPartitionHandlerWithoutGridSize() {
            // given
            var stepExecuteCallCount = 0
            var taskExecutorCallCount = 0
            val splitter = object : StepExecutionSplitter {
                override fun getStepName(): String = "splitStep"

                override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                    val jobExecution = stepExecution.jobExecution
                    return (0 until gridSize)
                        .map {
                            jobExecution.createStepExecution("${stepName}$it")
                        }
                        .toSet()
                }
            }
            // org.springframework.batch.core.step.builder.PartitionStepBuilder.DEFAULT_GRID_SIZE
            val defaultGridSize = 6

            // when
            val step = partitionStepBuilderDsl {
                partitionHandler {
                    step(
                        object : Step {
                            override fun getName(): String {
                                throw RuntimeException("Should not be called")
                            }

                            override fun isAllowStartIfComplete(): Boolean {
                                throw RuntimeException("Should not be called")
                            }

                            override fun getStartLimit(): Int {
                                throw RuntimeException("Should not be called")
                            }

                            override fun execute(stepExecution: StepExecution) {
                                ++stepExecuteCallCount
                                stepExecution.apply {
                                    status = BatchStatus.COMPLETED
                                    exitStatus = ExitStatus.COMPLETED
                                }
                            }
                        }
                    )
                    taskExecutor { task ->
                        ++taskExecutorCallCount
                        task.run()
                    }
                }
                splitter(splitter)
            }
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecuteCallCount).isEqualTo(defaultGridSize)
            assertThat(taskExecutorCallCount).isEqualTo(defaultGridSize)
            assertThat(jobExecution.stepExecutions).hasSize(defaultGridSize + 1)
            assertThat(jobExecution.stepExecutions).allSatisfy {
                assertThat(it.status).isEqualTo(BatchStatus.COMPLETED)
            }
            assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
            (0 until defaultGridSize).forEach { gridNumber ->
                assertThat(jobExecution.stepExecutions.find { it.stepName == "splitStep$gridNumber" }).isNotNull
            }
        }

        @Test
        fun testPartitionHandlerWithTaskExecutorPartitionHandlerWithoutStep() {
            // given
            val splitter = object : StepExecutionSplitter {
                override fun getStepName(): String = "splitStep"

                override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                    val jobExecution = stepExecution.jobExecution
                    return (0 until gridSize)
                        .map {
                            jobExecution.createStepExecution("${stepName}$it")
                        }
                        .toSet()
                }
            }

            // when, then
            assertThatThrownBy {
                partitionStepBuilderDsl {
                    partitionHandler {
                        taskExecutor { task ->
                            task.run()
                        }
                        gridSize(3)
                    }
                    splitter(splitter)
                }
            }.hasMessageContaining("step is not set")
        }

        @Test
        fun testWithoutPartitionHandler() {
            // given
            val splitter = object : StepExecutionSplitter {
                override fun getStepName(): String = "splitStep"

                override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                    val jobExecution = stepExecution.jobExecution
                    return (0 until gridSize)
                        .map {
                            jobExecution.createStepExecution("${stepName}$it")
                        }
                        .toSet()
                }
            }

            // when, then
            assertThatThrownBy {
                partitionStepBuilderDsl {
                    splitter(splitter)
                }
            }.hasMessageContaining("partitionHandler is not set")
        }
    }

    @Nested
    inner class SplitterTest {

        @Test
        fun testSplitterAndDummySettings() {
            // given
            var splitterCallCount = 0
            val dummyStepName = "dummyStepName"
            var partitionerCallCount = 0
            val stepBuilder = StepBuilder("testStep").apply {
                repository(mock())
                transactionManager(ResourcelessTransactionManager())
            }
            val partitionStepBuilder = PartitionStepBuilder(stepBuilder)

            // when
            val step = partitionStepBuilder
                .partitionHandler { stepSplitter, stepExecution ->
                    stepSplitter.split(stepExecution, 1)
                }
                .splitter(
                    object : StepExecutionSplitter {
                        override fun getStepName(): String {
                            return "testStep"
                        }

                        override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                            ++splitterCallCount
                            return setOf()
                        }
                    }
                )
                // dummy
                .partitioner(dummyStepName) {
                    ++partitionerCallCount
                    mapOf()
                }
                .build()
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(splitterCallCount).isEqualTo(1)
            assertThat(stepExecution.stepName).isEqualTo("testStep")
            assertThat(partitionerCallCount).isEqualTo(0)
        }

        @Test
        fun testSplitterWithDirectOne() {
            // given
            var splitterCallCount = 0
            val gridSize = 4
            val partitionHandler = PartitionHandler { stepSplitter, stepExecution ->
                stepSplitter.split(stepExecution, gridSize)
                    .map {
                        it.apply {
                            exitStatus = ExitStatus.COMPLETED
                            status = BatchStatus.COMPLETED
                        }
                    }
            }

            // when
            val step = partitionStepBuilderDsl {
                partitionHandler(partitionHandler)
                splitter(

                    object : StepExecutionSplitter {
                        override fun getStepName(): String = "splitStep"

                        override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                            ++splitterCallCount
                            val jobExecution = stepExecution.jobExecution
                            return (0 until gridSize)
                                .map {
                                    jobExecution.createStepExecution("${stepName}$it")
                                }
                                .toSet()
                        }
                    }
                )
            }
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(splitterCallCount).isEqualTo(1)
            assertThat(jobExecution.stepExecutions).hasSize(gridSize + 1)
            assertThat(jobExecution.stepExecutions).allSatisfy {
                assertThat(it.status).isEqualTo(BatchStatus.COMPLETED)
            }
            assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
            (0 until gridSize).forEach { gridNumber ->
                assertThat(jobExecution.stepExecutions.find { it.stepName == "splitStep$gridNumber" }).isNotNull
            }
        }

        @Test
        fun testSplitterWithSimpleStepExecutionSplitter() {
            // given
            var partitionerCallCount = 0
            val gridSize = 4
            val partitionHandler = PartitionHandler { stepSplitter, stepExecution ->
                stepSplitter.split(stepExecution, gridSize)
                    .map {
                        it.apply {
                            exitStatus = ExitStatus.COMPLETED
                            status = BatchStatus.COMPLETED
                        }
                    }
            }

            // when
            val step = partitionStepBuilderDsl {
                partitionHandler(partitionHandler)
                splitter("splitStep") { gridSize ->
                    ++partitionerCallCount
                    (0 until gridSize).map {
                        "$it" to ExecutionContext()
                    }.toMap()
                }
            }
            val jobExecution = JobExecution(jobInstance, jobParameters)
            val stepExecution = jobExecution.createStepExecution(step.name)
            step.execute(stepExecution)

            // then
            assertThat(partitionerCallCount).isEqualTo(1)
            assertThat(jobExecution.stepExecutions).hasSize(gridSize + 1)
            assertThat(jobExecution.stepExecutions).allSatisfy {
                assertThat(it.status).isEqualTo(BatchStatus.COMPLETED)
            }
            assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
            (0 until gridSize).forEach { gridNumber ->
                assertThat(jobExecution.stepExecutions.find { it.stepName == "splitStep:$gridNumber" }).isNotNull
            }
        }

        @Test
        fun testWithoutSplitter() {
            // given
            val partitionHandler = PartitionHandler { stepSplitter, stepExecution ->
                stepSplitter.split(stepExecution, 2_000_000_000)
                    .map {
                        it.apply {
                            exitStatus = ExitStatus.COMPLETED
                            status = BatchStatus.COMPLETED
                        }
                    }
            }

            // when, then
            assertThatThrownBy {
                partitionStepBuilderDsl {
                    partitionHandler(partitionHandler)
                }
            }.hasMessageContaining("splitter is not set")
        }
    }

    @Test
    fun testAggregator() {
        // given
        var aggregatorCallCount = 0

        // when
        val step = partitionStepBuilderDsl {
            aggregator { _, _ ->
                ++aggregatorCallCount
            }
            partitionHandler(mock<PartitionHandler>())
            splitter(mock())
        }
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(aggregatorCallCount).isEqualTo(1)
    }
}
