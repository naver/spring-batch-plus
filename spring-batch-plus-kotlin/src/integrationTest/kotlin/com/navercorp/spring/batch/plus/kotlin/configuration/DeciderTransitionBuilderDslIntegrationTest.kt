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

package com.navercorp.spring.batch.plus.kotlin.configuration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.TransactionManager
import javax.sql.DataSource

internal class DeciderTransitionBuilderDslIntegrationTest {

    @RepeatedTest(10)
    fun testDeciderWithMultipleTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val expectedFlowExecutionStatus = randomFlowExecutionStatus()
        var testDeciderCallCount = 0
        val testDecider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            expectedFlowExecutionStatus
        }

        // when
        val job = batch {
            job("testJob") {
                decider(testDecider) {
                    on("UNKNOWN") {
                        end()
                    }
                    on("*") {
                        fail()
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(testDeciderCallCount).isEqualTo(1)
        when (expectedFlowExecutionStatus) {
            FlowExecutionStatus.UNKNOWN -> {
                assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
                // just finish it so no exit status
                assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
            }

            FlowExecutionStatus.STOPPED -> { // when stopped, just stop the job
                assertThat(jobExecution.status).isEqualTo(BatchStatus.STOPPED)
                assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
            }

            else -> {
                assertThat(jobExecution.status).isEqualTo(BatchStatus.FAILED)
                assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.FAILED.exitCode)
            }
        }
    }

    @Test
    fun testStepWithNoTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        val testDecider = JobExecutionDecider { _, _ ->
            FlowExecutionStatus.COMPLETED
        }

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    decider(testDecider) {
                        // no transition
                    }
                }
            }
        }.hasMessageContaining("should set transition for decider")
    }

    private fun randomFlowExecutionStatus(): FlowExecutionStatus {
        return listOf(
            FlowExecutionStatus.COMPLETED,
            FlowExecutionStatus.FAILED,
            FlowExecutionStatus.UNKNOWN,
            FlowExecutionStatus.STOPPED, // when stopped, just stop the job
        ).random()
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
    }
}
