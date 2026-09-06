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
 * Covers a step as an immediate transition destination, distinct from branching from a step source.
 */
internal class OnStepTargetIntegrationTest {
    @Test
    fun stepBeanShouldExecuteAsTransitionTargetWhenBeanNameIsProvided() {
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
                            stepBean("transitionStep")
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stepShouldExecuteAsTransitionTargetWhenInitIsProvided() {
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
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stepShouldExecuteAsTransitionTargetWhenInstanceIsProvided() {
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
                            step(transitionStep)
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stepBeanShouldApplySubsequentTransitionWhenBeanNameAndTransitionAreProvided() {
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
                            throw RuntimeException("Error")
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
                            stepBean("transitionStep") {
                                on("COMPLETED") {
                                    fail()
                                }
                                on("FAILED") {
                                    end("TEST")
                                }
                                on("*") {
                                    end()
                                }
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stepShouldApplySubsequentTransitionWhenInitAndTransitionAreProvided() {
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
                            step(
                                "transitionStep",
                                {
                                    tasklet(
                                        { _, _ ->
                                            ++transitionStepCallCount
                                            throw RuntimeException("Error")
                                        },
                                        ResourcelessTransactionManager(),
                                    )
                                },
                            ) {
                                on("COMPLETED") {
                                    fail()
                                }
                                on("FAILED") {
                                    end("TEST")
                                }
                                on("*") {
                                    end()
                                }
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun stepShouldApplySubsequentTransitionWhenInstanceAndTransitionAreProvided() {
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
                            throw RuntimeException("Error")
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
                            step(transitionStep) {
                                on("COMPLETED") {
                                    fail()
                                }
                                on("FAILED") {
                                    end("TEST")
                                }
                                on("*") {
                                    end()
                                }
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
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
