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
import org.junit.jupiter.api.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.core.observability.BatchStepObservationConvention
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.transaction.PlatformTransactionManager
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

/**
 * org.springframework.batch.core.step.builder.StepBuilderHelper related tests
 */
internal class StepBuilderDslHelperTest {

    private fun stepBuilderDsl(stepBuilder: StepBuilder): StepBuilderDsl {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobRepository = mock(),
        )

        return StepBuilderDsl(dslContext, stepBuilder)
    }

    @Suppress("DEPRECATION")
    @Test
    fun testRepository() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val jobRepository = mock<JobRepository>()
        stepBuilderDsl.apply {
            repository(jobRepository)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).repository(jobRepository)
    }

    @Test
    fun testObservationConvention() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val observationConvention = mock<BatchStepObservationConvention>()
        stepBuilderDsl.apply {
            observationConvention(observationConvention)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).observationConvention(observationConvention)
    }

    @Test
    fun testObservationRegistry() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val observationRegistry = mock<ObservationRegistry>()
        stepBuilderDsl.apply {
            observationRegistry(observationRegistry)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).observationRegistry(observationRegistry)
    }

    @Test
    fun testMeterRegistry() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val meterRegistry = mock<MeterRegistry>()
        stepBuilderDsl.apply {
            meterRegistry(meterRegistry)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).meterRegistry(meterRegistry)
    }

    @Test
    fun testStartLimit() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val startLimit = ThreadLocalRandom.current().nextInt()
        stepBuilderDsl.apply {
            startLimit(startLimit)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).startLimit(startLimit)
    }

    @Test
    fun testObjectListener() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        class TestListener

        // when
        val testListener = TestListener()
        stepBuilderDsl.apply {
            listener(testListener)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).listener(testListener)
    }

    @Test
    fun testStepExecutionListener() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val stepExecutionListener = mock<StepExecutionListener>()
        stepBuilderDsl.apply {
            listener(stepExecutionListener)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).listener(stepExecutionListener)
    }

    @Test
    fun testAllowStartIfComplete() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val allowStartIfComplete = ThreadLocalRandom.current().nextBoolean()
        stepBuilderDsl.apply {
            allowStartIfComplete(allowStartIfComplete)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).allowStartIfComplete(allowStartIfComplete)
    }
}
