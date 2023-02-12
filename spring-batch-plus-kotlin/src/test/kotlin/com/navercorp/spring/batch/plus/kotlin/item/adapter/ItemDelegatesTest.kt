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

package com.navercorp.spring.batch.plus.kotlin.item.adapter

import com.navercorp.spring.batch.plus.item.adapter.ItemStreamReaderProcessorWriter
import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.TransactionManager
import reactor.core.publisher.Flux
import javax.sql.DataSource

internal class ItemDelegatesTest {

    companion object {
        private var processCallCount = 0
        private var writeCallCount = 0
    }

    @Test
    fun testExtensions() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val testTasklet = context.getBean<TestClass>("testTasklet")

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3, ResourcelessTransactionManager()) {
                        reader(testTasklet.asItemStreamReader())
                        processor(testTasklet.asItemProcessor())
                        writer(testTasklet.asItemStreamWriter())
                    }
                }
            }
        }
        val jobParameters = JobParametersBuilder()
            .addLong("limit", 20L)
            .toJobParameters()
        val jobExecution = jobLauncher.run(job, jobParameters)

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(processCallCount).isEqualTo(20)
        assertThat(writeCallCount).isEqualTo(7) // ceil(20/3)
    }

    internal open class TestClass(
        private val limit: Long,
    ) : ItemStreamReaderProcessorWriter<Int, String> {

        override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
            var count = 0
            return Flux.generate { sink ->
                if (count < limit) {
                    sink.next(count)
                    ++count
                } else {
                    sink.complete()
                }
            }
        }

        override fun process(item: Int): String {
            ++processCallCount
            return item.toString()
        }

        override fun write(chunk: Chunk<out String>) {
            ++writeCallCount
        }
    }

    @Configuration
    @EnableBatchProcessing(
        dataSourceRef = "metadataDataSource",
        transactionManagerRef = "metadataTransactionManager",
    )
    private open class TestConfiguration {

        @Bean
        open fun batchDsl(
            beanFactory: BeanFactory,
            jobRepository: JobRepository,
        ): BatchDsl = BatchDsl(
            beanFactory,
            jobRepository,
        )

        @Bean
        open fun metadataTransactionManager(): TransactionManager {
            return DataSourceTransactionManager(metadataDataSource())
        }

        @Bean
        open fun metadataDataSource(): DataSource {
            return EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("/org/springframework/batch/core/schema-h2.sql")
                .generateUniqueName(true)
                .build()
        }

        @Bean
        @StepScope
        open fun testTasklet(
            @Value("#{jobParameters['limit']}") limit: Long,
        ): TestClass {
            return TestClass(limit)
        }
    }
}
