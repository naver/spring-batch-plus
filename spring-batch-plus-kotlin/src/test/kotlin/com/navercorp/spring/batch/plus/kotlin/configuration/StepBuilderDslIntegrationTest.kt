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
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy
import org.springframework.batch.repeat.support.RepeatTemplate
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
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

@Suppress("DEPRECATION")
internal class StepBuilderDslIntegrationTest {

    @Test
    fun testDeprecatedTaskletBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        val tasklet = Tasklet { _, _ ->
            ++taskletCallCount
            RepeatStatus.FINISHED
        }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step("testStep") {
                        taskletBean("testTasklet")
                    }
                }
            }
        }
    }

    @Test
    fun testDeprecatedTaskletBeanWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var taskExecutorCallCount = 0
        val tasklet = Tasklet { _, _ ->
            ++taskletCallCount
            RepeatStatus.FINISHED
        }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step("testStep") {
                        taskletBean("testTasklet") {
                            taskExecutor {
                                ++taskExecutorCallCount
                                it.run()
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testDeprecatedTaskletWithLambda() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0

        // when, then
        assertThatThrownBy {
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
        }
    }

    @Test
    fun testDeprecatedTaskletWithLambdaAndInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var taskExecutorCallCount = 0

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step("testStep") {
                        tasklet(
                            { _, _ ->
                                ++taskletCallCount
                                RepeatStatus.FINISHED
                            },
                        ) {
                            taskExecutor {
                                ++taskExecutorCallCount
                                it.run()
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testDeprecatedChunkWithCount() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        val readLimit = 20
        val chunkSize = 3
        var readCallCount = 0
        var writeCallCount = 0

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step("testStep") {
                        chunk<Int, Int>(chunkSize) {
                            reader {
                                if (readCallCount < readLimit) {
                                    ++readCallCount
                                    1
                                } else {
                                    null
                                }
                            }
                            writer {
                                ++writeCallCount
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testDeprecatedChunkWithCompletionPolicy() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        val readLimit = 20
        val chunkSize = 3
        var readCallCount = 0
        var writeCallCount = 0

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step("testStep") {
                        chunk<Int, Int>(SimpleCompletionPolicy(chunkSize)) {
                            reader {
                                if (readCallCount < readLimit) {
                                    ++readCallCount
                                    1
                                } else {
                                    null
                                }
                            }
                            writer {
                                ++writeCallCount
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testDeprecatedChunkWithRepeatOperations() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        val readLimit = 20
        val chunkSize = 3
        var readCallCount = 0
        var writeCallCount = 0

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    step("testStep") {
                        chunk<Int, Int>(
                            RepeatTemplate().apply {
                                setCompletionPolicy(SimpleCompletionPolicy(chunkSize))
                            },
                        ) {
                            reader {
                                if (readCallCount < readLimit) {
                                    ++readCallCount
                                    1
                                } else {
                                    null
                                }
                            }
                            writer {
                                ++writeCallCount
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testTaskletBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        val tasklet = Tasklet { _, _ ->
            ++taskletCallCount
            RepeatStatus.FINISHED
        }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    taskletBean("testTasklet", ResourcelessTransactionManager())
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletBeanWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var taskExecutorCallCount = 0
        val tasklet = Tasklet { _, _ ->
            ++taskletCallCount
            RepeatStatus.FINISHED
        }
        context.registerBean("testTasklet") {
            tasklet
        }

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    taskletBean("testTasklet", ResourcelessTransactionManager()) {
                        taskExecutor {
                            ++taskExecutorCallCount
                            it.run()
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
        assertThat(taskExecutorCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletWithLambda() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    tasklet(
                        { _, _ ->
                            ++taskletCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
    }

    @Test
    fun testTaskletWithLambdaAndInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var taskletCallCount = 0
        var taskExecutorCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    tasklet(
                        { _, _ ->
                            ++taskletCallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    ) {
                        taskExecutor {
                            ++taskExecutorCallCount
                            it.run()
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(taskletCallCount).isEqualTo(1)
        assertThat(taskExecutorCallCount).isEqualTo(1)
    }

    @Test
    fun testChunkWithCount() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val readLimit = 20
        val chunkSize = 3
        var readCallCount = 0
        var writeCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(chunkSize, ResourcelessTransactionManager()) {
                        reader {
                            if (readCallCount < readLimit) {
                                ++readCallCount
                                1
                            } else {
                                null
                            }
                        }
                        writer {
                            ++writeCallCount
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(7) // Ceil(20/3)
    }

    @Test
    fun testChunkWithCompletionPolicy() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val readLimit = 20
        val chunkSize = 3
        var readCallCount = 0
        var writeCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(SimpleCompletionPolicy(chunkSize), ResourcelessTransactionManager()) {
                        reader {
                            if (readCallCount < readLimit) {
                                ++readCallCount
                                1
                            } else {
                                null
                            }
                        }
                        writer {
                            ++writeCallCount
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(7) // Ceil(20/3)
    }

    @Test
    fun testChunkWithRepeatOperations() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val readLimit = 20
        val chunkSize = 3
        var readCallCount = 0
        var writeCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(
                        RepeatTemplate().apply {
                            setCompletionPolicy(SimpleCompletionPolicy(chunkSize))
                        },
                        ResourcelessTransactionManager(),
                    ) {
                        reader {
                            if (readCallCount < readLimit) {
                                ++readCallCount
                                1
                            } else {
                                null
                            }
                        }
                        writer {
                            ++writeCallCount
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(readCallCount).isEqualTo(readLimit)
        assertThat(writeCallCount).isEqualTo(7) // Ceil(20/3)
    }

    @Test
    fun testPartitioner() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var partitionHandlerCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    partitioner {
                        partitionHandler { _, _ ->
                            ++partitionHandlerCallCount
                            listOf()
                        }
                        splitter("splitStep") { mapOf() }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(partitionHandlerCallCount).isEqualTo(1)
    }

    @Test
    fun testJobBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var stepCallCount = 0
        val testJob2 = batch {
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
        val job = batch {
            job("testJob") {
                step("testStep") {
                    jobBean("testJob2")
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepCallCount).isEqualTo(1)
    }

    @Test
    fun testJobBeanWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var stepCallCount = 0
        var jobParametersExtractorCallCount = 0
        val testJob2 = batch {
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
        val job = batch {
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
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepCallCount).isEqualTo(1)
        assertThat(jobParametersExtractorCallCount).isEqualTo(1)
    }

    @Test
    fun testJobWithJobVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var stepCallCount = 0
        val testJob2 = batch {
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
        val job = batch {
            job("testJob") {
                step("testStep") {
                    job(testJob2)
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepCallCount).isEqualTo(1)
    }

    @Test
    fun testJobWithJobVariableAndInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var jobCallcount = 0
        var jobParametersExtractorCallCount = 0
        val testJob2 = batch {
            job("testJob2") {
                step("testStep2") {
                    ++jobCallcount
                    tasklet(
                        { _, _ -> RepeatStatus.FINISHED },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        }

        // when
        val job = batch {
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
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobCallcount).isEqualTo(1)
        assertThat(jobParametersExtractorCallCount).isEqualTo(1)
    }

    @Test
    fun testFlowBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val testFlow = batch {
            flow("testFlow") {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
                step("testStep2") {
                    tasklet(
                        { _, _ ->
                            ++testStep2CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        }
        context.registerBean("testFlow") {
            testFlow
        }

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    flowBean("testFlow")
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithLambda() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    flow("testFlow") {
                        step("testStep1") {
                            tasklet(
                                { _, _ ->
                                    ++testStep1CallCount
                                    RepeatStatus.FINISHED
                                },
                                ResourcelessTransactionManager(),
                            )
                        }
                        step("testStep2") {
                            tasklet(
                                { _, _ ->
                                    ++testStep2CallCount
                                    RepeatStatus.FINISHED
                                },
                                ResourcelessTransactionManager(),
                            )
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val testFlow = batch {
            flow("testFlow") {
                step("testStep1") {
                    tasklet(
                        { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
                step("testStep2") {
                    tasklet(
                        { _, _ ->
                            ++testStep2CallCount
                            RepeatStatus.FINISHED
                        },
                        ResourcelessTransactionManager(),
                    )
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                step("testStep") {
                    flow(testFlow)
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
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
