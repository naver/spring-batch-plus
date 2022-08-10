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

package com.navercorp.spring.batch.plus.sample.job.flowjob.split.bean

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
import org.springframework.core.task.SimpleAsyncTaskExecutor

@SpringBootApplication
@EnableBatchProcessing
class BatchApplication {
    // common
    @Bean
    fun testFlow1(batch: BatchDsl): Flow = batch {
        flow("testFlow1") {
            step("flowStep1") {
                tasklet { _, _ ->
                    println("run tasklet1")
                    RepeatStatus.FINISHED
                }
            }
        }
    }

    @Bean
    fun testFlow2(batch: BatchDsl): Flow = batch {
        flow("testFlow2") {
            step("flowStep2") {
                tasklet { _, _ ->
                    println("run tasklet2")
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
        @Qualifier("testFlow1") testFlow1: Flow,
        @Qualifier("testFlow2") testFlow2: Flow,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory
                    .get("dummyStep")
                    .tasklet { _, _ ->
                        println("run dummyTasklet")
                        RepeatStatus.FINISHED
                    }
                    .build()
            )
            .split(SimpleAsyncTaskExecutor())
            .add(testFlow1, testFlow2)
            .end()
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            flows {
                split(SimpleAsyncTaskExecutor()) {
                    flowBean("testFlow1")
                    flowBean("testFlow2")
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
