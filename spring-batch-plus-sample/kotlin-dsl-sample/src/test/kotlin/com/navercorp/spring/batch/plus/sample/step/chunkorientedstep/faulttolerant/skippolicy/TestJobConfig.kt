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

package com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.skippolicy

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        skipPolicy(
                            LimitCheckingItemSkipPolicy(
                                4,
                                mapOf(RuntimeException::class.java to true),
                            ),
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                if (count < 11) {
                    return count++
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
