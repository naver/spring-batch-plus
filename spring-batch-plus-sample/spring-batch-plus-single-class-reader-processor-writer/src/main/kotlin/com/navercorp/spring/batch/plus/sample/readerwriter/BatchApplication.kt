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

package com.navercorp.spring.batch.plus.sample.readerwriter

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.item.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.item.asItemStreamWriter
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@EnableBatchProcessing
@SpringBootApplication
class BatchApplication {

    @Bean
    fun testJob(
        sampleTasklet: SampleTasklet,
        batch: BatchDsl,
    ): Job = batch {
        job("testJob") {
            steps {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(sampleTasklet.asItemStreamReader())
                        writer(sampleTasklet.asItemStreamWriter())
                    }
                }
            }
        }
    }
}

fun main() {
    runApplication<BatchApplication>()
}
