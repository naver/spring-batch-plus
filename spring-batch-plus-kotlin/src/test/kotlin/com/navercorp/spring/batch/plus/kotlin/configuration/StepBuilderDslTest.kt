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
import io.micrometer.observation.ObservationRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.BeanFactory
import org.springframework.transaction.PlatformTransactionManager
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

/**
 * Covers deferred application of common step settings before specialized step creation.
 */
internal class StepBuilderDslTest {
    @Test
    fun observationRegistryShouldConfigureStepBuilderWhenRegistryIsProvided() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val observationRegistry = mockk<ObservationRegistry>()
        stepBuilderDsl
            .apply {
                observationRegistry(observationRegistry)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.observationRegistry(observationRegistry) }
    }

    @Test
    fun startLimitShouldConfigureStepBuilderWhenLimitIsProvided() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val startLimit = ThreadLocalRandom.current().nextInt()
        stepBuilderDsl
            .apply {
                startLimit(startLimit)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.startLimit(startLimit) }
    }

    @Test
    fun listenerBeanShouldConfigureStepBuilderWhenStepExecutionListenerBeanIsProvided() {
        // given
        val listenerName = UUID.randomUUID().toString()
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepExecutionListener = mockk<StepExecutionListener>()
        val beanFactory = mockk<BeanFactory>()
        every { beanFactory.getBean(listenerName, Any::class.java) } returns stepExecutionListener
        val stepBuilderDsl = StepBuilderDsl(DslContext(beanFactory, mockk()), stepBuilder)

        // when
        stepBuilderDsl
            .apply {
                listenerBean(listenerName)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilder.listener(stepExecutionListener) }
    }

    @Test
    fun listenerBeanShouldConfigureStepBuilderWhenObjectListenerBeanIsProvided() {
        // given
        val listenerName = UUID.randomUUID().toString()
        val stepBuilder = mockk<StepBuilder>(relaxed = true)

        class TestListener

        val testListener = TestListener()
        val beanFactory = mockk<BeanFactory>()
        every { beanFactory.getBean(listenerName, Any::class.java) } returns testListener
        val stepBuilderDsl = StepBuilderDsl(DslContext(beanFactory, mockk()), stepBuilder)

        // when
        stepBuilderDsl
            .apply {
                listenerBean(listenerName)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilder.listener(testListener) }
    }

    @Test
    fun listenerShouldConfigureStepBuilderWhenObjectListenerIsProvided() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        class TestListener

        // when
        val testListener = TestListener()
        stepBuilderDsl
            .apply {
                listener(testListener)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.listener(testListener) }
    }

    @Test
    fun listenerShouldConfigureStepBuilderWhenStepExecutionListenerIsProvided() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val stepExecutionListener = mockk<StepExecutionListener>()
        stepBuilderDsl
            .apply {
                listener(stepExecutionListener)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.listener(stepExecutionListener) }
    }

    @Test
    fun allowStartIfCompleteShouldConfigureStepBuilderWhenFlagIsProvided() {
        // given
        val stepBuilder = mockk<StepBuilder>(relaxed = true)
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val allowStartIfComplete = ThreadLocalRandom.current().nextBoolean()
        stepBuilderDsl
            .apply {
                allowStartIfComplete(allowStartIfComplete)
            }.tasklet(mockk(), mockk<PlatformTransactionManager>())

        // then
        verify(exactly = 1) { stepBuilderDsl.allowStartIfComplete(allowStartIfComplete) }
    }

    private fun stepBuilderDsl(stepBuilder: StepBuilder): StepBuilderDsl {
        val dslContext =
            DslContext(
                beanFactory = mockk(),
                jobRepository = mockk(),
            )

        return StepBuilderDsl(dslContext, stepBuilder)
    }
}
