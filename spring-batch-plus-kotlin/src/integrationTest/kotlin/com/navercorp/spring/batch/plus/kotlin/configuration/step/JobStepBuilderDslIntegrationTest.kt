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
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.registerBean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.TransactionManager
import javax.sql.DataSource

/**
 * Integration tests for creating and executing job steps through the public Kotlin DSL.
 */
internal class JobStepBuilderDslIntegrationTest {

    @Test
    fun testJobBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var stepCallCount = 0
        val testJob2 =
            batch {
                job("testJob2") {
                    step("testStep2") {
                        ++stepCallCount
                        tasklet(
                            { _, _ -> RepeatStatus.FINISHED },
                            ResourcelessTransactionManager(),
                        )
                    }
                }
            }
        context.registerBean("testJob2") {
            testJob2
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        jobBean("testJob2")
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepCallCount).isEqualTo(1)
    }

    @Test
    fun testJobBeanWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var stepCallCount = 0
        var jobParametersExtractorCallCount = 0
        val testJob2 =
            batch {
                job("testJob2") {
                    step("testStep2") {
                        ++stepCallCount
                        tasklet(
                            { _, _ -> RepeatStatus.FINISHED },
                            ResourcelessTransactionManager(),
                        )
                    }
                }
            }
        context.registerBean("testJob2") {
            testJob2
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        jobBean("testJob2") {
                            parametersExtractor { _, _ ->
                                ++jobParametersExtractorCallCount
                                JobParameters()
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepCallCount).isEqualTo(1)
        assertThat(jobParametersExtractorCallCount).isEqualTo(1)
    }

    @Test
    fun testJobWithJobVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var stepCallCount = 0
        val testJob2 =
            batch {
                job("testJob2") {
                    step("testStep2") {
                        ++stepCallCount
                        tasklet(
                            { _, _ -> RepeatStatus.FINISHED },
                            ResourcelessTransactionManager(),
                        )
                    }
                }
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        job(testJob2)
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepCallCount).isEqualTo(1)
    }

    @Test
    fun testJobWithJobVariableAndInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var jobCallCount = 0
        var jobParametersExtractorCallCount = 0
        val testJob2 =
            batch {
                job("testJob2") {
                    step("testStep2") {
                        ++jobCallCount
                        tasklet(
                            { _, _ -> RepeatStatus.FINISHED },
                            ResourcelessTransactionManager(),
                        )
                    }
                }
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        job(testJob2) {
                            parametersExtractor { _, _ ->
                                ++jobParametersExtractorCallCount
                                JobParameters()
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobCallCount).isEqualTo(1)
        assertThat(jobParametersExtractorCallCount).isEqualTo(1)
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
