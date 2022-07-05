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
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
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
import javax.sql.DataSource

internal class SimpleJobBuilderDslIntegrationTest {

    @Test
    fun testStepBean() {
        // given
        AnnotationConfigApplicationContext()
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0

        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val testStep2 = batch {
            step("testStep2") {
                tasklet { _, _ ->
                    ++testStep2CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        context.apply {
            registerBean("testStep1") {
                testStep1
            }
            registerBean("testStep2") {
                testStep2
            }
        }

        // when
        val job = batch {
            job("testJob") {
                steps {
                    stepBean("testStep1")
                    stepBean("testStep2")
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
    fun testStepWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0

        // when
        val job = batch {
            job("testJob") {
                steps {
                    step("testStep1") {
                        tasklet { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        }
                    }
                    step("testStep2") {
                        tasklet { _, _ ->
                            ++testStep2CallCount
                            RepeatStatus.FINISHED
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
    fun testStepWithVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val testStep2 = batch {
            step("testStep2") {
                tasklet { _, _ ->
                    ++testStep2CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                steps {
                    step(testStep1)
                    step(testStep2)
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
