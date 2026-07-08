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

package com.navercorp.spring.batch.plus.kotlin.configuration.step

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeChunk
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.TransactionManager
import java.util.UUID
import javax.sql.DataSource

/**
 * Integration tests for creating and executing chunk-oriented steps through the public Kotlin DSL.
 */
internal class ChunkOrientedStepBuilderDslIntegrationTest {
    @Test
    fun testChunkOrientedStep() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val jobName = UUID.randomUUID().toString()
        val stepName = UUID.randomUUID().toString()
        val readLimit = 6
        var readCallCount = 0
        val writtenItems = mutableListOf<Int>()
        val reader: ItemReader<Int> =
            ItemReader {
                if (readCallCount < readLimit) {
                    readCallCount++
                } else {
                    null
                }
            }
        val writer: ItemWriter<Int> =
            ItemWriter { chunk ->
                writtenItems.addAll(chunk.items)
            }

        // when
        val job =
            batch {
                job(jobName) {
                    step(stepName) {
                        chunk<Int, Int>(3) {
                            transactionManager(ResourcelessTransactionManager())
                            reader(reader)
                            writer(writer)
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(writtenItems).containsExactly(0, 1, 2, 3, 4, 5)
    }

    @Test
    fun testChunkOrientedStepWithAnnotationListener() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val jobName = UUID.randomUUID().toString()
        val stepName = UUID.randomUUID().toString()
        var readCallCount = 0
        var beforeStepCallCount = 0
        var afterStepCallCount = 0
        var beforeChunkCallCount = 0
        var afterChunkCallCount = 0
        val reader: ItemReader<Int> =
            ItemReader {
                if (readCallCount < 1) {
                    readCallCount++
                } else {
                    null
                }
            }
        val writer: ItemWriter<Int> = ItemWriter { }

        // when
        val job =
            batch {
                job(jobName) {
                    step(stepName) {
                        chunk<Int, Int>(3) {
                            transactionManager(ResourcelessTransactionManager())
                            reader(reader)
                            writer(writer)
                            listener(
                                object {
                                    @BeforeStep
                                    fun beforeStep() {
                                        ++beforeStepCallCount
                                    }

                                    @AfterStep
                                    fun afterStep() {
                                        ++afterStepCallCount
                                    }

                                    @BeforeChunk
                                    fun beforeChunk() {
                                        ++beforeChunkCallCount
                                    }

                                    @AfterChunk
                                    fun afterChunk() {
                                        ++afterChunkCallCount
                                    }
                                },
                            )
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(1)
        assertThat(beforeStepCallCount).isEqualTo(1)
        assertThat(afterStepCallCount).isEqualTo(1)
        assertThat(beforeChunkCallCount).isEqualTo(1)
        assertThat(afterChunkCallCount).isEqualTo(1)
    }

    @Test
    fun testFaultTolerantChunkOrientedStepWithVariantItemComponentTypes() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val jobName = UUID.randomUUID().toString()
        val stepName = UUID.randomUUID().toString()
        var readCallCount = 0
        var skipInProcessCallCount = 0
        val writtenItems = mutableListOf<Number>()
        val reader: ItemReader<Int> =
            ItemReader {
                if (readCallCount < 6) {
                    readCallCount++
                } else {
                    null
                }
            }
        val processor: ItemProcessor<Number, Int> =
            ItemProcessor { item ->
                val value = item.toInt()
                if (value == 2) {
                    throw IllegalArgumentException("skipped")
                }
                value * 10
            }
        val writer: ItemWriter<Number> =
            ItemWriter { chunk ->
                writtenItems.addAll(chunk.items)
            }
        val skipListener =
            object : SkipListener<Any, Number> {
                override fun onSkipInProcess(
                    item: Any,
                    t: Throwable,
                ) {
                    ++skipInProcessCallCount
                }
            }

        // when
        val job =
            batch {
                job(jobName) {
                    step(stepName) {
                        chunk<Number, Int>(3) {
                            transactionManager(ResourcelessTransactionManager())
                            reader(reader)
                            processor(processor)
                            writer(writer)
                            faultTolerant()
                            skip<IllegalArgumentException>()
                            skipLimit(1L)
                            skipListener(skipListener)
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(6)
        assertThat(skipInProcessCallCount).isEqualTo(1)
        assertThat(writtenItems).containsExactly(0, 10, 30, 40, 50)
    }

    @Configuration
    @EnableBatchProcessing
    @EnableJdbcJobRepository(
        dataSourceRef = "metadataDataSource",
        transactionManagerRef = "metadataTransactionManager",
    )
    private open class TestConfiguration {
        @Bean
        open fun batchDsl(
            beanFactory: BeanFactory,
            jobRepository: JobRepository,
        ): BatchDsl =
            BatchDsl(
                beanFactory,
                jobRepository,
            )

        @Bean
        open fun metadataTransactionManager(): TransactionManager = DataSourceTransactionManager(metadataDataSource())

        @Bean
        open fun metadataDataSource(): DataSource =
            EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("/org/springframework/batch/core/schema-h2.sql")
                .generateUniqueName(true)
                .build()
    }
}
