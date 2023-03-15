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

package com.navercorp.spring.batch.plus.sample.step.taskletstep.config.transactionattribute

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import org.springframework.transaction.support.TransactionSynchronizationManager

@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    transactionAttribute(
                        DefaultTransactionAttribute().apply {
                            setName("test-tx")
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        // print false
        val transactionName = TransactionSynchronizationManager.getCurrentTransactionName()
        println("run testTasklet (transactionName: $transactionName}")
        RepeatStatus.FINISHED
    }
}
