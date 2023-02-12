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

import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.step.builder.StepBuilder

internal class FlowStepBuilderDslTest {

    private val jobInstance = JobInstance(0L, "testJob")

    private val jobParameters = JobParameters()

    private fun flowStepBuilderDsl(flow: Flow): Step {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobRepository = mock(),
        )
        val stepBuilder = StepBuilder("testStep", mock())
        val flowStepBuilder = stepBuilder.flow(flow)

        return FlowStepBuilderDsl(dslContext, flowStepBuilder).build()
    }

    @Test
    fun testBuild() {
        // given
        var firstStepCallCount = 0
        var secondStepCallCount = 0

        // when
        val flow = FlowBuilder<Flow>("testFlow")
            .start(
                object : Step {
                    override fun getName(): String {
                        return "step1"
                    }

                    override fun isAllowStartIfComplete(): Boolean {
                        return false
                    }

                    override fun getStartLimit(): Int {
                        return 1
                    }

                    override fun execute(stepExecution: StepExecution) {
                        ++firstStepCallCount
                        stepExecution.apply {
                            status = BatchStatus.COMPLETED
                            exitStatus = ExitStatus.COMPLETED
                        }
                    }
                }
            )
            .next(
                object : Step {
                    override fun getName(): String {
                        return "step2"
                    }

                    override fun isAllowStartIfComplete(): Boolean {
                        return false
                    }

                    override fun getStartLimit(): Int {
                        return 1
                    }

                    override fun execute(stepExecution: StepExecution) {
                        ++secondStepCallCount
                        stepExecution.apply {
                            status = BatchStatus.COMPLETED
                            exitStatus = ExitStatus.COMPLETED
                        }
                    }
                }
            )
            .build()
        val step = flowStepBuilderDsl(flow)
        val jobExecution = JobExecution(jobInstance, jobParameters)
        val stepExecution = jobExecution.createStepExecution(step.name)
        step.execute(stepExecution)

        // then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(firstStepCallCount).isEqualTo(1)
        assertThat(secondStepCallCount).isEqualTo(1)
        assertThat(jobExecution.stepExecutions).hasSize(3)
        assertThat(jobExecution.stepExecutions.find { it.stepName == "testStep" }).isNotNull
        assertThat(jobExecution.stepExecutions.find { it.stepName == "step1" }).isNotNull
        assertThat(jobExecution.stepExecutions.find { it.stepName == "step2" }).isNotNull
    }
}
