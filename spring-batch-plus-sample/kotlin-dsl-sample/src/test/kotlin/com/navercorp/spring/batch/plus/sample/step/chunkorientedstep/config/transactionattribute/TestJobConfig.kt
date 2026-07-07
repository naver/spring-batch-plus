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

package com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.transactionattribute

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.job.Job
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import org.springframework.transaction.support.TransactionSynchronizationManager

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(testItemReader())
                        writer(testItemWriter())
                        transactionAttribute(
                            DefaultTransactionAttribute().apply {
                                setName("test-tx")
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 20) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> =
        ItemWriter { items ->
            val transactionName = TransactionSynchronizationManager.getCurrentTransactionName()
            println("write $items (transactionName: $transactionName)")
        }
}
