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

package com.navercorp.spring.batch.plus.kotlin.configuration.job.transition

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
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
 * Covers a step as the restart target after a stopped execution.
 */
internal class StopAndRestartToStepIntegrationTest {
    @Test
    fun stopAndRestartToStepBeanShouldRestartAtStepWhenBeanNameIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 =
            batch {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        val transitionStep =
            batch {
                step("transitionStep") {
                    tasklet(
                        { _, _ ->
                            ++transitionStepCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        context.registerBean("transitionStep") {
            transitionStep
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step(testStep1) {
                        on("COMPLETED") {
                            stopAndRestartToStepBean("transitionStep")
                        }
                    }
                }
            }
        val firstJobExecution = jobOperator.start(job, JobParameters())
        val secondJobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stopAndRestartToStepShouldRestartAtStepWhenInitIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 =
            batch {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step(testStep1) {
                        on("COMPLETED") {
                            stopAndRestartToStep("transitionStep") {
                                tasklet(
                                    { _, _ ->
                                        ++transitionStepCallCount
                                        RepeatStatus.FINISHED
                                    },
                                    ResourcelessTransactionManager(),
                                )
                            }
                        }
                    }
                }
            }
        val firstJobExecution = jobOperator.start(job, JobParameters())
        val secondJobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stopAndRestartToStepShouldRestartAtStepWhenInstanceIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 =
            batch {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        val transitionStep =
            batch {
                step("transitionStep") {
                    tasklet(
                        { _, _ ->
                            ++transitionStepCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step(testStep1) {
                        on("COMPLETED") {
                            stopAndRestartToStep(transitionStep)
                        }
                    }
                }
            }
        val firstJobExecution = jobOperator.start(job, JobParameters())
        val secondJobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stopAndRestartToStepBeanShouldApplySubsequentTransitionAfterRestartWhenBeanNameAndTransitionAreProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 =
            batch {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        val transitionStep =
            batch {
                step("transitionStep") {
                    tasklet(
                        { _, _ ->
                            ++transitionStepCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        context.registerBean("transitionStep") {
            transitionStep
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step(testStep1) {
                        on("COMPLETED") {
                            stopAndRestartToStepBean("transitionStep") {
                                on("COMPLETED") {
                                    end("TEST")
                                }
                                on("*") {
                                    stop()
                                }
                            }
                        }
                    }
                }
            }
        val firstJobExecution = jobOperator.start(job, JobParameters())
        val secondJobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stopAndRestartToStepShouldApplySubsequentTransitionAfterRestartWhenInitAndTransitionAreProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 =
            batch {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step(testStep1) {
                        on("COMPLETED") {
                            stopAndRestartToStep("transitionStep", {
                                tasklet(
                                    { _, _ ->
                                        ++transitionStepCallCount
                                        RepeatStatus.FINISHED
                                    },
                                    ResourcelessTransactionManager(),
                                )
                            }) {
                                on("COMPLETED") {
                                    end("TEST")
                                }
                                on("*") {
                                    stop()
                                }
                            }
                        }
                    }
                }
            }
        val firstJobExecution = jobOperator.start(job, JobParameters())
        val secondJobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stopAndRestartToStepShouldApplySubsequentTransitionAfterRestartWhenInstanceAndTransitionAreProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 =
            batch {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        val transitionStep =
            batch {
                step("transitionStep") {
                    tasklet(
                        { _, _ ->
                            ++transitionStepCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }

        // when
        val job =
            batch {
                job("testJob") {
                    step(testStep1) {
                        on("COMPLETED") {
                            stopAndRestartToStep(transitionStep) {
                                on("COMPLETED") {
                                    end("TEST")
                                }
                                on("*") {
                                    stop()
                                }
                            }
                        }
                    }
                }
            }
        val firstJobExecution = jobOperator.start(job, JobParameters())
        val secondJobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
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
