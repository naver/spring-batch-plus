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

    private fun jobBuilderDsl(jobBuilder: JobBuilder): JobBuilderDsl {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobRepository = mock(),
        )

        return JobBuilderDsl(dslContext, jobBuilder)
    }

    @Test
    fun testValidator() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersValidator = mock<JobParametersValidator>()
        jobBuilderDsl.apply {
            validator(jobParametersValidator)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).validator(jobParametersValidator)
    }

    @Test
    fun testIncrementer() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersIncrementer = mock<JobParametersIncrementer>()
        jobBuilderDsl.apply {
            incrementer(jobParametersIncrementer)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).incrementer(jobParametersIncrementer)
    }

    @Test
    fun testObservationConvention() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val observationConvention = mock<BatchJobObservationConvention>()
        jobBuilderDsl.apply {
            observationConvention(observationConvention)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).observationConvention(observationConvention)
    }

    @Test
    fun testObservationRegistry() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val observationRegistry = mock<ObservationRegistry>()
        jobBuilderDsl.apply {
            observationRegistry(observationRegistry)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).observationRegistry(observationRegistry)
    }

    @Test
    fun testMeterRegistry() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val meterRegistry = mock<MeterRegistry>()
        jobBuilderDsl.apply {
            meterRegistry(meterRegistry)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).meterRegistry(meterRegistry)
    }

    @Test
    fun testRepository() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobRepository = mock<JobRepository>()
        jobBuilderDsl.apply {
            repository(jobRepository)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).repository(jobRepository)
    }

    @Test
    fun testObjectListener() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        class TestListener

        // when
        val testListener = TestListener()
        jobBuilderDsl.apply {
            listener(testListener)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).listener(testListener)
    }

    @Test
    fun testJobExecutionListener() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobExecutionListener = mock<JobExecutionListener>()
        jobBuilderDsl.apply {
            listener(jobExecutionListener)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).listener(jobExecutionListener)
    }

    @Test
    fun testPreventRestart() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        jobBuilderDsl.apply {
            preventRestart()
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).preventRestart()
    }

    @Test
    fun testConfigurationAfterStep() {
        // given
        val jobBuilder = spy(JobBuilder(UUID.randomUUID().toString(), mock()))
        val jobBuilderDsl = jobBuilderDsl(jobBuilder)

        // when
        val jobParametersValidator = mock<JobParametersValidator>()
        jobBuilderDsl.apply {
            step(mock())
            validator(jobParametersValidator)
        }.build()

        // then
        verify(jobBuilder, atLeastOnce()).validator(jobParametersValidator)
    }
}
