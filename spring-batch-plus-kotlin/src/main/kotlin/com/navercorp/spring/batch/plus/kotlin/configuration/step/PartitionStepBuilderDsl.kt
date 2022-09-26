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

import com.navercorp.spring.batch.plus.kotlin.configuration.support.BatchDslMarker
import com.navercorp.spring.batch.plus.kotlin.configuration.support.Configurer
import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import com.navercorp.spring.batch.plus.kotlin.configuration.support.LazyConfigurer
import org.springframework.batch.core.Step
import org.springframework.batch.core.partition.PartitionHandler
import org.springframework.batch.core.partition.StepExecutionSplitter
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.core.partition.support.StepExecutionAggregator
import org.springframework.batch.core.step.builder.PartitionStepBuilder
import org.springframework.core.task.TaskExecutor

/**
 * A dsl for [PartitionStepBuilder][org.springframework.batch.core.step.builder.PartitionStepBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class PartitionStepBuilderDsl internal constructor(
    private val dslContext: DslContext,
    private val partitionStepBuilder: PartitionStepBuilder,
) {
    private val lazyConfigurer = LazyConfigurer<PartitionStepBuilder>()

    private var partitionHandlerSet = false
    private var splitterSet = false

    /**
     * Set for [PartitionStepBuilder.partitionHandler][org.springframework.batch.core.step.builder.PartitionStepBuilder.partitionHandler].
     */
    fun partitionHandler(partitionHandler: PartitionHandler) {
        this.lazyConfigurer.add {
            it.partitionHandler(partitionHandler)
        }
        this.partitionHandlerSet = true
    }

    /**
     * Build [TaskExecutorPartitionHandler][org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler]
     * for [PartitionHandler][org.springframework.batch.core.partition.PartitionHandler].
     */
    fun partitionHandler(init: TaskExecutorPartitionHandlerBuilderDsl.() -> Unit) {
        val taskExecutorPartitionHandlerConfigurers = TaskExecutorPartitionHandlerBuilderDsl(this.dslContext)
            .apply(init)
            .build()
        this.lazyConfigurer.add(taskExecutorPartitionHandlerConfigurers)
        this.partitionHandlerSet = true
    }

    /**
     * Set for [PartitionStepBuilder.splitter][org.springframework.batch.core.step.builder.PartitionStepBuilder.splitter].
     */
    fun splitter(splitter: StepExecutionSplitter) {
        this.lazyConfigurer.add {
            it.splitter(splitter)
        }
        this.splitterSet = true
    }

    /**
     * Build [SimpleStepExecutionSplitter][org.springframework.batch.core.partition.support.SimpleStepExecutionSplitter]
     * for [StepExecutionSplitter][org.springframework.batch.core.partition.StepExecutionSplitter].
     *
     * @see [PartitionStepBuilder.partitioner][org.springframework.batch.core.step.builder.PartitionStepBuilder.partitioner]
     */
    fun splitter(stepName: String, partitioner: Partitioner) {
        this.lazyConfigurer.add {
            it.partitioner(stepName, partitioner)
        }
        this.splitterSet = true
    }

    /**
     * Set for [PartitionStepBuilder.aggregator][org.springframework.batch.core.step.builder.PartitionStepBuilder.aggregator].
     */
    fun aggregator(aggregator: StepExecutionAggregator) {
        this.lazyConfigurer.add {
            it.aggregator(aggregator)
        }
    }

    internal fun build(): Step {
        // see org.springframework.batch.core.step.builder.PartitionStepBuilder.build
        check(this.partitionHandlerSet) {
            "partitionHandler is not set."
        }
        check(this.splitterSet) {
            "splitter is not set."
        }

        return this.partitionStepBuilder.apply(this.lazyConfigurer)
            .build()
    }

    /**
     * A dsl for building [TaskExecutorPartitionHandler][org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler].
     *
     * @since 0.1.0
     */
    @BatchDslMarker
    class TaskExecutorPartitionHandlerBuilderDsl internal constructor(
        @Suppress("unused")
        private val dslContext: DslContext,
    ) {
        private val lazyConfigurer = LazyConfigurer<PartitionStepBuilder>()

        private var stepSet = false

        /**
         * Set step to be used in building [TaskExecutorPartitionHandler][org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler].
         * It must be present.
         *
         * @see [org.springframework.batch.core.step.builder.PartitionStepBuilder.step]
         */
        fun step(step: Step) {
            this.lazyConfigurer.add {
                it.step(step)
            }
            this.stepSet = true
        }

        /**
         * Set taskExecutor to be used in building [TaskExecutorPartitionHandler][org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler].
         * If not present, [SyncTaskExecutor][org.springframework.core.task.SyncTaskExecutor] is used.
         *
         * @see [org.springframework.batch.core.step.builder.PartitionStepBuilder.taskExecutor]
         */
        fun taskExecutor(taskExecutor: TaskExecutor) {
            this.lazyConfigurer.add {
                it.taskExecutor(taskExecutor)
            }
        }

        /**
         * Set gridSize to be used in making [TaskExecutorPartitionHandler][org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler].
         * If not present, set as a default gridSize of [PartitionStepBuilder][org.springframework.batch.core.step.builder.PartitionStepBuilder].
         *
         * @see [org.springframework.batch.core.step.builder.PartitionStepBuilder.gridSize]
         */
        fun gridSize(gridSize: Int) {
            this.lazyConfigurer.add {
                it.gridSize(gridSize)
            }
        }

        // see org.springframework.batch.core.step.builder.PartitionStepBuilder.build
        internal fun build(): Configurer<PartitionStepBuilder> {
            check(this.stepSet) { "step is not set." }

            return this.lazyConfigurer
        }
    }
}
