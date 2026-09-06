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

package com.navercorp.spring.batch.plus.kotlin.configuration.job.decider

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
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
 * Covers the boundary where introducing a decider selects flow-job construction at any state position.
 */
internal class JobBuilderDslDeciderCreationIntegrationTest {
    @Test
    fun deciderBeanShouldExecuteAsFirstStateWhenBeanNameIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testDeciderCallCount = 0
        val testDecider =
            JobExecutionDecider { _, _ ->
                ++testDeciderCallCount
                FlowExecutionStatus.COMPLETED
            }
        context.registerBean("testDecider") {
            testDecider
        }

        // when
        val job =
            batch {
                job("testJob") {
                    deciderBean("testDecider") {
                        on("COMPLETED") {
                            end("TEST")
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun deciderBeanShouldExecuteAfterStepWhenBeanNameIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        val testDecider =
            JobExecutionDecider { _, _ ->
                ++testDeciderCallCount
                FlowExecutionStatus.COMPLETED
            }
        context.registerBean("testDecider") {
            testDecider
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep1") {
                        tasklet(
                            { _, _ ->
                                ++testStep1CallCount
                                RepeatStatus.FINISHED
                            },
                            ResourcelessTransactionManager(),
                        )
                    }
                    deciderBean("testDecider") {
                        on("COMPLETED") {
                            end("TEST")
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun deciderShouldExecuteAsFirstStateWhenInstanceIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testDeciderCallCount = 0
        val decider =
            JobExecutionDecider { _, _ ->
                ++testDeciderCallCount
                FlowExecutionStatus.COMPLETED
            }

        // when
        val job =
            batch {
                job("testJob") {
                    decider(decider) {
                        on("COMPLETED") {
                            end("TEST")
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun deciderShouldExecuteAfterStepWhenInstanceIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        val decider =
            JobExecutionDecider { _, _ ->
                ++testDeciderCallCount
                FlowExecutionStatus.COMPLETED
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep1") {
                        tasklet(
                            { _, _ ->
                                ++testStep1CallCount
                                RepeatStatus.FINISHED
                            },
                            ResourcelessTransactionManager(),
                        )
                    }
                    decider(decider) {
                        on("COMPLETED") {
                            end("TEST")
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testDeciderCallCount).isEqualTo(1)
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
