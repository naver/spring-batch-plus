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

package com.navercorp.spring.batch.plus.sample.step.flowstep.bean

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.Flow
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
    fun testFlow(batch: BatchDsl): Flow = batch {
        val innerStep = batch {
            step("innerStep") {
                tasklet { _, _ ->
                    println("run innerStep")
                    RepeatStatus.FINISHED
                }
            }
        }

        flow("testFlow") {
            step(innerStep) {
                on("COMPLETED") {
                    fail()
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("testFlow") testFlow: Flow,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("flowStep")
                    .flow(testFlow)
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("innerStep") {
                    flowBean("testFlow")
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
