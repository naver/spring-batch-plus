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
import org.springframework.batch.core.*
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import javax.sql.DataSource

internal class JobBuilderDslIntegrationTest {

    @Test
    fun testValidator() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var validatorCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                validator {
                    ++validatorCallCount
                }
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(validatorCallCount).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun testIncrementer() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val jobExplorer = context.getBean<JobExplorer>()
        val batch = context.getBean<BatchDsl>()
        var incrementerCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                incrementer {
                    ++incrementerCallCount
                    it!!
                }
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
        }
        val jobParameters = JobParametersBuilder(jobExplorer)
            .getNextJobParameters(job)
            .toJobParameters()
        val jobExecution = jobLauncher.run(job, jobParameters)

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(incrementerCallCount).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun testRepository() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val jobRepository = context.getBean<JobRepository>()
        val batch = context.getBean<BatchDsl>()
        var repositoryCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                repository(
                    object : JobRepository by jobRepository {
                        override fun update(jobExecution: JobExecution) {
                            ++repositoryCallCount
                            jobRepository.update(jobExecution)
                        }
                    }
                )
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(repositoryCallCount).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun testObjectListener() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var beforeJobCallCount = 0
        var afterJobCallCount = 0

        @Suppress("unused")
        class TestListener {
            @BeforeJob
            fun beforeJob() {
                ++beforeJobCallCount
            }

            @AfterJob
            fun afterJob() {
                ++afterJobCallCount
            }
        }

        // when
        val job = batch {
            job("testJob") {
                listener(TestListener())
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(beforeJobCallCount).isEqualTo(1)
        assertThat(afterJobCallCount).isEqualTo(1)
    }

    @Test
    fun testJobExecutionListener() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var beforeJobCallCount = 0
        var afterJobCallCount = 0

        // when
        val job = batch {
            job("testJob") {
                listener(
                    object : JobExecutionListener {
                        override fun beforeJob(jobExecution: JobExecution) {
                            ++beforeJobCallCount
                        }

                        override fun afterJob(jobExecution: JobExecution) {
                            ++afterJobCallCount
                        }
                    }
                )
                step("testStep") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(beforeJobCallCount).isEqualTo(1)
        assertThat(afterJobCallCount).isEqualTo(1)
    }

    @Test
    fun testPreventRestart() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var tryCount = 0

        // when
        val job = batch {
            job("testJob") {
                preventRestart()
                step("testStep") {
                    tasklet { _, _ ->
                        if (tryCount == 0) {
                            ++tryCount
                            throw RuntimeException()
                        }
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // then
        val jobExecution = jobLauncher.run(job, JobParameters())
        assertThat(jobExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThatThrownBy {
            jobLauncher.run(job, JobParameters())
        }.hasMessageContaining("JobInstance already exists and is not restartable")
        assertThat(tryCount).isEqualTo(1)
    }

    @Test
    fun testConfigurationAfterStep() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var validatorCallCount = 0
        var step1CallCount = 0
        var step2CallCount = 0

        // when
        val job = batch {
            job("testJob") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++step1CallCount
                        RepeatStatus.FINISHED
                    }
                }
                step("testStep2") {
                    tasklet { _, _ ->
                        ++step2CallCount
                        RepeatStatus.FINISHED
                    }
                }
                validator {
                    ++validatorCallCount
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(step1CallCount).isEqualTo(1)
        assertThat(step2CallCount).isEqualTo(1)
        assertThat(validatorCallCount).isGreaterThanOrEqualTo(1)
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
            stepBuilderFactory,
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
