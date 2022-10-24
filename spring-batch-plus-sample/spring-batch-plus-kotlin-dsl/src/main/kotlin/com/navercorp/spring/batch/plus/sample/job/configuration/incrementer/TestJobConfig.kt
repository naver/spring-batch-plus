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

package com.navercorp.spring.batch.plus.sample.job.configuration.incrementer

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            incrementer {
                val nextValue = it?.getLong("param")?.plus(1L) ?: 0L
                JobParametersBuilder(it ?: JobParameters())
                    .addLong("param", nextValue)
                    .toJobParameters()
            }
            // same as
            // incrementer(
            //     object : JobParametersIncrementer {
            //         override fun getNext(parameters: JobParameters?): JobParameters {
            //             val nextValue = parameters?.getLong("param")?.plus(1L) ?: 0L
            //             return JobParametersBuilder(parameters ?: JobParameters())
            //                 .addLong("param", nextValue)
            //                 .toJobParameters()
            //         }
            //     }
            // )
            step("testStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
