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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Step
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.step.builder.JobStepBuilder
import org.springframework.batch.core.step.job.JobParametersExtractor

internal class JobStepBuilderDslTest {

    @Test
    fun testLauncher() {
        // given
        val jobStepBuilder = mockk<JobStepBuilder>(relaxed = true)

        // when
        val jobLauncher = mockk<JobLauncher>()
        JobStepBuilderDsl(mockk(), jobStepBuilder).apply {
            launcher(jobLauncher)
        }.build()

        // then
        verify(exactly = 1) { jobStepBuilder.launcher(jobLauncher) }
    }

    @Test
    fun testParametersExtractor() {
        // given
        val jobStepBuilder = mockk<JobStepBuilder>(relaxed = true)

        // when
        val jobParametersExtractor = mockk<JobParametersExtractor>()
        JobStepBuilderDsl(mockk(), jobStepBuilder).apply {
            parametersExtractor(jobParametersExtractor)
        }.build()

        // then
        verify(exactly = 1) { jobStepBuilder.parametersExtractor(jobParametersExtractor) }
    }

    @Test
    fun testBuild() {
        // given
        val mockStep = mockk<Step>()
        val jobStepBuilder = mockk<JobStepBuilder>(relaxed = true) {
            every { build() } returns mockStep
        }

        // when
        val actual = JobStepBuilderDsl(mockk(), jobStepBuilder).build()

        // then
        assertThat(actual).isEqualTo(mockStep)
    }
}
