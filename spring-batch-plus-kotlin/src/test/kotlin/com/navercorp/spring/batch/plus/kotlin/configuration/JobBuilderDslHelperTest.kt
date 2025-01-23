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
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.JobParametersIncrementer
import org.springframework.batch.core.JobParametersValidator
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.observability.BatchJobObservationConvention
import org.springframework.batch.core.repository.JobRepository
import java.util.UUID

/**
 * org.springframework.batch.core.job.builder.JobBuilderHelper related tests
 */
internal class JobBuilderDslHelperTest {

    @Test
    fun testValidator() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersValidator = mockk<JobParametersValidator>()
        jobBuilderDsl.apply {
            validator(jobParametersValidator)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.validator(jobParametersValidator) }
    }

    @Test
    fun testIncrementer() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersIncrementer = mockk<JobParametersIncrementer>()
        jobBuilderDsl.apply {
            incrementer(jobParametersIncrementer)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.incrementer(jobParametersIncrementer) }
    }

    @Test
    fun testObservationConvention() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val observationConvention = mockk<BatchJobObservationConvention>()
        jobBuilderDsl.apply {
            observationConvention(observationConvention)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.observationConvention(observationConvention) }
    }

    @Test
    fun testObservationRegistry() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val observationRegistry = mockk<ObservationRegistry>()
        jobBuilderDsl.apply {
            observationRegistry(observationRegistry)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.observationRegistry(observationRegistry) }
    }

    @Test
    fun testMeterRegistry() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val meterRegistry = mockk<MeterRegistry>()
        jobBuilderDsl.apply {
            meterRegistry(meterRegistry)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.meterRegistry(meterRegistry) }
    }

    @Suppress("DEPRECATION")
    @Test
    fun testRepository() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobRepository = mockk<JobRepository>()
        jobBuilderDsl.apply {
            repository(jobRepository)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.repository(jobRepository) }
    }

    @Test
    fun testObjectListener() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        class TestListener

        // when
        val testListener = TestListener()
        jobBuilderDsl.apply {
            listener(testListener)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.listener(testListener) }
    }

    @Test
    fun testJobExecutionListener() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobExecutionListener = mockk<JobExecutionListener>()
        jobBuilderDsl.apply {
            listener(jobExecutionListener)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.listener(jobExecutionListener) }
    }

    @Test
    fun testPreventRestart() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        jobBuilderDsl.apply {
            preventRestart()
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.preventRestart() }
    }

    @Test
    fun testConfigurationAfterStep() {
        // given
        val jobBuilder = spyk(JobBuilder(UUID.randomUUID().toString(), mockk(relaxed = true)))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersValidator = mockk<JobParametersValidator>()
        jobBuilderDsl.apply {
            step(mockk())
            validator(jobParametersValidator)
        }.build()

        // then
        verify(exactly = 1) { jobBuilder.validator(jobParametersValidator) }
    }

    private fun jobBuilderDsl(jobBuilder: JobBuilder): JobBuilderDsl {
        val dslContext = DslContext(
            beanFactory = mockk(),
            jobRepository = mockk(),
        )

        return JobBuilderDsl(dslContext, jobBuilder)
    }
}
