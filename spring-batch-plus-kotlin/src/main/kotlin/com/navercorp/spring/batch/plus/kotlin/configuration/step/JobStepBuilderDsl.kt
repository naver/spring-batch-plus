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
import org.springframework.batch.core.Step
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.step.builder.JobStepBuilder
import org.springframework.batch.core.step.job.JobParametersExtractor

/**
 * A dsl for [JobStepBuilder][org.springframework.batch.core.step.builder.JobStepBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class JobStepBuilderDsl internal constructor(
    @Suppress("unused")
    private val dslContext: DslContext,
    private val jobStepBuilder: JobStepBuilder
) {
    /**
     * Set for [JobStepBuilder.jobLauncher][org.springframework.batch.core.step.builder.JobStepBuilder.jobLauncher].
     */
    fun launcher(jobLauncher: JobLauncher) {
        this.jobStepBuilder.launcher(jobLauncher)
    }

    /**
     * Set for [JobStepBuilder.parametersExtractor][org.springframework.batch.core.step.builder.JobStepBuilder.parametersExtractor].
     */
    fun parametersExtractor(jobParametersExtractor: JobParametersExtractor) {
        this.jobStepBuilder.parametersExtractor(jobParametersExtractor)
    }

    internal fun build(): Step {
        return this.jobStepBuilder.build()
    }
}
