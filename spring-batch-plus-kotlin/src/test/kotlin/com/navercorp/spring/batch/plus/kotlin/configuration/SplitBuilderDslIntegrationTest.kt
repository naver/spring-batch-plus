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
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.registerBean
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import javax.sql.DataSource

internal class SplitBuilderDslIntegrationTest {

    @Test
    fun testFlowBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val callerThread = Thread.currentThread().name
        var taskExecutorCallCount = 0
        val taskExecutor = object : ThreadPoolTaskExecutor() {
            override fun execute(task: Runnable) {
                ++taskExecutorCallCount
                super.execute(task)
            }
        }.apply { initialize() }
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val testFlow1 = batch {
            flow("testFlow1") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
                        assertThat(Thread.currentThread().name).isNotEqualTo(callerThread)
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        val testFlow2 = batch {
            flow("testFlow2") {
                step("testStep2") {
                    tasklet { _, _ ->
                        ++testStep2CallCount
                        assertThat(Thread.currentThread().name).isNotEqualTo(callerThread)
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        context.apply {
            registerBean("testFlow1") {
                testFlow1
            }
            registerBean("testFlow2") {
                testFlow2
            }
        }

        // when
        val job = batch {
            job("testJob") {
                split(taskExecutor) {
                    flowBean("testFlow1")
                    flowBean("testFlow2")
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(taskExecutorCallCount).isEqualTo(2)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var taskExecutorCallCount = 0
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val callerThread = Thread.currentThread().name
        val taskExecutor = object : ThreadPoolTaskExecutor() {
            override fun execute(task: Runnable) {
                ++taskExecutorCallCount
                super.execute(task)
            }
        }.apply { initialize() }

        // when
        val job = batch {
            job("testJob") {
                split(taskExecutor) {
                    flow("testFlow1") {
                        step("testStep1") {
                            tasklet { _, _ ->
                                ++testStep1CallCount
                                assertThat(Thread.currentThread().name).isNotEqualTo(callerThread)
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    flow("testFlow2") {
                        step("testStep2") {
                            tasklet { _, _ ->
                                ++testStep2CallCount
                                assertThat(Thread.currentThread().name).isNotEqualTo(callerThread)
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(taskExecutorCallCount).isEqualTo(2)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        val callerThread = Thread.currentThread().name
        var taskExecutorCallCount = 0
        val taskExecutor = object : ThreadPoolTaskExecutor() {
            override fun execute(task: Runnable) {
                ++taskExecutorCallCount
                super.execute(task)
            }
        }.apply { initialize() }
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val testFlow1 = batch {
            flow("testFlow1") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
                        assertThat(Thread.currentThread().name).isNotEqualTo(callerThread)
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        val testFlow2 = batch {
            flow("testFlow2") {
                step("testStep2") {
                    tasklet { _, _ ->
                        ++testStep2CallCount
                        assertThat(Thread.currentThread().name).isNotEqualTo(callerThread)
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                split(taskExecutor) {
                    flow(testFlow1)
                    flow(testFlow2)
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(taskExecutorCallCount).isEqualTo(2)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testNoFlow() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        val taskExecutor = ThreadPoolTaskExecutor().apply {
            initialize()
        }

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    split(taskExecutor) {
                    }
                }
            }
        }.hasMessageContaining("should set at least one flow to split")
    }

    @Configuration
    @EnableBatchProcessing
    private open class TestConfiguration {

        @Bean
        open fun batchDsl(
            beanFactory: BeanFactory,
            jobBuilderFactory: JobBuilderFactory,
            stepBuilderFactory: StepBuilderFactory
        ): BatchDsl = BatchDsl(
            beanFactory,
            jobBuilderFactory,
            stepBuilderFactory
        )

        @Bean
        open fun dataSource(): DataSource {
            return EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("/org/springframework/batch/core/schema-h2.sql")
                .generateUniqueName(true)
                .build()
        }
    }
}
