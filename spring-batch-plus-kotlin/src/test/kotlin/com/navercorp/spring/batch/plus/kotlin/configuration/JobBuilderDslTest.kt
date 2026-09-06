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
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.parameters.JobParametersIncrementer
import org.springframework.batch.core.job.parameters.JobParametersValidator
import org.springframework.batch.core.listener.JobExecutionListener
import java.util.UUID

/**
 * Covers deferred application of common job settings when a job is built.
 */
internal class JobBuilderDslTest {
    @Test
    fun validatorShouldConfigureJobBuilderWhenValidatorIsProvided() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersValidator = mockk<JobParametersValidator>()
        jobBuilderDsl
            .apply {
                validator(jobParametersValidator)
            }.build()

        // then
        verify(exactly = 1) { jobBuilder.validator(jobParametersValidator) }
    }

    @Test
    fun incrementerShouldConfigureJobBuilderWhenIncrementerIsProvided() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersIncrementer = mockk<JobParametersIncrementer>()
        jobBuilderDsl
            .apply {
                incrementer(jobParametersIncrementer)
            }.build()

        // then
        verify(exactly = 1) { jobBuilder.incrementer(jobParametersIncrementer) }
    }

    @Test
    fun observationRegistryShouldConfigureJobBuilderWhenRegistryIsProvided() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val observationRegistry = mockk<ObservationRegistry>()
        jobBuilderDsl
            .apply {
                observationRegistry(observationRegistry)
            }.build()

        // then
        verify(exactly = 1) { jobBuilder.observationRegistry(observationRegistry) }
    }

    @Test
    fun listenerShouldConfigureJobBuilderWhenObjectListenerIsProvided() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        class TestListener

        // when
        val testListener = TestListener()
        jobBuilderDsl
            .apply {
                listener(testListener)
            }.build()

        // then
        verify(exactly = 1) { jobBuilder.listener(testListener) }
    }

    @Test
    fun listenerShouldConfigureJobBuilderWhenJobExecutionListenerIsProvided() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobExecutionListener = mockk<JobExecutionListener>()
        jobBuilderDsl
            .apply {
                listener(jobExecutionListener)
            }.build()

        // then
        verify(exactly = 1) { jobBuilder.listener(jobExecutionListener) }
    }

    @Test
    fun preventRestartShouldConfigureJobBuilderWhenInvoked() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        jobBuilderDsl
            .apply {
                preventRestart()
            }.build()

        // then
        verify(exactly = 1) { jobBuilder.preventRestart() }
    }

    private fun jobBuilderDsl(jobBuilder: JobBuilder): JobBuilderDsl {
        val dslContext =
            DslContext(
                beanFactory = mockk(),
                jobRepository = mockk(),
            )

        return JobBuilderDsl(dslContext, jobBuilder)
    }
}
