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
import javax.sql.DataSource

/**
 * Separated from FlowJobBuilderDslIntegrationTest since it's too big.
 */
internal class TransitionBuilderDslIntegrationTest {

    @Test
    fun testTransitionToStepBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        context.registerBean("transitionStep") {
            transitionStep
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stepBean("transitionStep")
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
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStepWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                step("transitionStep") {
                                    tasklet { _, _ ->
                                        ++transitionStepCallCount
                                        RepeatStatus.FINISHED
                                    }
                                }
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
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStepVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                step(transitionStep)
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
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStepBeanWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    throw RuntimeException("Error")
                }
            }
        }
        context.registerBean("transitionStep") {
            transitionStep
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
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
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStepWithInitAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                step(
                                    "transitionStep",
                                    {
                                        tasklet { _, _ ->
                                            ++transitionStepCallCount
                                            throw RuntimeException("Error")
                                        }
                                    }
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
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStepVariableWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    throw RuntimeException("Error")
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
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
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFlowBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        context.registerBean("transitionFlow") {
            transitionFlow
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                flowBean("transitionFlow")
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
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFlowWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                flow("transitionFlow") {
                                    step("transitionStep") {
                                        tasklet { _, _ ->
                                            ++transitionStepCallCount
                                            RepeatStatus.FINISHED
                                        }
                                    }
                                }
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
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFlowVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                flow(transitionFlow)
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
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFlowBeanWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        context.registerBean("transitionFlow") {
            transitionFlow
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                flowBean("transitionFlow") {
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
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFlowWithInitAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                flow(
                                    "transitionFlow",
                                    {
                                        step("transitionStep") {
                                            tasklet { _, _ ->
                                                ++transitionStepCallCount
                                                RepeatStatus.FINISHED
                                            }
                                        }
                                    }
                                ) {
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
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFlowWithVariableAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                flow(transitionFlow) {
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
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToDeciderBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val testDecider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus("SKIPPED")
        }
        context.registerBean("testDecider") {
            testDecider
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                deciderBean("testDecider") {
                                    on("COMPLETED") {
                                        fail()
                                    }
                                    on("SKIPPED") {
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
    fun testTransitionToDeciderVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val testDecider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus("SKIPPED")
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                decider(testDecider) {
                                    on("COMPLETED") {
                                        fail()
                                    }
                                    on("SKIPPED") {
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
    fun testTransitionToStop() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stop()
                            }
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToFlowBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        context.registerBean("transitionFlow") {
            transitionFlow
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToFlowBean("transitionFlow")
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToFlowWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToFlow("transitionFlow") {
                                    step("transitionStep") {
                                        tasklet { _, _ ->
                                            ++transitionStepCallCount
                                            RepeatStatus.FINISHED
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToFlowVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToFlow(transitionFlow)
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToFlowBeanWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }
        context.registerBean("transitionFlow") {
            transitionFlow
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToFlowBean("transitionFlow") {
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
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToFlowWithInitAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToFlow("transitionFlow", {
                                    step("transitionStep") {
                                        tasklet { _, _ ->
                                            ++transitionStepCallCount
                                            RepeatStatus.FINISHED
                                        }
                                    }
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
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToFlowVariableWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionFlow = batch {
            flow("transitionFlow") {
                step("transitionStep") {
                    tasklet { _, _ ->
                        ++transitionStepCallCount
                        RepeatStatus.FINISHED
                    }
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToFlow(transitionFlow) {
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
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToDeciderBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val testDecider = JobExecutionDecider { _, _ ->
            ++testDeciderCallCount
            FlowExecutionStatus.UNKNOWN
        }
        context.registerBean("testDecider") {
            testDecider
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToDeciderBean("testDecider") {
                                    on("UNKNOWN") {
                                        step("transitionStep") {
                                            tasklet { _, _ ->
                                                ++transitionStepCallCount
                                                RepeatStatus.FINISHED
                                            }
                                        }
                                    }
                                    on("*") {
                                        fail()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testDeciderCallCount).isEqualTo(2) // always run on restart
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToDeciderVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var testDeciderCallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToDecider(
                                    { _, _ ->
                                        ++testDeciderCallCount
                                        FlowExecutionStatus.UNKNOWN
                                    }
                                ) {
                                    on("UNKNOWN") {
                                        step("transitionStep") {
                                            tasklet { _, _ ->
                                                ++transitionStepCallCount
                                                RepeatStatus.FINISHED
                                            }
                                        }
                                    }
                                    on("*") {
                                        fail()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(testDeciderCallCount).isEqualTo(2) // always run on restart
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToStepBean() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        context.registerBean("transitionStep") {
            transitionStep
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToStepBean("transitionStep")
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToStepWithInit() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToStep("transitionStep") {
                                    tasklet { _, _ ->
                                        ++transitionStepCallCount
                                        RepeatStatus.FINISHED
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToStepVariable() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToStep(transitionStep)
                            }
                        }
                    }
                }
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.NOOP.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToStepBeanWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        context.registerBean("transitionStep") {
            transitionStep
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
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
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToStepWithInitAndTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                stopAndRestartToStep("transitionStep", {
                                    tasklet { _, _ ->
                                        ++transitionStepCallCount
                                        RepeatStatus.FINISHED
                                    }
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
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToStopAndRestartToStepVariableWithTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        var transitionStepCallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }
        val transitionStep = batch {
            step("transitionStep") {
                tasklet { _, _ ->
                    ++transitionStepCallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
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
            }
        }
        val firstJobExecution = jobLauncher.run(job, JobParameters())
        val secondJobExecution = jobLauncher.run(job, JobParameters())
        val thirdJobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(firstJobExecution.status).isEqualTo(BatchStatus.STOPPED)
        assertThat(firstJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.STOPPED.exitCode)
        assertThat(secondJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(secondJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(thirdJobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(thirdJobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
        assertThat(transitionStepCallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToEnd() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { contribution, _ ->
                    ++testStep1CallCount
                    contribution.exitStatus = ExitStatus.UNKNOWN
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("UNKNOWN") {
                                end()
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
        assertThat(testStep1CallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToEndWithStatus() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { contribution, _ ->
                    ++testStep1CallCount
                    contribution.exitStatus = ExitStatus.UNKNOWN
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("UNKNOWN") {
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
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus("TEST").exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
    }

    @Test
    fun testTransitionToFail() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val jobLauncher = context.getBean<JobLauncher>()
        val batch = context.getBean<BatchDsl>()
        var testStep1CallCount = 0
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    ++testStep1CallCount
                    RepeatStatus.FINISHED
                }
            }
        }

        // when
        val job = batch {
            job("testJob") {
                flows {
                    flow("testFlow") {
                        step(testStep1) {
                            on("COMPLETED") {
                                fail()
                            }
                        }
                    }
                }
            }
        }
        val jobExecution = jobLauncher.run(job, JobParameters())

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.FAILED)
        assertThat(jobExecution.exitStatus.exitCode).isEqualTo(ExitStatus.FAILED.exitCode)
        assertThat(testStep1CallCount).isEqualTo(1)
    }

    @Test
    fun testNoTransition() {
        // given
        val context = AnnotationConfigApplicationContext(TestConfiguration::class.java)
        val batch = context.getBean<BatchDsl>()
        val testStep1 = batch {
            step("testStep1") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }

        // when, then
        assertThatThrownBy {
            batch {
                job("testJob") {
                    flows {
                        flow("testFlow") {
                            step(testStep1) {
                                on("COMPLETED") {
                                }
                            }
                        }
                    }
                }
            }
        }.hasMessageContaining("should set transition")
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
