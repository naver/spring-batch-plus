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

@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.navercorp.spring.batch.plus.kotlin.configuration.step

import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.annotation.OnSkipInRead
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.listener.ChunkListener
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.FatalStepExecutionException
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemStream
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.MapRetryContextCache
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.transaction.interceptor.DefaultTransactionAttribute

/**
 * Covers fault-tolerant behavior retained by the deprecated simple-step DSL and ordering-sensitive builder overrides.
 */
internal class FaultTolerantStepBuilderDslTest {
    private val jobInstance = JobInstance(0L, "testJob")

    private val jobParameters = JobParameters()

    @Test
    fun skipListenerShouldReceiveCallbackWhenSkippableReadFailureOccurs() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val skipLimit = 3
        var readCallCount = 0
        var tryCount = 0
        var onSkipInReadCallCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (tryCount < skipLimit) {
                        ++tryCount
                        throw IllegalStateException("Error")
                    }

                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {}
                faultTolerant {
                    skipLimit(skipLimit)
                    skip<IllegalStateException>()
                    listener(
                        object : SkipListener<Number, Number> {
                            override fun onSkipInRead(t: Throwable) {
                                ++onSkipInReadCallCount
                            }

                            override fun onSkipInProcess(
                                item: Number,
                                t: Throwable,
                            ) {
                                // no need to test. we are just testing if listener is invoked
                            }

                            override fun onSkipInWrite(
                                item: Number,
                                t: Throwable,
                            ) {
                                // no need to test. we are just testing if listener is invoked
                            }
                        },
                    )
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(tryCount).isEqualTo(skipLimit)
        assertThat(onSkipInReadCallCount).isEqualTo(skipLimit)
    }

