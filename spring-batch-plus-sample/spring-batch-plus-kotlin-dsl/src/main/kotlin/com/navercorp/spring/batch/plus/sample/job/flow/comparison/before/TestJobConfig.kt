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

package com.navercorp.spring.batch.plus.sample.job.flow.comparison.before

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    open fun testJob(): Job {
        return jobBuilderFactory.get("testJob")
            .start(testStep1()).on("COMPLETED").to(successStep())
            .from(testStep1()).on("FAILED").to(failureStep())
            .from(testStep1()).on("*").stop()
            .build()
            .build()
    }

    @Bean
    open fun testStep1(): Step {
        return stepBuilderFactory.get("testStep1")
            .tasklet { _, _ ->
                throw IllegalStateException("step failed")
            }
            .build()
    }

    @Bean
    open fun successStep(): Step {
        return stepBuilderFactory.get("successStep")
            .tasklet { _, _ -> RepeatStatus.FINISHED }
            .build()
    }

    @Bean
    open fun failureStep(): Step {
        return stepBuilderFactory.get("failureStep")
            .tasklet { _, _ -> RepeatStatus.FINISHED }
            .build()
    }
}
