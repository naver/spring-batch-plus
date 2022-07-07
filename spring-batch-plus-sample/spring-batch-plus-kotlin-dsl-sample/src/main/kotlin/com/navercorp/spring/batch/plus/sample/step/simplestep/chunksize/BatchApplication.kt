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

package com.navercorp.spring.batch.plus.sample.step.simplestep.chunksize

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBatchProcessing
class BatchApplication {
    // before
    @Bean
    fun beforeJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory,
    ): Job {
        return jobBuilderFactory.get("beforeJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .chunk<Int, Int>(3)
                    .reader(
                        object : ItemReader<Int> {
                            private var count = 0

                            override fun read(): Int? {
                                return if (count < 5) {
                                    count++
                                } else {
                                    null
                                }
                            }
                        }
                    )
                    .processor(ItemProcessor { item -> item })
                    .writer {
                        println("write $it")
                    }
                    .faultTolerant()
                    .retry(RuntimeException::class.java)
                    .retryLimit(2)
                    .exceptionHandler { _, _ ->
                        // do something
                    }
                    .build()
            )
            .build()
    }

    // after
    @Bean
    fun afterJob(batch: BatchDsl): Job = batch {
        job("afterJob") {
            steps {
                step("testStep") {
                    chunk(3) {
                        reader(
                            object : ItemReader<Int> {
                                private var count = 0

                                override fun read(): Int? {
                                    return if (count < 5) {
                                        count++
                                    } else {
                                        null
                                    }
                                }
                            }
                        )
                        processor { item -> item }
                        writer {
                            println("write $it")
                        }
                        exceptionHandler { _, _ ->
                            // do something
                        }
                        faultTolerant {
                            retry(RuntimeException::class)
                            retryLimit(2)
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
