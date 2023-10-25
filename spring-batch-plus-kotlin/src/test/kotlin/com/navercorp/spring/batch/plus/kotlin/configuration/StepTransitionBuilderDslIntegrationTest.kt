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
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
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

internal class StepTransitionBuilderDslIntegrationTest {

    @RepeatedTest(10)
    fun testStepWithMultipleTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val expectedExitStatus = randomExitStatus()
        var testStep1CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet(
                    { contribution, _ ->
                        ++testStep1CallCount
                        contribution.exitStatus = expectedExitStatus
                        RepeatStatus.FINISHED
                    },
                    ResourcelessTransactionManager(),
                )
            }
        }

        // when
        val job = batch {
            job("testJob") {
                step(testStep1) {
                    on("COMPLETED") {
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
        assertThat(testStep1CallCount).isEqualTo(1)
        when (expectedExitStatus) {
            ExitStatus.COMPLETED -> {
                assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
                assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
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
        val testStep1 = batch {
            step("testStep1") {
                tasklet(
                    { _, _ ->
                        RepeatStatus.FINISHED
                    },
                    ResourcelessTransactionManager(),
                )
            }
        }

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step(testStep1) {
                        // no transition
                    }
                }
            }
        }.hasMessageContaining("should set transition for step")
    }

    private fun randomExitStatus(): ExitStatus {
        return listOf(
            ExitStatus.UNKNOWN,
            ExitStatus.NOOP,
            ExitStatus.FAILED,
            ExitStatus.STOPPED,
            ExitStatus.COMPLETED,
            // ExitStatus.EXECUTING, // why considered ExitStatus.COMPLETE?
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
