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

package com.navercorp.spring.batch.plus.sample.job.flow.step.variable

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            val testStep3 = batch {
                step("testStep3") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }

            step(testStep1())
            step(testStep2())
            step(testStep3)
        }
    }

    @Bean
    open fun testStep1(): Step = batch {
        step("testStep1") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }

    @Bean
    open fun testStep2(): Step = batch {
        step("testStep2") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
