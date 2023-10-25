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
import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import com.navercorp.spring.batch.plus.kotlin.configuration.support.LazyConfigurer
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.step.builder.TaskletStepBuilder
import org.springframework.batch.item.ItemStream
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.exception.ExceptionHandler
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.interceptor.TransactionAttribute

/**
 * A dsl for [TaskletStepBuilder][org.springframework.batch.core.step.builder.TaskletStepBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class TaskletStepBuilderDsl internal constructor(
    @Suppress("unused")
    private val dslContext: DslContext,
    private val taskletStepBuilder: TaskletStepBuilder,
) {
    private val lazyConfigurer = LazyConfigurer<TaskletStepBuilder>()

    private var taskExecutorSet = false
    private var exceptionHandlerSet = false
    private var stepOperationsSet = false

    /**
     * Set for [TaskletStepBuilder.listener][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.listener].
     */
    fun listener(chunkListener: ChunkListener) {
        this.lazyConfigurer.add {
            it.listener(chunkListener)
        }
    }

    /**
     * Set listener processing followings.
     *
     * - [org.springframework.batch.core.annotation.BeforeChunk]
     * - [org.springframework.batch.core.annotation.AfterChunk]
     * - [org.springframework.batch.core.annotation.AfterChunkError]
     */
    fun listener(listener: Any) {
        this.lazyConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set for [TaskletStepBuilder.stream][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.stream].
     */
    fun stream(stream: ItemStream) {
        this.lazyConfigurer.add {
            it.stream(stream)
        }
    }

    /**
     * Set for [TaskletStepBuilder.taskExecutor][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.taskExecutor].
     * It can't be used when [stepOperations] is set.
     */
    fun taskExecutor(taskExecutor: TaskExecutor) {
        this.lazyConfigurer.add {
            it.taskExecutor(taskExecutor)
        }
        this.taskExecutorSet = true
    }

    // Maybe throttleLimit can be here. But throttleLimit is redundant in a tasklet step.

    /**
     * Set for [TaskletStepBuilder.exceptionHandler][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.exceptionHandler].
     * It can't be used when [stepOperations] is set.
     */
    fun exceptionHandler(exceptionHandler: ExceptionHandler) {
        this.lazyConfigurer.add {
            it.exceptionHandler(exceptionHandler)
        }
        this.exceptionHandlerSet = true
    }

    /**
     * Set for [TaskletStepBuilder.stepOperations][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.stepOperations].
     */
    fun stepOperations(repeatOperations: RepeatOperations) {
        this.lazyConfigurer.add {
            it.stepOperations(repeatOperations)
        }
        this.stepOperationsSet = true
    }

    /**
     * Set for [TaskletStepBuilder.transactionAttribute][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.transactionAttribute].
     */
    fun transactionAttribute(transactionAttribute: TransactionAttribute) {
        this.lazyConfigurer.add {
            it.transactionAttribute(transactionAttribute)
        }
    }

    internal fun build(): Step {
        // see org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.build
        if (this.stepOperationsSet) {
            check(!this.taskExecutorSet) {
                "taskExecutor is redundant when stepOperation is set."
            }
            check(!this.exceptionHandlerSet) {
                "exceptionHandler is redundant when stepOperation is set."
            }
        }

        return this.taskletStepBuilder.apply(this.lazyConfigurer)
            .build()
    }
}
