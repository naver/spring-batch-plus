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
import io.micrometer.observation.ObservationRegistry
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.core.listener.StepListener
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.StepInterruptionPolicy
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder
import org.springframework.batch.core.step.skip.SkipPolicy
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemStream
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.core.retry.RetryListener
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.interceptor.TransactionAttribute
import kotlin.reflect.KClass

/**
 * A dsl for [ChunkOrientedStepBuilder][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder].
 *
 * Only methods defined on `ChunkOrientedStepBuilder` itself are exposed here. Step-level lifecycle
 * members inherited from `StepBuilderHelper` are configured on
 * the outer [com.navercorp.spring.batch.plus.kotlin.configuration.StepBuilderDsl] before `chunk()` is called.
 * Method order matches the upstream source.
 *
 * @since 2.0.0
 */
@BatchDslMarker
class ChunkOrientedStepBuilderDsl<I : Any, O : Any> internal constructor(
    @Suppress("unused")
    private val dslContext: DslContext,
    private val chunkOrientedStepBuilder: ChunkOrientedStepBuilder<I, O>,
) {
    private val lazyConfigurer = LazyConfigurer<ChunkOrientedStepBuilder<I, O>>()

    /**
     * Set for [ChunkOrientedStepBuilder.reader][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.reader].
     */
    fun reader(reader: ItemReader<out I>) {
        this.lazyConfigurer.add {
            it.reader(reader)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.processor][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.processor].
     */
    fun processor(processor: ItemProcessor<in I, out O>) {
        this.lazyConfigurer.add {
            it.processor(processor)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.writer][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.writer].
     */
    fun writer(writer: ItemWriter<in O>) {
        this.lazyConfigurer.add {
            it.writer(writer)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.transactionManager][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.transactionManager].
     */
    fun transactionManager(transactionManager: PlatformTransactionManager) {
        this.lazyConfigurer.add {
            it.transactionManager(transactionManager)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.transactionAttribute][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.transactionAttribute].
     */
    fun transactionAttribute(transactionAttribute: TransactionAttribute) {
        this.lazyConfigurer.add {
            it.transactionAttribute(transactionAttribute)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.stream][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.stream].
     */
    fun stream(stream: ItemStream) {
        this.lazyConfigurer.add {
            it.stream(stream)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.listener][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.listener].
     */
    fun listener(listener: StepListener) {
        this.lazyConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set listener processing followings.
     *
     * - [org.springframework.batch.core.annotation.BeforeStep]
     * - [org.springframework.batch.core.annotation.AfterStep]
     * - [org.springframework.batch.core.annotation.BeforeChunk]
     * - [org.springframework.batch.core.annotation.AfterChunk]
     * - [org.springframework.batch.core.annotation.OnChunkError]
     * - [org.springframework.batch.core.annotation.BeforeRead]
     * - [org.springframework.batch.core.annotation.AfterRead]
     * - [org.springframework.batch.core.annotation.OnReadError]
     * - [org.springframework.batch.core.annotation.BeforeProcess]
     * - [org.springframework.batch.core.annotation.AfterProcess]
     * - [org.springframework.batch.core.annotation.OnProcessError]
     * - [org.springframework.batch.core.annotation.BeforeWrite]
     * - [org.springframework.batch.core.annotation.AfterWrite]
     * - [org.springframework.batch.core.annotation.OnWriteError]
     */
    fun listener(listener: Any) {
        this.lazyConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.interruptionPolicy][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.interruptionPolicy].
     */
    fun interruptionPolicy(interruptionPolicy: StepInterruptionPolicy) {
        this.lazyConfigurer.add {
            it.interruptionPolicy(interruptionPolicy)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.faultTolerant][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.faultTolerant].
     *
     * Must be called before any retry/skip configuration takes effect.
     */
    fun faultTolerant() {
        this.lazyConfigurer.add {
            it.faultTolerant()
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.retryPolicy][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.retryPolicy].
     */
    fun retryPolicy(policy: RetryPolicy) {
        this.lazyConfigurer.add {
            it.retryPolicy(policy)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.retryListener][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.retryListener].
     */
    fun retryListener(listener: RetryListener) {
        this.lazyConfigurer.add {
            it.retryListener(listener)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.retry][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.retry].
     */
    inline fun <reified T : Throwable> retry() {
        retry(T::class)
    }

    /**
     * Set for [ChunkOrientedStepBuilder.retry][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.retry].
     */
    fun retry(exceptionClass: KClass<out Throwable>) {
        this.lazyConfigurer.add {
            it.retry(exceptionClass.java)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.retryLimit][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.retryLimit].
     */
    fun retryLimit(retryLimit: Long) {
        this.lazyConfigurer.add {
            it.retryLimit(retryLimit)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.skipPolicy][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.skipPolicy].
     */
    fun skipPolicy(skipPolicy: SkipPolicy) {
        this.lazyConfigurer.add {
            it.skipPolicy(skipPolicy)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.skipListener][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.skipListener].
     */
    fun skipListener(skipListener: SkipListener<in I, in O>) {
        this.lazyConfigurer.add {
            it.skipListener(skipListener)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.skip][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.skip].
     */
    inline fun <reified T : Throwable> skip() {
        skip(T::class)
    }

    /**
     * Set for [ChunkOrientedStepBuilder.skip][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.skip].
     */
    fun skip(exceptionClass: KClass<out Throwable>) {
        this.lazyConfigurer.add {
            it.skip(exceptionClass.java)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.skipLimit][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.skipLimit].
     */
    fun skipLimit(skipLimit: Long) {
        this.lazyConfigurer.add {
            it.skipLimit(skipLimit)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.taskExecutor][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.taskExecutor].
     */
    fun taskExecutor(asyncTaskExecutor: AsyncTaskExecutor) {
        this.lazyConfigurer.add {
            it.taskExecutor(asyncTaskExecutor)
        }
    }

    /**
     * Set for [ChunkOrientedStepBuilder.observationRegistry][org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder.observationRegistry].
     */
    fun observationRegistry(observationRegistry: ObservationRegistry) {
        this.lazyConfigurer.add {
            it.observationRegistry(observationRegistry)
        }
    }

    internal fun build(): Step =
        this.chunkOrientedStepBuilder
            .apply(this.lazyConfigurer)
            .build()
}
