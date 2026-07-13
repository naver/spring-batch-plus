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
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.annotation.AfterChunk
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeChunk
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.item.ItemStream
import org.springframework.batch.infrastructure.repeat.RepeatCallback
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.batch.infrastructure.repeat.support.RepeatTemplate
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
import java.util.concurrent.ThreadLocalRandom
import javax.sql.DataSource

/**
 * Integration tests for creating and executing tasklet steps through the public Kotlin DSL.
 */
internal class TaskletStepBuilderDslIntegrationTest {
    @Test
    fun testTaskletBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        val tasklet =
            Tasklet { _, _ ->
                ++taskletCallCount
                RepeatStatus.FINISHED
            }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        taskletBean("testTasklet")
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletBeanWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var streamOpenCallCount = 0
        var streamCloseCallCount = 0
        val tasklet =
            Tasklet { _, _ ->
                ++taskletCallCount
                RepeatStatus.FINISHED
            }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        taskletBean("testTasklet") {
                            stream(
                                object : ItemStream {
                                    override fun open(executionContext: ExecutionContext) {
                                        ++streamOpenCallCount
                                    }

                                    override fun close() {
                                        ++streamCloseCallCount
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
        assertThat(taskletCallCount).isEqualTo(1)
        assertThat(streamOpenCallCount).isEqualTo(1)
        assertThat(streamCloseCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletBeanWithTransactionManager() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        val tasklet =
            Tasklet { _, _ ->
                ++taskletCallCount
                RepeatStatus.FINISHED
            }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        taskletBean("testTasklet", ResourcelessTransactionManager())
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletBeanWithTransactionManagerAndInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var streamOpenCallCount = 0
        var streamCloseCallCount = 0
        val tasklet =
            Tasklet { _, _ ->
                ++taskletCallCount
                RepeatStatus.FINISHED
            }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        taskletBean("testTasklet", ResourcelessTransactionManager()) {
                            stream(
                                object : ItemStream {
                                    override fun open(executionContext: ExecutionContext) {
                                        ++streamOpenCallCount
                                    }

                                    override fun close() {
                                        ++streamCloseCallCount
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
        assertThat(taskletCallCount).isEqualTo(1)
        assertThat(streamOpenCallCount).isEqualTo(1)
        assertThat(streamCloseCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletWithLambda() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        tasklet { _, _ ->
                            ++taskletCallCount
                            RepeatStatus.FINISHED
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletWithLambdaAndInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var streamOpenCallCount = 0
        var streamCloseCallCount = 0

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        tasklet(
                            { _, _ ->
                                ++taskletCallCount
                                RepeatStatus.FINISHED
                            },
                        ) {
                            stream(
                                object : ItemStream {
                                    override fun open(executionContext: ExecutionContext) {
                                        ++streamOpenCallCount
                                    }

                                    override fun close() {
                                        ++streamCloseCallCount
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
        assertThat(taskletCallCount).isEqualTo(1)
        assertThat(streamOpenCallCount).isEqualTo(1)
        assertThat(streamCloseCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletWithAnnotationListener() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var beforeStepCallCount = 0
        var afterStepCallCount = 0
        var beforeChunkCallCount = 0
        var afterChunkCallCount = 0

        // when
        val job =
            batch {
                job("testJob") {
                    step("testStep") {
                        tasklet(
                            { _, _ ->
                                ++taskletCallCount
                                RepeatStatus.FINISHED
                            },
                        ) {
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
        assertThat(taskletCallCount).isEqualTo(1)
        assertThat(beforeStepCallCount).isEqualTo(1)
        assertThat(afterStepCallCount).isEqualTo(1)
        assertThat(beforeChunkCallCount).isEqualTo(1)
        assertThat(afterChunkCallCount).isEqualTo(1)
    }

    @Nested
    inner class RedundancyCheck {
        @Suppress("DEPRECATION")
        @Test
        fun testStepOperationsIgnoreTaskExecutorAndExceptionHandler() {
            // given
            var iterateCount = 0
            var taskExecutorCallCount = 0
            var exceptionHandlerCallCount = 0
            val stepBuilder = StepBuilder(UUID.randomUUID().toString(), mockk(relaxed = true))

            // when
            val step =
                stepBuilder
                    .tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    .stepOperations(
                        object : RepeatTemplate() {
                            override fun iterate(callback: RepeatCallback): RepeatStatus {
                                ++iterateCount
                                return super.iterate(callback)
                            }
                        },
                    ).taskExecutor { task ->
                        ++taskExecutorCallCount
                        task.run()
                    }.exceptionHandler { _, e ->
                        ++exceptionHandlerCallCount
                        throw e
                    }.build()
            val jobInstance = JobInstance(ThreadLocalRandom.current().nextLong(), UUID.randomUUID().toString())
            val jobExecution = JobExecution(0L, jobInstance, JobParameters())
            val stepExecution = StepExecution(0L, step.name, jobExecution)
            step.execute(stepExecution)

            // then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(iterateCount).isEqualTo(1)
            assertThat(taskExecutorCallCount).isEqualTo(0)
            assertThat(exceptionHandlerCallCount).isEqualTo(0)
        }
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
