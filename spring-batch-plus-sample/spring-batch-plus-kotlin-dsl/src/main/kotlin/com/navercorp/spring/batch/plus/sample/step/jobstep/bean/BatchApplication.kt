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

package com.navercorp.spring.batch.plus.sample.step.jobstep.bean

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
class BatchApplication {
    // common
    @Bean
    fun testJob1(batch: BatchDsl): Job = batch {
        job("testJob1") {
            step("testStep1") {
                allowStartIfComplete(true)
                tasklet { _, _ ->
                    println("run testStep1Tasklet")
                    RepeatStatus.FINISHED
                }
            }
        }
    }

    @Bean
    fun testJob2(batch: BatchDsl): Job = batch {
        job("testJob2") {
            step("testStep2") {
                allowStartIfComplete(true)
                tasklet { _, _ ->
                    println("run testStep2Tasklet")
                    RepeatStatus.FINISHED
                }
            }
        }
    }

    @Bean
    fun testJob3(batch: BatchDsl): Job = batch {
        job("testJob3") {
            step("testStep3") {
                allowStartIfComplete(true)
                tasklet { _, _ ->
                    println("run testStep3Tasklet")
                    RepeatStatus.FINISHED
                }
            }
        }
    }

    @Bean
    fun testJob4(batch: BatchDsl): Job = batch {
        job("testJob4") {
            step("testStep4") {
                allowStartIfComplete(true)
                tasklet { _, _ ->
                    println("run testStep4Tasklet")
                    RepeatStatus.FINISHED
                }
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("testJob1") testJob1: Job,
        @Qualifier("testJob2") testJob2: Job,
        @Qualifier("testJob3") testJob3: Job,
        @Qualifier("testJob4") testJob4: Job
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("jobStep1")
                    .job(testJob1)
                    .build()
            )
            .next(
                stepBuilderFactory.get("jobStep2")
                    .job(testJob2)
                    .build()
            )
            .next(
                stepBuilderFactory.get("jobStep3")
                    .job(testJob3)
                    .build()
            )
            .next(
                stepBuilderFactory.get("jobStep4")
                    .job(testJob4)
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            step("jobStep1") {
                jobBean("testJob1")
            }
            step("jobStep2") {
                jobBean("testJob2")
            }
            step("jobStep3") {
                jobBean("testJob3")
            }
            step("jobStep4") {
                jobBean("testJob4")
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
