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
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
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

internal class ConcreteFlowBuilderDslIntegrationTest {

    @Test
    fun testStepBean() {
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
        context.apply {
            registerBean("testStep1") {
                testStep1
            }
            registerBean("testStep2") {
                testStep2
            }
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                stepBean("testStep1")
                stepBean("testStep2")
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
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
        val testFlow = batch {
            flow("testFlow") {
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
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
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
        val testFlow = batch {
            flow("testFlow") {
                step(testStep1)
                step(testStep2)
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testStepBeanWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStep1CallCount = 0
        var transitionStep2CallCount = 0
        var testStep2CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    throw RuntimeException("Error")
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
        val testFlow = batch {
            flow("testFlow") {
                stepBean("testStep1") {
                    on("COMPLETED") {
                        step("transitionStep1") {
                            tasklet { _, _ ->
                                ++transitionStep1CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    on("FAILED") {
                        step("transitionStep2") {
                            tasklet { _, _ ->
                                ++transitionStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
                stepBean("testStep2") {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStep1CallCount).isEqualTo(0)
        assertThat(transitionStep2CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testStepWithInitAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStep1CallCount = 0
        var transitionStep2CallCount = 0
        var testStep2CallCount = 0

        // when
        val testFlow = batch {
            flow("testFlow") {
                step(
                    "testStep1",
                    {
                        tasklet { _, _ ->
                            ++testStep1CallCount
                            throw RuntimeException("Error")
                        }
                    }
                ) {
                    on("COMPLETED") {
                        step("transitionStep1") {
                            tasklet { _, _ ->
                                ++transitionStep1CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    on("FAILED") {
                        step("transitionStep2") {
                            tasklet { _, _ ->
                                ++transitionStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
                step(
                    "testStep2",
                    {
                        tasklet { _, _ ->
                            ++testStep2CallCount
                            RepeatStatus.FINISHED
                        }
                    }
                ) {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStep1CallCount).isEqualTo(0)
        assertThat(transitionStep2CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testStepWithVariableAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStep1CallCount = 0
        var transitionStep2CallCount = 0
        var testStep2CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    throw RuntimeException("Error")
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
        val testFlow = batch {
            flow("testFlow") {
                step(testStep1) {
                    on("COMPLETED") {
                        step("transitionStep1") {
                            tasklet { _, _ ->
                                ++transitionStep1CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    on("FAILED") {
                        step("transitionStep2") {
                            tasklet { _, _ ->
                                ++transitionStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
                step(testStep2) {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStep1CallCount).isEqualTo(0)
        assertThat(transitionStep2CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0
        val testFlow1 = batch {
            flow("testFlow1") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
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
        val testFlow = batch {
            flow("testFlow") {
                flowBean("testFlow1")
                flowBean("testFlow2")
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testStep2CallCount = 0

        // when
        val testFlow = batch {
            flow("testFlow") {
                flow("testFlow1") {
                    step("testStep1") {
                        tasklet { _, _ ->
                            ++testStep1CallCount
                            RepeatStatus.FINISHED
                        }
                    }
                }
                flow("testFlow2") {
                    step("testStep2") {
                        tasklet { _, _ ->
                            ++testStep2CallCount
                            RepeatStatus.FINISHED
                        }
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
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
        val testFlow1 = batch {
            flow("testFlow1") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
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
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                flow(testFlow1)
                flow(testFlow2)
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowBeanWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStep1CallCount = 0
        var transitionStep2CallCount = 0
        var testStep2CallCount = 0
        val testFlow1 = batch {
            flow("testFlow1") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
                        throw RuntimeException("Error")
                    }
                }
            }
        }
        val testFlow2 = batch {
            flow("testFlow2") {
                step("testStep2") {
                    tasklet { _, _ ->
                        ++testStep2CallCount
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
        val testFlow = batch {
            flow("testFlow") {
                flowBean("testFlow1") {
                    on("COMPLETED") {
                        step("transitionStep1") {
                            tasklet { _, _ ->
                                ++transitionStep1CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    on("FAILED") {
                        step("transitionStep2") {
                            tasklet { _, _ ->
                                ++transitionStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
                flowBean("testFlow2") {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStep1CallCount).isEqualTo(0)
        assertThat(transitionStep2CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithInitAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStep1CallCount = 0
        var transitionStep2CallCount = 0
        var testStep2CallCount = 0

        // when
        val testFlow = batch {
            flow("testFlow") {
                flow(
                    "testFlow1",
                    {
                        step("testStep1") {
                            tasklet { _, _ ->
                                ++testStep1CallCount
                                throw RuntimeException("Error")
                            }
                        }
                    }
                ) {
                    on("COMPLETED") {
                        step("transitionStep1") {
                            tasklet { _, _ ->
                                ++transitionStep1CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    on("FAILED") {
                        step("transitionStep2") {
                            tasklet { _, _ ->
                                ++transitionStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
                flow(
                    "testFlow2",
                    {
                        step("testStep2") {
                            tasklet { _, _ ->
                                ++testStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                ) {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStep1CallCount).isEqualTo(0)
        assertThat(transitionStep2CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testFlowWithVariableAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStep1CallCount = 0
        var transitionStep2CallCount = 0
        var testStep2CallCount = 0
        val testFlow1 = batch {
            flow("testFlow1") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
                        throw RuntimeException("Error")
                    }
                }
            }
        }
        val testFlow2 = batch {
            flow("testFlow2") {
                step("testStep2") {
                    tasklet { _, _ ->
                        ++testStep2CallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                flow(testFlow1) {
                    on("COMPLETED") {
                        step("transitionStep1") {
                            tasklet { _, _ ->
                                ++transitionStep1CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                    on("FAILED") {
                        step("transitionStep2") {
                            tasklet { _, _ ->
                                ++transitionStep2CallCount
                                RepeatStatus.FINISHED
                            }
                        }
                    }
                }
                flow(testFlow2) {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStep1CallCount).isEqualTo(0)
        assertThat(transitionStep2CallCount).isEqualTo(1)
        assertThat(testStep2CallCount).isEqualTo(1)
    }

    @Test
    fun testDeciderBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testDeciderCallCount = 0
        val testDecider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus.COMPLETED
        }
        context.registerBean("testDecider") {
            testDecider
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                deciderBean("testDecider") {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun testDeciderBeanNotFirst() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        val testDecider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus.COMPLETED
        }
        context.registerBean("testDecider") {
            testDecider
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
                        RepeatStatus.FINISHED
                    }
                }
                deciderBean("testDecider") {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun testDeciderWithVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testDeciderCallCount = 0
        val decider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus.COMPLETED
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                decider(decider) {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun testDeciderWithVariableNotFirst() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        val decider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus.COMPLETED
        }

        // when
        val testFlow = batch {
            flow("testFlow") {
                step("testStep1") {
                    tasklet { _, _ ->
                        ++testStep1CallCount
                        RepeatStatus.FINISHED
                    }
                }
                decider(decider) {
                    on("COMPLETED") {
                        end("TEST")
                    }
                }
            }
        }
        val job = batch {
            job("testJob") {
                flow(testFlow)
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testDeciderCallCount).isEqualTo(1)
    }

    @Test
    fun testSplit() {
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
        val testFlow = batch {
            flow("testFlow") {
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
        val job = batch {
            job("testJob") {
                flow(testFlow)
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
