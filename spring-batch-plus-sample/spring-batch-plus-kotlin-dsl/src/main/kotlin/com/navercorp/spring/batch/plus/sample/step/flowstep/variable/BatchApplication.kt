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

package com.navercorp.spring.batch.plus.sample.step.flowstep.variable

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
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
        val flowStep = stepBuilderFactory.get("flowStep")
            .tasklet { _, _ ->
                println("run flowStep")
                RepeatStatus.FINISHED
            }
            .build()

        return jobBuilderFactory.get("beforeJob")
            .start(
                FlowBuilder<Flow>("testFlow")
                    .start(flowStep)
                    .on("COMPLETED").fail()
                    .from(flowStep).on("*").stop()
                    .build()
            )
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        val flowStep = batch {
            step("flowStep") {
                tasklet { _, _ ->
                    println("run flowStep")
                    RepeatStatus.FINISHED
                }
            }
        }

        job("afterJob") {
            steps {
                step("testStep") {
                    flow("testFlow") {
                        step(flowStep) {
                            on("COMPLETED") {
                                fail()
                            }
                            on("*") {
                                stop()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
