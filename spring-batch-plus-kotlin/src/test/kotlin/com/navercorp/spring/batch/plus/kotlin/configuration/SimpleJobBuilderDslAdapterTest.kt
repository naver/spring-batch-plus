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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.step.Step
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager
import java.util.UUID

/**
 * Covers the adapter boundary between supported plain-step replay and rejected flow-job operations.
 */
internal class SimpleJobBuilderDslAdapterTest {
    @Test
    fun stepBeanShouldDelegateToSimpleJobBuilderDslWhenBeanNameIsProvided() {
        // given
        val stepName = UUID.randomUUID().toString()
        val simpleJobBuilderDsl = mockk<SimpleJobBuilderDsl>(relaxed = true)
        val simpleJobBuilderDslAdapter = SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)

        // when
        simpleJobBuilderDslAdapter.stepBean(stepName)

        // then
        verify(exactly = 1) { simpleJobBuilderDsl.stepBean(stepName) }
    }

    @Test
    fun stepShouldDelegateToSimpleJobBuilderDslWhenNameAndInitAreProvided() {
        // given
        val stepName = UUID.randomUUID().toString()
        val step = mockk<Step>()
        val stepInit: StepBuilderDsl.() -> Step = { step }
        val simpleJobBuilderDsl = mockk<SimpleJobBuilderDsl>(relaxed = true)
        val simpleJobBuilderDslAdapter = SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)

        // when
        simpleJobBuilderDslAdapter.step(stepName, stepInit)

        // then
        verify(exactly = 1) { simpleJobBuilderDsl.step(stepName, stepInit) }
    }

    @Test
    fun stepShouldDelegateToSimpleJobBuilderDslWhenStepIsProvided() {
        // given
        val step = mockk<Step>()
        val simpleJobBuilderDsl = mockk<SimpleJobBuilderDsl>(relaxed = true)
        val simpleJobBuilderDslAdapter = SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)

        // when
        simpleJobBuilderDslAdapter.step(step)

        // then
        verify(exactly = 1) { simpleJobBuilderDsl.step(step) }
    }

    @Test
    fun buildShouldReturnJobWhenSimpleJobBuilderDslBuildsJob() {
        // given
        val job = mockk<Job>()
        val simpleJobBuilderDsl =
            mockk<SimpleJobBuilderDsl> {
                every { build() } returns job
            }
        val simpleJobBuilderDslAdapter = SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)

        // when
        val actual = simpleJobBuilderDslAdapter.build()

        // then
        assertThat(actual).isSameAs(job)
    }

    @Test
    fun flowJobOperationShouldThrowUnsupportedOperationExceptionWhenSimpleJobAdapterIsUsed() {
        // given
        val simpleJobBuilderDsl = SimpleJobBuilderDsl(DslContext(mockk(), mockk()), mockk())
        val simpleJobBuilderDslAdapter = SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)

        // when, then
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.stepBean("testStep") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.step(
                "testStep",
                {
                    tasklet(
                        { _, _ -> RepeatStatus.FINISHED },
                        ResourcelessTransactionManager(),
                    )
                },
            ) {}
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.step(mockk<Step>()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flowBean("testFlow")
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow("testFlow") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow(mockk())
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flowBean("testFlow") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow("testFlow", {}) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow(mockk<Flow>()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.deciderBean("testDecider") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.decider(mockk()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.split(mockk()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}
