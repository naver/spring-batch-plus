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

package com.navercorp.spring.batch.plus.sample.step.jobstep.config.launcher

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val jobLauncher: JobLauncher
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                job(anotherJob()) {
                    launcher { job, jobParameters ->
                        println("launch anotherJob!!!")
                        jobLauncher.run(job, jobParameters)
                    }
                    // same as
                    // launcher(
                    //     object : JobLauncher {
                    //         override fun run(job: Job, jobParameters: JobParameters): JobExecution {
                    //             println("launch anotherJob!!!")
                    //             return jobLauncher.run(job, jobParameters)
                    //         }
                    //     }
                    // )
                }
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
