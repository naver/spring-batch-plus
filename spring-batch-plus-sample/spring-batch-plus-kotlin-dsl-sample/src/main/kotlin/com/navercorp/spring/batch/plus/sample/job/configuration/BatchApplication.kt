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

package com.navercorp.spring.batch.plus.sample.job.configuration

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
class BatchApplication {
    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            // configuration
            .preventRestart()
            .validator { /* do something */ }
            .start(
                stepBuilderFactory.get("testStep")
                    .tasklet { _, _ ->
                        println("run testTasklet")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            // configuration
            preventRestart()
            validator { /* do something */ }

            steps {
                step("testStep") {
                    tasklet { _, _ ->
                        println("run testTasklet")
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