    @Test
    fun retryListenerShouldBeInvokedWhenWriterRetryOccurs() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var retryOpenCallCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retry<IllegalStateException>()
                    listener(
                        object : RetryListener {
                            override fun <T : Any?, E : Throwable?> open(
                                context: RetryContext?,
                                callback: RetryCallback<T, E>?,
                            ): Boolean {
                                ++retryOpenCallCount
                                return true
                            }

                            override fun <T : Any?, E : Throwable?> close(
                                context: RetryContext?,
                                callback: RetryCallback<T, E>?,
                                throwable: Throwable?,
                            ) {
                                // no need to test. we are just testing if listener is invoked
                            }

                            override fun <T : Any?, E : Throwable?> onError(
                                context: RetryContext?,
                                callback: RetryCallback<T, E>?,
                                throwable: Throwable?,
                            ) {
                                // no need to test. we are just testing if listener is invoked
                            }
                        },
                    )
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(retryOpenCallCount).isGreaterThan(0)
    }

    @Test
    fun keyGeneratorShouldBeInvokedWhenWriterRetryOccurs() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var keyGeneratorCallCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retry<IllegalStateException>()
                    keyGenerator {
                        ++keyGeneratorCallCount
                        "testkey"
                    }
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(keyGeneratorCallCount).isGreaterThan(0)
    }

    @Test
    fun retryLimitShouldBoundAttemptsWhenWriterKeepsFailing() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    ++tryCount
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retry<RuntimeException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(tryCount).isEqualTo(retryLimit)
    }

    @Test
    fun retryShouldRetryMatchingExceptionWhenWriterFails() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    ++tryCount
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retry<RuntimeException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(tryCount).isEqualTo(retryLimit)
    }

    @Test
    fun noRetryShouldPreventRetryWhenExceptionIsExcluded() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    ++tryCount
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retry<RuntimeException>()
                    noRetry<IllegalStateException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(tryCount).isEqualTo(1)
    }

    @Test
    fun retryPolicyShouldControlAttemptsWhenWriterKeepsFailing() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    ++tryCount
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryPolicy(SimpleRetryPolicy(retryLimit))
                    retry<RuntimeException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(tryCount).isEqualTo(retryLimit)
    }

    @Test
    fun backOffPolicyShouldBeInvokedWhenWriterRetryOccurs() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var tryCount = 0
        var backoffPolicyCallCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    ++tryCount
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    backOffPolicy(
                        object : FixedBackOffPolicy() {
                            override fun doBackOff() {
                                ++backoffPolicyCallCount
                                super.doBackOff()
                            }
                        },
                    )
                    retry<RuntimeException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(tryCount).isEqualTo(retryLimit)
        assertThat(backoffPolicyCallCount).isGreaterThan(0)
    }

    @Test
    fun retryContextCacheShouldBeUsedWhenWriterRetryOccurs() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var tryCount = 0
        var retryContextCacheCallCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    ++tryCount
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retryContextCache(
                        object : MapRetryContextCache() {
                            override fun containsKey(key: Any?): Boolean {
                                ++retryContextCacheCallCount
                                return super.containsKey(key)
                            }
                        },
                    )
                    retry<RuntimeException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(readCallCount).isEqualTo(chunkSize)
        assertThat(tryCount).isEqualTo(retryLimit)
        assertThat(retryContextCacheCallCount).isGreaterThan(0)
    }

    @Test
    fun skipLimitShouldAllowConfiguredNumberOfReadFailuresWhenExceptionIsSkippable() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val skipLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (tryCount < skipLimit) {
                        ++tryCount
                        throw IllegalStateException("Error")
                    }

                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer { }
                faultTolerant {
                    skipLimit(skipLimit)
                    skip<IllegalStateException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(tryCount).isEqualTo(skipLimit)
    }

    @Test
    fun skipShouldSkipMatchingExceptionWhenReadFails() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val skipLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (tryCount < skipLimit) {
                        ++tryCount
                        throw IllegalStateException("Error")
                    }

                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer { }
                faultTolerant {
                    skipLimit(skipLimit)
                    skip<IllegalStateException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(tryCount).isEqualTo(skipLimit)
    }

    @Test
    fun noSkipShouldFailStepWithoutSkippingWhenExceptionIsExcluded() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val skipLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (tryCount < skipLimit) {
                        ++tryCount
                        throw IllegalStateException("Error")
                    }

                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer { }
                faultTolerant {
                    skipLimit(skipLimit)
                    skip<RuntimeException>()
                    noSkip<IllegalStateException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(tryCount).isEqualTo(1)
        assertThat(readCallCount).isEqualTo(0)
    }

    @Test
    fun skipPolicyShouldAllowReadFailuresWhenPolicyAcceptsException() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val skipLimit = 3
        var readCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (tryCount < skipLimit) {
                        ++tryCount
                        throw IllegalStateException("Error")
                    }

                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer { }
                faultTolerant {
                    skipPolicy(
                        LimitCheckingItemSkipPolicy(
                            skipLimit,
                            mapOf(IllegalStateException::class.java to true),
                        ),
                    )
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(tryCount).isEqualTo(skipLimit)
    }

    @Test
    fun noRollbackShouldCompleteStepWhenWriterThrowsExcludedException() {
        // given
        val chunkSize = 3
        val readLimit = 20
        var readCallCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
                reader {
                    if (readCallCount < readLimit) {
                        ++readCallCount
                        1
                    } else {
                        null
                    }
                }
                writer {
                    // ignored when noRollback is set
                    throw IllegalStateException("Error")
                }
                faultTolerant {
                    noRollback<IllegalStateException>()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
    }

    @Test
    fun processorNonTransactionalShouldAvoidReprocessingItemsWhenWriterRetries() {
        // given
        val chunkSize = 3
        val readLimit = 20
        val retryLimit = 3
        var readCallCount = 0
        var processCallCount = 0
        var tryCount = 0

        // when
        val step =
            simpleStepBuilderDsl<Int, Int>(chunkSize) {
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
                    if (tryCount < (retryLimit - 1)) {
                        ++tryCount
                        throw IllegalStateException("Error")
                    }
                }
                faultTolerant {
                    retryLimit(retryLimit)
                    retry<IllegalStateException>()
                    processorNonTransactional()
                }
            }
        val jobExecution = JobExecution(0L, jobInstance, jobParameters)
        val stepExecution = StepExecution(step.name, jobExecution)
        step.execute(stepExecution)

        // then
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        // without processorNonTransactional, it would be 26 (readLimit + chunkSize * retryCount)
        assertThat(processCallCount).isEqualTo(readLimit)
    }

    /**
     * Covers ordering compatibility for options whose delegate changes after fault tolerance is enabled.
     */
    @Nested
    inner class OverriddenMethodTest {
        @Test
        fun objectListenerShouldNotReceiveSkipCallbackWhenRegisteredBeforeFaultTolerantOnDelegate() {
            // given
            val chunkSize = 1
            val readLimit = 3
            val skipLimit = 1
            var readCallCount = 0
            var tryCount = 0
            var onSkipInReadCallCount = 0

            val stepBuilder = StepBuilder("testStep", mockk(relaxed = true))
            val simpleStepBuilder = TestBuilderBridge.simpleStepBuilder<Int, Int>(stepBuilder)

            class TestListener {
                @Suppress("unused")
                @OnSkipInRead
                fun onSkipInRead() {
                    ++onSkipInReadCallCount
                }
            }

            // when
            val step =
                simpleStepBuilder
                    .chunk(chunkSize)
                    .transactionManager(ResourcelessTransactionManager())
                    .listener(TestListener()) // called before faultTolerant()
                    .reader {
                        if (tryCount < skipLimit) {
                            ++tryCount
                            throw IllegalStateException("Error")
                        }

                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }.writer {}
                    .faultTolerant()
                    .skipLimit(skipLimit)
                    .skip(IllegalStateException::class.java)
                    .build()
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(tryCount).isEqualTo(skipLimit)
            assertThat(onSkipInReadCallCount).isEqualTo(0) // never called
        }

        @Test
        fun objectListenerShouldReceiveSkipCallbackWhenRegisteredAfterFaultTolerantOnDelegate() {
            // given
            val chunkSize = 1
            val readLimit = 3
            val skipLimit = 1
            var readCallCount = 0
            var tryCount = 0
            var onSkipInReadCallCount = 0

            val stepBuilder = StepBuilder("testStep", mockk(relaxed = true))
            val simpleStepBuilder = TestBuilderBridge.simpleStepBuilder<Int, Int>(stepBuilder)

            class TestListener {
                @Suppress("unused")
                @OnSkipInRead
                fun onSkipInRead() {
                    ++onSkipInReadCallCount
                }
            }

            // when
            val step =
                simpleStepBuilder
                    .chunk(chunkSize)
                    .transactionManager(ResourcelessTransactionManager())
                    .reader {
                        if (tryCount < skipLimit) {
                            ++tryCount
                            throw IllegalStateException("Error")
                        }

                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }.writer {}
                    .faultTolerant()
                    .skipLimit(skipLimit)
                    .skip(IllegalStateException::class.java)
                    .listener(TestListener()) // called after faultTolerant()
                    .build()
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(tryCount).isEqualTo(skipLimit)
            assertThat(onSkipInReadCallCount).isEqualTo(skipLimit) // called
        }

        @Test
        fun objectListenerShouldReceiveSkipCallbackWhenRegisteredBeforeFaultTolerantOnDsl() {
            // given
            val chunkSize = 1
            val readLimit = 3
            val skipLimit = 1
            var readCallCount = 0
            var tryCount = 0
            var onSkipInReadCallCount = 0

            class TestListener {
                @Suppress("unused")
                @OnSkipInRead
                fun onSkipInRead() {
                    ++onSkipInReadCallCount
                }
            }

            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(chunkSize) {
                    listener(TestListener()) // called before faultTolerant
                    reader {
                        if (tryCount < skipLimit) {
                            ++tryCount
                            throw IllegalStateException("Error")
                        }

                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }
                    writer {}
                    faultTolerant {
                        skipLimit(skipLimit)
                        skip(IllegalStateException::class)
                    }
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(tryCount).isEqualTo(skipLimit)
            assertThat(onSkipInReadCallCount).isEqualTo(tryCount)
        }

        @Test
        fun objectListenerShouldReceiveSkipCallbackWhenRegisteredAfterFaultTolerantOnDsl() {
            // given
            val chunkSize = 1
            val readLimit = 3
            val skipLimit = 1
            var readCallCount = 0
            var tryCount = 0
            var onSkipInReadCallCount = 0

            class TestListener {
                @Suppress("unused")
                @OnSkipInRead
                fun onSkipInRead() {
                    ++onSkipInReadCallCount
                }
            }

            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(chunkSize) {
                    reader {
                        if (tryCount < skipLimit) {
                            ++tryCount
                            throw IllegalStateException("Error")
                        }

                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }
                    writer {}
                    faultTolerant {
                        skipLimit(skipLimit)
                        skip(IllegalStateException::class)
                    }
                    listener(TestListener()) // called before faultTolerant
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(tryCount).isEqualTo(skipLimit)
            assertThat(onSkipInReadCallCount).isEqualTo(tryCount)
        }

        @Test
        fun chunkListenerShouldBeInvokedWhenRegisteredAfterFaultTolerantOnDelegate() {
            // given
            val stepBuilder = StepBuilder("testStep", mockk(relaxed = true))
            val simpleStepBuilder = TestBuilderBridge.simpleStepBuilder<Int, Int>(stepBuilder)

            // when
            val step =
                simpleStepBuilder
                    .chunk(3)
                    .transactionManager(ResourcelessTransactionManager())
                    .reader { null }
                    .faultTolerant()
                    .retryLimit(3)
                    .retry(RuntimeException::class.java)
                    .writer {}
                    .listener(
                        object : ChunkListener<Int, Int> {
                            override fun beforeChunk(context: ChunkContext): Unit = throw IllegalStateException("Error")

                            override fun afterChunk(context: ChunkContext) {
                            }

                            override fun afterChunkError(context: ChunkContext) {
                            }
                        },
                    ).build()
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
            assertThat(stepExecution.failureExceptions).anyMatch {
                it is FatalStepExecutionException
            }
        }

        @Test
        fun chunkListenerShouldBeInvokedWhenRegisteredBeforeFaultTolerantOnDsl() {
            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(3) {
                    reader { null }
                    writer {}
                    listener(
                        object : ChunkListener<Int, Int> {
                            override fun beforeChunk(context: ChunkContext): Unit = throw IllegalStateException("Error")

                            override fun afterChunk(context: ChunkContext) {
                            }

                            override fun afterChunkError(context: ChunkContext) {
                            }
                        },
                    )
                    faultTolerant {
                        retryLimit(3)
                        retry<RuntimeException>()
                    }
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
            assertThat(stepExecution.failureExceptions).anyMatch {
                it is FatalStepExecutionException
            }
        }

        @Test
        fun chunkListenerShouldBeInvokedWhenRegisteredAfterFaultTolerantOnDsl() {
            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(3) {
                    reader { null }
                    writer {}
                    faultTolerant {
                        retryLimit(3)
                        retry<RuntimeException>()
                    }
                    listener(
                        object : ChunkListener<Int, Int> {
                            override fun beforeChunk(context: ChunkContext): Unit = throw IllegalStateException("Error")

                            override fun afterChunk(context: ChunkContext) {
                            }

                            override fun afterChunkError(context: ChunkContext) {
                            }
                        },
                    )
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
            assertThat(stepExecution.failureExceptions).anyMatch {
                it is FatalStepExecutionException
            }
        }

        @Test
        fun transactionAttributeShouldHonorNoRollbackWhenConfiguredAfterFaultTolerantOnDelegate() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var readCallCount = 0
            var noRollbackCallCount = 0

            val stepBuilder = StepBuilder("testStep", mockk(relaxed = true))
            val simpleStepBuilder = TestBuilderBridge.simpleStepBuilder<Int, Int>(stepBuilder)

            // when
            val step =
                simpleStepBuilder
                    .chunk(chunkSize)
                    .transactionManager(ResourcelessTransactionManager())
                    .reader {
                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }.writer {
                        throw IllegalStateException("Error")
                    }.faultTolerant() // use faultTolerant
                    .noRollback(IllegalStateException::class.java) // wrapped by making it as noRollback
                    .transactionAttribute(
                        object : DefaultTransactionAttribute() {
                            override fun rollbackOn(ex: Throwable): Boolean {
                                // make it always rollback (batch exit with failed)
                                // but with faultTolerant, class defined in noRollback is considered
                                // noRollback in transaction by wrapping transactionAttribute
                                ++noRollbackCallCount
                                return ex is IllegalStateException
                            }
                        },
                    ).build()
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(noRollbackCallCount).isEqualTo(0)
        }

        @Test
        fun transactionAttributeShouldHonorNoRollbackWhenConfiguredBeforeFaultTolerantOnDsl() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var readCallCount = 0
            var noRollbackCallCount = 0

            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(chunkSize) {
                    transactionAttribute(
                        object : DefaultTransactionAttribute() {
                            override fun rollbackOn(ex: Throwable): Boolean {
                                ++noRollbackCallCount
                                return ex is IllegalStateException
                            }
                        },
                    )
                    reader {
                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }
                    writer {
                        throw IllegalStateException("Error")
                    }
                    faultTolerant {
                        noRollback<IllegalStateException>()
                    }
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(noRollbackCallCount).isEqualTo(0)
        }

        @Test
        fun transactionAttributeShouldHonorNoRollbackWhenConfiguredAfterFaultTolerantOnDsl() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var readCallCount = 0
            var noRollbackCallCount = 0

            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(chunkSize) {
                    reader {
                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }
                    writer {
                        throw IllegalStateException("Error")
                    }
                    faultTolerant {
                        noRollback<IllegalStateException>()
                    }
                    transactionAttribute(
                        object : DefaultTransactionAttribute() {
                            override fun rollbackOn(ex: Throwable): Boolean {
                                ++noRollbackCallCount
                                return ex is IllegalStateException
                            }
                        },
                    )
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(noRollbackCallCount).isEqualTo(0)
        }

        @Test
        fun streamShouldRegisterWithChunkMonitorWhenConfiguredAfterFaultTolerantOnDelegate() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var streamOpenCallCount = 0
            var readCallCount = 0

            val stepBuilder = StepBuilder("testStep", mockk(relaxed = true))
            val simpleStepBuilder = TestBuilderBridge.simpleStepBuilder<Int, Int>(stepBuilder)

            class TestStream :
                ItemStream,
                ItemReader<Int> {
                override fun open(executionContext: ExecutionContext) {
                    ++streamOpenCallCount
                    Throwable()
                        .stackTrace
                        .filter { it.className.endsWith("ChunkMonitor") }
                        .also {
                            assertThat(it).isNotEmpty
                        }
                }

                override fun update(executionContext: ExecutionContext) {
                }

                override fun close() {
                }

                override fun read(): Int? = null
            }

            // when
            val step =
                simpleStepBuilder
                    .chunk(chunkSize)
                    .transactionManager(ResourcelessTransactionManager())
                    .reader {
                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }.writer {}
                    .faultTolerant()
                    .stream(TestStream())
                    .build()
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(streamOpenCallCount).isEqualTo(1)
        }

        @Test
        fun streamShouldRegisterWithChunkMonitorWhenConfiguredBeforeFaultTolerantOnDsl() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var streamOpenCallCount = 0
            var readCallCount = 0

            class TestStream :
                ItemStream,
                ItemReader<Int> {
                override fun open(executionContext: ExecutionContext) {
                    ++streamOpenCallCount
                    Throwable().printStackTrace()
                    Throwable()
                        .stackTrace
                        .filter { it.className.endsWith("ChunkMonitor") }
                        .also {
                            assertThat(it).isNotEmpty
                        }
                }

                override fun update(executionContext: ExecutionContext) {
                }

                override fun close() {
                }

                override fun read(): Int? = null
            }

            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(chunkSize) {
                    stream(TestStream())
                    reader {
                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }
                    writer {}
                    faultTolerant {}
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(streamOpenCallCount).isEqualTo(1)
        }

        @Test
        fun streamShouldRegisterWithChunkMonitorWhenConfiguredAfterFaultTolerantOnDsl() {
            // given
            val chunkSize = 3
            val readLimit = 20
            var streamOpenCallCount = 0
            var readCallCount = 0

            class TestStream :
                ItemStream,
                ItemReader<Int> {
                override fun open(executionContext: ExecutionContext) {
                    ++streamOpenCallCount
                    Throwable()
                        .stackTrace
                        .filter { it.className.endsWith("ChunkMonitor") }
                        .also {
                            assertThat(it).isNotEmpty
                        }
                }

                override fun update(executionContext: ExecutionContext) {
                }

                override fun close() {
                }

                override fun read(): Int? = null
            }

            // when
            val step =
                simpleStepBuilderDsl<Int, Int>(chunkSize) {
                    reader {
                        if (readCallCount < readLimit) {
                            ++readCallCount
                            1
                        } else {
                            null
                        }
                    }
                    writer {}
                    faultTolerant {
                    }
                    stream(TestStream())
                }
            val jobExecution = JobExecution(0L, jobInstance, jobParameters)
            val stepExecution = StepExecution(step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(readCallCount).isEqualTo(readLimit)
            assertThat(streamOpenCallCount).isEqualTo(1)
        }
    }

    private fun <I : Any, O : Any> simpleStepBuilderDsl(
        chunkSize: Int,
        init: SimpleStepBuilderDsl<I, O>.() -> Unit,
    ): Step {
        val dslContext =
            DslContext(
                beanFactory = mockk(),
                jobRepository = mockk(),
            )
        val stepBuilder = StepBuilder("testStep", mockk(relaxed = true))
        val simpleStepBuilder = stepBuilder.chunk<I, O>(chunkSize, ResourcelessTransactionManager())

        return SimpleStepBuilderDsl(dslContext, simpleStepBuilder).apply(init).build()
    }
}
