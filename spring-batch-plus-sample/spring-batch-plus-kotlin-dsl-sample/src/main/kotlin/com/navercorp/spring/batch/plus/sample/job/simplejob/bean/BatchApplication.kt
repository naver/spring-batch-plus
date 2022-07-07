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

package com.navercorp.spring.batch.plus.sample.job.simplejob.bean

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
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
    fun testStep1(batch: BatchDsl): Step = batch {
        step("testStep1") {
            tasklet { _, _ ->
                println("run testTasklet1")
                RepeatStatus.FINISHED
            }
        }
    }

    @Bean
    fun testStep2(batch: BatchDsl): Step = batch {
        step("testStep2") {
            tasklet { _, _ ->
                println("run testTasklet2")
                RepeatStatus.FINISHED
            }
        }
    }

    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
        @Qualifier("testStep1") testStep1: Step,
        @Qualifier("testStep2") testStep2: Step,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(testStep1)
            .next(testStep2)
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                stepBean("testStep1")
                stepBean("testStep2")
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
