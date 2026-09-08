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

package com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.skiplistener

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.listener.SkipListener
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
                        skipListener(
                            object : SkipListener<Int, String> {
                                override fun onSkipInRead(t: Throwable) {
                                    println("Ignore exception of read (exception: ${t.message})")
                                }

                                override fun onSkipInProcess(
                                    item: Int,
                                    t: Throwable,
                                ) {
                                    println("Ignore exception of process (item: $item, exception: ${t.message})")
                                }

                                override fun onSkipInWrite(
                                    item: String,
                                    t: Throwable,
                                ) {
                                    println("Ignore exception of write (item: $item, exception: ${t.message})")
                                }
                            },
                        )
                        skip<IllegalStateException>()
                        skipLimit(3L)
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                val next = count++

                if (next == 3) {
                    throw IllegalStateException("I am ignored in read")
                }

                if (next < 11) {
                    return next
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            if (item == 5) {
                throw IllegalStateException("I am ignored in process")
            }

            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { chunk ->
            if (chunk.items.contains("7")) {
                throw IllegalStateException("I am ignored in write")
            }

            println("write ${chunk.items}")
        }
}
