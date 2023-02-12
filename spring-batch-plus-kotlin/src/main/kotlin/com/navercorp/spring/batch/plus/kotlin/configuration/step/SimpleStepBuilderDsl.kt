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
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.ItemProcessListener
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.core.ItemWriteListener
import org.springframework.batch.core.SkipListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder
import org.springframework.batch.core.step.builder.SimpleStepBuilder
import org.springframework.batch.core.step.item.KeyGenerator
import org.springframework.batch.core.step.skip.SkipPolicy
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.repeat.RepeatOperations
import org.springframework.batch.repeat.exception.ExceptionHandler
import org.springframework.core.task.TaskExecutor
import org.springframework.retry.RetryListener
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.BackOffPolicy
import org.springframework.retry.policy.RetryContextCache
import org.springframework.transaction.interceptor.TransactionAttribute
import kotlin.reflect.KClass

/**
 * A dsl for [SimpleStepBuilder][org.springframework.batch.core.step.builder.SimpleStepBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class SimpleStepBuilderDsl<I : Any, O : Any> internal constructor(
    private val dslContext: DslContext,
    private val simpleStepBuilder: SimpleStepBuilder<I, O>
) {
    private val simpleStepConfigurer = LazyConfigurer<SimpleStepBuilder<I, O>>()
    private var faultTolerantStepConfigurer: Configurer<FaultTolerantStepBuilder<I, O>>? = null

    private var taskExecutorSet = false
    private var throttleLimitSet = false
    private var exceptionHandlerSet = false
    private var stepOperationsSet = false

    /**
     * Set faultTolerant config.
     *
     * @see [FaultTolerantStepBuilder][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder].
     */
    fun faultTolerant(init: FaultTolerantStepBuilderDsl<I, O>.() -> Unit) {
        this.faultTolerantStepConfigurer = FaultTolerantStepBuilderDsl<I, O>(this.dslContext)
            .apply(init)
            .build()
    }

    /**
     * Set for [SimpleStepBuilder.reader][org.springframework.batch.core.step.builder.SimpleStepBuilder.reader].
     */
    fun reader(reader: ItemReader<out I>) {
        this.simpleStepConfigurer.add {
            it.reader(reader)
        }
    }

    /**
     * Set for [SimpleStepBuilder.writer][org.springframework.batch.core.step.builder.SimpleStepBuilder.writer].
     */
    fun writer(writer: ItemWriter<in O>) {
        this.simpleStepConfigurer.add {
            it.writer(writer)
        }
    }

    /**
     * Set for [SimpleStepBuilder.processor][org.springframework.batch.core.step.builder.SimpleStepBuilder.processor].
     */
    fun processor(processor: ItemProcessor<in I, out O>) {
        this.simpleStepConfigurer.add {
            it.processor(processor)
        }
    }

    /**
     * Set for [SimpleStepBuilder.readerIsTransactionalQueue][org.springframework.batch.core.step.builder.SimpleStepBuilder.readerIsTransactionalQueue].
     */
    fun readerIsTransactionalQueue() {
        this.simpleStepConfigurer.add {
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
     *
     * If faultTolerant is set, also process followings.
     *
     * - [org.springframework.batch.core.annotation.OnSkipInRead]
     * - [org.springframework.batch.core.annotation.OnSkipInProcess]
     * - [org.springframework.batch.core.annotation.OnSkipInWrite]
     */
    fun listener(listener: Any) {
        this.simpleStepConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set item read listener.
     */
    fun listener(listener: ItemReadListener<in I>) {
        this.simpleStepConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set item write listener.
     */
    fun listener(listener: ItemWriteListener<in O>) {
        this.simpleStepConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set item process listener.
     */
    fun listener(listener: ItemProcessListener<in I, in O>) {
        this.simpleStepConfigurer.add {
            it.listener(listener)
        }
    }

    // from AbstractTaskletStepBuilder.xxx

    /**
     * Set for [SimpleStepBuilder.listener][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.listener].
     */
    fun listener(chunkListener: ChunkListener) {
        this.simpleStepConfigurer.add {
            it.listener(chunkListener)
        }
    }

    /**
     * Set for [SimpleStepBuilder.stream][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.stream].
     */
    fun stream(stream: ItemStream) {
        this.simpleStepConfigurer.add {
            it.stream(stream)
        }
    }

    /**
     * Set for [SimpleStepBuilder.taskExecutor][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.taskExecutor].
     * It can't be used when [stepOperations] is set.
     */
    fun taskExecutor(taskExecutor: TaskExecutor) {
        this.simpleStepConfigurer.add {
            it.taskExecutor(taskExecutor)
        }
        this.taskExecutorSet = true
    }

    /**
     * Set for [SimpleStepBuilder.throttleLimit][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.throttleLimit].
     * If not present, set as default value of [AbstractTaskletStepBuilder][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder].
     * It can't be used when no [taskExecutor] is set.
     */
    @Suppress("DEPRECATION")
    @Deprecated(
        message = "spring batch 5.0.0 deprecates this",
    )
    fun throttleLimit(throttleLimit: Int) {
        this.simpleStepConfigurer.add {
            it.throttleLimit(throttleLimit)
        }
        this.throttleLimitSet = true
    }

    /**
     * Set for [SimpleStepBuilder.exceptionHandler][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.exceptionHandler].
     * It can't be used when [stepOperations] is set.
     */
    fun exceptionHandler(exceptionHandler: ExceptionHandler) {
        this.simpleStepConfigurer.add {
            it.exceptionHandler(exceptionHandler)
        }
        this.exceptionHandlerSet = true
    }

    /**
     * Set for [SimpleStepBuilder.stepOperations][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.stepOperations].
     */
    fun stepOperations(repeatOperations: RepeatOperations) {
        this.simpleStepConfigurer.add {
            it.stepOperations(repeatOperations)
        }
        this.stepOperationsSet = true
    }

    /**
     * Set for [SimpleStepBuilder.transactionAttribute][org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder.transactionAttribute].
     */
    fun transactionAttribute(transactionAttribute: TransactionAttribute) {
        this.simpleStepConfigurer.add {
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

        val faultTolerantStepConfigurer = this.faultTolerantStepConfigurer
        val simpleStepConfigurer = this.simpleStepConfigurer
        return if (faultTolerantStepConfigurer != null) {
            this.simpleStepBuilder.faultTolerant()
                .apply(faultTolerantStepConfigurer)
                .apply(simpleStepConfigurer)
                .build()
        } else {
            this.simpleStepBuilder.apply(simpleStepConfigurer)
                .build()
        }
    }

    /**
     * A dsl for [FaultTolerantStepBuilder][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder].
     *
     * @since 0.1.0
     */
    @BatchDslMarker
    class FaultTolerantStepBuilderDsl<I : Any, O : Any> internal constructor(
        @Suppress("unused")
        private val dslContext: DslContext,
    ) {
        private val lazyConfigurer = LazyConfigurer<FaultTolerantStepBuilder<I, O>>()

        /**
         * Set skip listener.
         */
        fun listener(listener: SkipListener<in I, in O>) {
            this.lazyConfigurer.add {
                it.listener(listener)
            }
        }

        /**
         * Set retry listener.
         */
        fun listener(listener: RetryListener) {
            this.lazyConfigurer.add {
                it.listener(listener)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.keyGenerator][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.keyGenerator].
         */
        fun keyGenerator(keyGenerator: KeyGenerator) {
            this.lazyConfigurer.add {
                it.keyGenerator(keyGenerator)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.retryLimit][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.retryLimit].
         */
        fun retryLimit(retryLimit: Int) {
            this.lazyConfigurer.add {
                it.retryLimit(retryLimit)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.noRetry][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.noRetry].
         */
        inline fun <reified T : Throwable> noRetry() {
            noRetry(T::class)
        }

        /**
         * Set for [FaultTolerantStepBuilder.noRetry][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.noRetry].
         */
        fun noRetry(type: KClass<out Throwable>) {
            this.lazyConfigurer.add {
                it.noRetry(type.java)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.retry][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.retry].
         */
        inline fun <reified T : Throwable> retry() {
            retry(T::class)
        }

        /**
         * Set for [FaultTolerantStepBuilder.retry][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.retry].
         */
        fun retry(type: KClass<out Throwable>) {
            this.lazyConfigurer.add {
                it.retry(type.java)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.retryPolicy][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.retryPolicy].
         */
        fun retryPolicy(retryPolicy: RetryPolicy) {
            this.lazyConfigurer.add {
                it.retryPolicy(retryPolicy)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.backOffPolicy][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.backOffPolicy].
         */
        fun backOffPolicy(backOffPolicy: BackOffPolicy) {
            this.lazyConfigurer.add {
                it.backOffPolicy(backOffPolicy)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.retryContextCache][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.retryContextCache].
         */
        fun retryContextCache(retryContextCache: RetryContextCache) {
            this.lazyConfigurer.add {
                it.retryContextCache(retryContextCache)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.skipLimit][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.skipLimit].
         */
        fun skipLimit(skipLimit: Int) {
            this.lazyConfigurer.add {
                it.skipLimit(skipLimit)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.noSkip][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.noSkip].
         */
        inline fun <reified T : Throwable> noSkip() {
            noSkip(T::class)
        }

        /**
         * Set for [FaultTolerantStepBuilder.noSkip][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.noSkip].
         */
        fun noSkip(type: KClass<out Throwable>) {
            this.lazyConfigurer.add {
                it.noSkip(type.java)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.skip][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.skip].
         */
        inline fun <reified T : Throwable> skip() {
            skip(T::class)
        }

        /**
         * Set for [FaultTolerantStepBuilder.skip][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.skip].
         */
        fun skip(type: KClass<out Throwable>) {
            this.lazyConfigurer.add {
                it.skip(type.java)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.skipPolicy][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.skipPolicy].
         */
        fun skipPolicy(skipPolicy: SkipPolicy) {
            this.lazyConfigurer.add {
                it.skipPolicy(skipPolicy)
            }
        }

        /**
         * Set for [FaultTolerantStepBuilder.noRollback][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.noRollback].
         */
        inline fun <reified T : Throwable> noRollback() {
            noRollback(T::class)
        }

        /**
         * Set for [FaultTolerantStepBuilder.noRollback][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.noRollback].
         */
        fun noRollback(type: KClass<out Throwable>) {
            this.lazyConfigurer.add {
                it.noRollback(type.java)
            }
        }

        /**
         * Cache processor result between retries and during skip processing.
         *
         * @see [FaultTolerantStepBuilder.processorNonTransactional][org.springframework.batch.core.step.builder.FaultTolerantStepBuilder.processorNonTransactional].
         */
        fun processorNonTransactional() {
            this.lazyConfigurer.add {
                it.processorNonTransactional()
            }
        }

        internal fun build(): Configurer<FaultTolerantStepBuilder<I, O>> {
            return this.lazyConfigurer
        }
    }
}
