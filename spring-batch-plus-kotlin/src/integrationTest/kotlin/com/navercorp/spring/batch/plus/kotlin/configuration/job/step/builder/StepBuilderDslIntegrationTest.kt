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

package com.navercorp.spring.batch.plus.kotlin.configuration.job.step.builder

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.StepExecution
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
import java.util.UUID
import javax.sql.DataSource

/**
 * Covers common step settings, rather than the declarations that select a step type.
 */
internal class StepBuilderDslIntegrationTest {
    @Test
    fun listenerBeanShouldInvokeStepExecutionListenerWhenStepRuns() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val listenerName = UUID.randomUUID().toString()
        var beforeStepCallCount = 0
        var afterStepCallCount = 0
        context.registerBean(listenerName) {
            object : StepExecutionListener {
                override fun beforeStep(stepExecution: StepExecution) {
                    ++beforeStepCallCount
                }

                override fun afterStep(stepExecution: StepExecution): ExitStatus? {
                    ++afterStepCallCount
                    return null
                }
            }
        }

        // when
        val job =
            batch {
                job(UUID.randomUUID().toString()) {
                    step(UUID.randomUUID().toString()) {
                        listenerBean(listenerName)
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(beforeStepCallCount).isEqualTo(1)
        assertThat(afterStepCallCount).isEqualTo(1)
    }

    @Test
    fun listenerBeanShouldInvokeAnnotatedStepCallbacksWhenStepRuns() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val listenerName = UUID.randomUUID().toString()
        var beforeStepCallCount = 0
        var afterStepCallCount = 0
        context.registerBean(listenerName) {
            object {
                @BeforeStep
                fun beforeStep() {
                    ++beforeStepCallCount
                }

                @AfterStep
                fun afterStep() {
                    ++afterStepCallCount
                }
            }
        }

        // when
        val job =
            batch {
                job(UUID.randomUUID().toString()) {
                    step(UUID.randomUUID().toString()) {
                        listenerBean(listenerName)
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(beforeStepCallCount).isEqualTo(1)
        assertThat(afterStepCallCount).isEqualTo(1)
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
