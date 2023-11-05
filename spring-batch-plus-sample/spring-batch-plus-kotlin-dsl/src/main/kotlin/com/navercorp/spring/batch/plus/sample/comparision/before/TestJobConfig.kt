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

package com.navercorp.spring.batch.plus.sample.comparision.before

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
open class TestJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job {
        return JobBuilder("testJob", jobRepository)
            .start(
                StepBuilder("testStep1", jobRepository)
                    .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    .build(),
            )
            .next(testStep2())
            .on("COMPLETED").to(testStep3())
            .from(testStep2())
            .on("FAILED").to(testStep4())
            .end()
            .build()
    }

    @Bean
    open fun testStep2(): Step {
        return StepBuilder("testStep2", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun testStep3(): Step {
        return StepBuilder("testStep3", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun testStep4(): Step {
        return StepBuilder("testStep4", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }
}
