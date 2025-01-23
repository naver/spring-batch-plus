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

package com.navercorp.spring.batch.plus.kotlin.configuration

import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.core.observability.BatchStepObservationConvention
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.ThreadLocalRandom

/**
 * org.springframework.batch.core.step.builder.StepBuilderHelper related tests
 */
internal class StepBuilderDslHelperTest {

    @Suppress("DEPRECATION")
    @Test
    fun testRepository() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val jobRepository = mockk<JobRepository>()
        stepBuilderDsl.apply {
            repository(jobRepository)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.repository(jobRepository) }
    }

    @Test
    fun testObservationConvention() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val observationConvention = mockk<BatchStepObservationConvention>()
        stepBuilderDsl.apply {
            observationConvention(observationConvention)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.observationConvention(observationConvention) }
    }

    @Test
    fun testObservationRegistry() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val observationRegistry = mockk<ObservationRegistry>()
        stepBuilderDsl.apply {
            observationRegistry(observationRegistry)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.observationRegistry(observationRegistry) }
    }

    @Test
    fun testMeterRegistry() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val meterRegistry = mockk<MeterRegistry>()
        stepBuilderDsl.apply {
            meterRegistry(meterRegistry)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.meterRegistry(meterRegistry) }
    }

    @Test
    fun testStartLimit() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val startLimit = ThreadLocalRandom.current().nextInt()
        stepBuilderDsl.apply {
            startLimit(startLimit)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.startLimit(startLimit) }
    }

    @Test
    fun testObjectListener() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        class TestListener

        // when
        val testListener = TestListener()
        stepBuilderDsl.apply {
            listener(testListener)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.listener(testListener) }
    }

    @Test
    fun testStepExecutionListener() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val stepExecutionListener = mockk<StepExecutionListener>()
        stepBuilderDsl.apply {
            listener(stepExecutionListener)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.listener(stepExecutionListener) }
    }

    @Test
    fun testAllowStartIfComplete() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val allowStartIfComplete = ThreadLocalRandom.current().nextBoolean()
        stepBuilderDsl.apply {
            allowStartIfComplete(allowStartIfComplete)
        }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.allowStartIfComplete(allowStartIfComplete) }
    }

    private fun stepBuilderDsl(stepBuilder: StepBuilder): StepBuilderDsl {
        val dslContext = DslContext(
            beanFactory = mockk(),
            jobRepository = mockk(),
        )

        return StepBuilderDsl(dslContext, stepBuilder)
    }
}
