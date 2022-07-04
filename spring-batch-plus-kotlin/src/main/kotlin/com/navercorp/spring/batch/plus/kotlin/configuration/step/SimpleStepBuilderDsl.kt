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
import com.navercorp.spring.batch.plus.kotlin.configuration.support.CompositeConfigurer
import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.ItemProcessListener
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.core.ItemWriteListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.step.builder.SimpleStepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.exception.ExceptionHandler
import org.springframework.core.task.TaskExecutor
import org.springframework.transaction.interceptor.TransactionAttribute

/**
 * A dsl for [SimpleStepBuilder][org.springframework.batch.core.step.builder.SimpleStepBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class SimpleStepBuilderDsl<I : Any, O : Any> internal constructor(
    private val dslContext: DslContext,
    private var simpleStepBuilder: SimpleStepBuilder<I, O>
) {
    private val compositeConfigurer = CompositeConfigurer<SimpleStepBuilder<I, O>>()

    private var taskExecutorSet = false
    private var throttleLimitSet = false
    private var exceptionHandlerSet = false
    private var stepOperationsSet = false

    /**
     * Set for [SimpleStepBuilder.reader][org.springframework.batch.core.step.builder.SimpleStepBuilder.reader].
     */
    fun reader(reader: ItemReader<out I>) {
        this.compositeConfigurer.add {
            it.reader(reader)
        }
    }

    /**
     * Set for [SimpleStepBuilder.writer][org.springframework.batch.core.step.builder.SimpleStepBuilder.writer].
     */
    fun writer(writer: ItemWriter<in O>) {
        this.compositeConfigurer.add {
            it.writer(writer)
        }
    }

    /**
     * Set for [SimpleStepBuilder.processor][org.springframework.batch.core.step.builder.SimpleStepBuilder.processor].
     */
    fun processor(processor: ItemProcessor<in I, out O>) {
        this.compositeConfigurer.add {
            it.processor(processor)
        }
    }

    /**
     * Set for [SimpleStepBuilder.readerIsTransactionalQueue][org.springframework.batch.core.step.builder.SimpleStepBuilder.readerIsTransactionalQueue].
     */
    fun readerIsTransactionalQueue() {
        this.compositeConfigurer.add {
            it.readerIsTransactionalQueue()
        }
    }

    /**
     * Set listener processing followings.
     *
     * - [org.springframework.batch.core.annotation.BeforeChunk]
     * - [org.springframework.batch.core.annotation.AfterChunk]
     * - [org.springframework.batch.core.annotation.AfterChunkError]
     * - [org.springframework.batch.core.annotation.BeforeRead]
     * - [org.springframework.batch.core.annotation.AfterRead]
     * - [org.springframework.batch.core.annotation.BeforeProcess]
     * - [org.springframework.batch.core.annotation.AfterProcess]
     * - [org.springframework.batch.core.annotation.BeforeWrite]
     * - [org.springframework.batch.core.annotation.AfterWrite]
     * - [org.springframework.batch.core.annotation.OnReadError]
     * - [org.springframework.batch.core.annotation.OnProcessError]
     * - [org.springframework.batch.core.annotation.OnWriteError]
     */
    fun listener(listener: Any) {
        this.compositeConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set item read listener.
     */
    fun listener(listener: ItemReadListener<in I>) {
        this.compositeConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set item write listener.
     */
    fun listener(listener: ItemWriteListener<in O>) {
        this.compositeConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set item process listener.
     */
    fun listener(listener: ItemProcessListener<in I, in O>) {
        this.compositeConfigurer.add {
            it.listener(listener)
        }
    }

    // from AbstractTaskletStepBuilder.xxx

    /**
     * Set for [SimpleStepBuilder.listener][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.listener].
     */
    fun listener(chunkListener: ChunkListener) {
        this.compositeConfigurer.add {
            it.listener(chunkListener)
        }
    }

    /**
     * Set for [SimpleStepBuilder.stream][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.stream].
     */
    fun stream(stream: ItemStream) {
        this.compositeConfigurer.add {
            it.stream(stream)
        }
    }

    /**
     * Set for [SimpleStepBuilder.taskExecutor][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.taskExecutor].
     * It can't be used when [stepOperations] is set.
     */
    fun taskExecutor(taskExecutor: TaskExecutor) {
        this.compositeConfigurer.add {
            it.taskExecutor(taskExecutor)
        }
        this.taskExecutorSet = true
    }

    /**
     * Set for [SimpleStepBuilder.throttleLimit][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.throttleLimit].
     * If not present, set as default value of [AbstractTaskletStepBuilder][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder].
     * It can't be used when no [taskExecutor] is set.
     */
    fun throttleLimit(throttleLimit: Int) {
        this.compositeConfigurer.add {
            it.throttleLimit(throttleLimit)
        }
        this.throttleLimitSet = true
    }

    /**
     * Set for [SimpleStepBuilder.exceptionHandler][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.exceptionHandler].
     * It can't be used when [stepOperations] is set.
     */
    fun exceptionHandler(exceptionHandler: ExceptionHandler) {
        this.compositeConfigurer.add {
            it.exceptionHandler(exceptionHandler)
        }
        this.exceptionHandlerSet = true
    }

    /**
     * Set for [SimpleStepBuilder.stepOperations][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.stepOperations].
     */
    fun stepOperations(repeatOperations: RepeatOperations) {
        this.compositeConfigurer.add {
            it.stepOperations(repeatOperations)
        }
        this.stepOperationsSet = true
    }

    /**
     * Set for [SimpleStepBuilder.transactionAttribute][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.transactionAttribute].
     */
    fun transactionAttribute(transactionAttribute: TransactionAttribute) {
        this.compositeConfigurer.add {
            it.transactionAttribute(transactionAttribute)
        }
    }

    // see org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.build
    internal fun build(): Step {
        if (this.stepOperationsSet) {
            check(!this.taskExecutorSet) {
                "taskExecutor is redundant when stepOperation is set."
            }
            check(!this.exceptionHandlerSet) {
                "exceptionHandler is redundant when stepOperation is set."
            }
        }

        if (!this.taskExecutorSet) {
            check(!this.throttleLimitSet) {
                "throttleLimit is redundant when no taskExecutor is set."
            }
        }

        return this.simpleStepBuilder.apply(this.compositeConfigurer)
            .build()
    }
}
