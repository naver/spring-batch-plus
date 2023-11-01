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

package com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.retrylistener

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.transaction.PlatformTransactionManager

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        listener(
                            object : RetryListener {
                                override fun <T : Any?, E : Throwable?> open(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                ): Boolean {
                                    println("RetryListener::open (context: $context")
                                    return true
                                }

                                override fun <T : Any?, E : Throwable?> close(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                    throwable: Throwable?,
                                ) {
                                    println("RetryListener::close (error: ${throwable?.message})")
                                }

                                override fun <T : Any?, E : Throwable?> onError(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                    throwable: Throwable?,
                                ) {
                                    println("RetryListener::onError (error: ${throwable?.message})")
                                }
                            },
                        )
                        retry<IllegalStateException>()
                        retryLimit(4)
                    }
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
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        var tryCount = 0

        return ItemProcessor<Int, String> { item ->
            if (item == 5 && tryCount < 3) {
                ++tryCount
                throw IllegalStateException("Error (tryCount: $tryCount)")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
