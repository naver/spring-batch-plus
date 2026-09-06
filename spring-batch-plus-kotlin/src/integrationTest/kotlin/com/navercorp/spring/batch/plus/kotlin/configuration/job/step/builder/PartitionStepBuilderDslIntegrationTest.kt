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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.partition.PartitionHandler
import org.springframework.batch.core.partition.StepExecutionSplitter
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
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
 * Covers the integration boundary from partition declarations to actual partition-step execution.
 */
internal class PartitionStepBuilderDslIntegrationTest {
    @Test
    fun partitionerShouldCreatePartitionStepWhenPartitionHandlerIsProvided() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val jobName = UUID.randomUUID().toString()
        val stepName = UUID.randomUUID().toString()
        val splitStepName = UUID.randomUUID().toString()
        val gridSize = 4
        var partitionHandlerCallCount = 0
        var partitionerCallCount = 0

        // when
        val job =
            batch {
                job(jobName) {
                    step(stepName) {
                        partitioner {
                            partitionHandler { stepSplitter, stepExecution ->
                                ++partitionHandlerCallCount
                                stepSplitter
                                    .split(stepExecution, gridSize)
                                    .map { it.complete() }
                            }
                            splitter(splitStepName) { partitionCount ->
                                ++partitionerCallCount
                                (0 until partitionCount)
                                    .associate { "$it" to ExecutionContext() }
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(partitionHandlerCallCount).isEqualTo(1)
        assertThat(partitionerCallCount).isEqualTo(1)
    }

    @Test
    fun partitionerShouldCreatePartitionStepWhenTaskExecutorPartitionHandlerIsConfigured() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobOperator = context.getBean<JobOperator>()
        val batch = context.getBean<BatchDsl>()
        val jobName = UUID.randomUUID().toString()
        val stepName = UUID.randomUUID().toString()
        val splitStepName = UUID.randomUUID().toString()
        val gridSize = 4
        var workerStepCallCount = 0
        var taskExecutorCallCount = 0
        var partitionerCallCount = 0
        val workerStep =
            batch {
                step(splitStepName) {
                    tasklet(
                        { _, _ ->
                            ++workerStepCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }

        // when
        val job =
            batch {
                job(jobName) {
                    step(stepName) {
                        partitioner {
                            partitionHandler {
                                step(workerStep)
                                taskExecutor { task ->
                                    ++taskExecutorCallCount
                                    task.run()
                                }
                                gridSize(gridSize)
                            }
                            splitter(splitStepName) { partitionCount ->
                                ++partitionerCallCount
                                (0 until partitionCount)
                                    .associate { "$it" to ExecutionContext() }
                            }
                        }
                    }
                }
            }
        val jobExecution = jobOperator.start(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(workerStepCallCount).isEqualTo(gridSize)
        assertThat(taskExecutorCallCount).isEqualTo(gridSize)
        assertThat(partitionerCallCount).isEqualTo(1)
    }

    @Nested
    inner class RedundancyCheck {
        @Test
        fun partitionHandlerShouldIgnoreTaskExecutorPartitionHandlerSettingsWhenBothAreConfigured() {
            // given
            val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
            val jobOperator = context.getBean<JobOperator>()
            val batch = context.getBean<BatchDsl>()
            val jobName = UUID.randomUUID().toString()
            val stepName = UUID.randomUUID().toString()
            val splitStepName = UUID.randomUUID().toString()
            val gridSize = 4
            var partitionHandlerCallCount = 0
            var taskExecutorCallCount = 0
            var workerStepCallCount = 0
            val workerStep =
                batch {
                    step(splitStepName) {
                        tasklet(
                            { _, _ ->
                                ++workerStepCallCount
                                RepeatStatus.FINISHED
                            },
                            ResourcelessTransactionManager(),
                        )
                    }
                }

            // when
            val job =
                batch {
                    job(jobName) {
                        step(stepName) {
                            partitioner {
                                partitionHandler { _, _ ->
                                    ++partitionHandlerCallCount
                                    listOf()
                                }
                                partitionHandler {
                                    step(workerStep)
                                    taskExecutor { task ->
                                        ++taskExecutorCallCount
                                        task.run()
                                    }
                                    gridSize(gridSize)
                                }
                                splitter(splitStepName) { partitionCount ->
                                    (0 until partitionCount)
                                        .associate { "$it" to ExecutionContext() }
                                }
                            }
                        }
                    }
                }
            val jobExecution = jobOperator.start(job, JobParameters())

            // then
            assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(partitionHandlerCallCount).isEqualTo(1)
            assertThat(taskExecutorCallCount).isEqualTo(0)
            assertThat(workerStepCallCount).isEqualTo(0)
        }

        @Test
        fun splitterShouldIgnorePartitionerWhenBothAreConfigured() {
            // given
            val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
            val jobOperator = context.getBean<JobOperator>()
            val batch = context.getBean<BatchDsl>()
            val jobName = UUID.randomUUID().toString()
            val stepName = UUID.randomUUID().toString()
            val splitStepName = UUID.randomUUID().toString()
            val gridSize = 4
            var splitterCallCount = 0
            var partitionerCallCount = 0
            val partitionHandler =
                PartitionHandler { stepSplitter, stepExecution ->
                    stepSplitter
                        .split(stepExecution, gridSize)
                        .map { it.complete() }
                }
            val splitter =
                object : StepExecutionSplitter {
                    override fun getStepName(): String = splitStepName

                    override fun split(
                        stepExecution: StepExecution,
                        gridSize: Int,
                    ): Set<StepExecution> {
                        ++splitterCallCount
                        val jobExecution = stepExecution.jobExecution
                        return (0 until gridSize)
                            .map {
                                StepExecution(0L, "${stepName}$it", jobExecution)
                                    .also { jobExecution.addStepExecution(it) }
                            }.toSet()
                    }
                }

            // when
            val job =
                batch {
                    job(jobName) {
                        step(stepName) {
                            partitioner {
                                partitionHandler(partitionHandler)
                                splitter(splitter)
                                splitter(splitStepName) {
                                    ++partitionerCallCount
                                    mapOf()
                                }
                            }
                        }
                    }
                }
            val jobExecution = jobOperator.start(job, JobParameters())

            // then
            assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(splitterCallCount).isEqualTo(1)
            assertThat(partitionerCallCount).isEqualTo(0)
        }
    }

    private fun StepExecution.complete(): StepExecution =
        apply {
            exitStatus = ExitStatus.COMPLETED
            status = BatchStatus.COMPLETED
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
