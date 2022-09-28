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

import com.navercorp.spring.batch.plus.kotlin.configuration.support.BatchDslMarker
import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import com.navercorp.spring.batch.plus.kotlin.configuration.support.LazyConfigurer
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.JobParametersIncrementer
import org.springframework.batch.core.JobParametersValidator
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowJobBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.builder.JobBuilderHelper
import org.springframework.batch.core.job.builder.JobFlowBuilder
import org.springframework.batch.core.job.builder.SimpleJobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.repository.JobRepository
import org.springframework.core.task.TaskExecutor

/**
 * A dsl for [JobBuilder][org.springframework.batch.core.job.builder.JobBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class JobBuilderDsl internal constructor(
    private val dslContext: DslContext,
    private val jobBuilder: JobBuilder
) : FlowBuilderDsl<FlowJobBuilder> {

    private var lazyConfigurer = LazyConfigurer<JobBuilderHelper<*>>()

    private var lazyFlowConfigurer = LazyConfigurer<FlowBuilderDsl<FlowJobBuilder>>()

    /**
     * Set for [JobBuilder.validator][org.springframework.batch.core.job.builder.JobBuilderHelper.validator].
     */
    fun validator(jobParametersValidator: JobParametersValidator) {
        lazyConfigurer.add {
            it.validator(jobParametersValidator)
        }
    }

    /**
     * Set for [JobBuilder.incrementer][org.springframework.batch.core.job.builder.JobBuilderHelper.incrementer].
     */
    fun incrementer(jobParametersIncrementer: JobParametersIncrementer) {
        lazyConfigurer.add {
            it.incrementer(jobParametersIncrementer)
        }
    }

    /**
     * Set for [JobBuilder.repository][org.springframework.batch.core.job.builder.JobBuilderHelper.repository].
     */
    fun repository(jobRepository: JobRepository) {
        lazyConfigurer.add {
            it.repository(jobRepository)
        }
    }

    /**
     * Set listener processing followings.
     *
     * - [org.springframework.batch.core.annotation.BeforeJob]
     * - [org.springframework.batch.core.annotation.AfterJob]
     */
    fun listener(listener: Any) {
        lazyConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set job execution listener.
     */
    fun listener(listener: JobExecutionListener) {
        lazyConfigurer.add {
            it.listener(listener)
        }
    }

    /**
     * Set for [JobBuilder.preventRestart][org.springframework.batch.core.job.builder.JobBuilderHelper.preventRestart].
     */
    fun preventRestart() {
        lazyConfigurer.add {
            it.preventRestart()
        }
    }

    /**
     * Build [SimpleJobBuilder][org.springframework.batch.core.job.builder.SimpleJobBuilder] for job.
     */
    fun steps(init: SimpleJobBuilderDsl.() -> Unit): Job {
        this.jobBuilder.apply(this.lazyConfigurer)
        val simpleJobBuilder = SimpleJobBuilder(this.jobBuilder)
        return SimpleJobBuilderDsl(this.dslContext, simpleJobBuilder).apply(init)
            .build()
    }

    /**
     * Build [FlowJobBuilder][org.springframework.batch.core.job.builder.FlowJobBuilder] for job.
     */
    fun flows(init: FlowBuilderDsl<FlowJobBuilder>.() -> Unit): Job {
        this.jobBuilder.apply(this.lazyConfigurer)
        val flowJobBuilder = FlowJobBuilder(this.jobBuilder)
        val jobFlowBuilder = JobFlowBuilder(flowJobBuilder)
        val delegate = ConcreteFlowBuilderDsl(this.dslContext, jobFlowBuilder)
        return FlowJobBuilderDsl(this.dslContext, delegate).apply(init)
            .build()
    }

    override fun stepBean(name: String) {
        this.lazyFlowConfigurer.add {
            it.stepBean(name)
        }
    }

    override fun step(name: String, stepInit: StepBuilderDsl.() -> Step) {
        this.lazyFlowConfigurer.add {
            it.step(name, stepInit)
        }
    }

    override fun step(step: Step) {
        this.lazyFlowConfigurer.add {
            it.step(step)
        }
    }

    override fun stepBean(name: String, stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        this.lazyFlowConfigurer.add {
            it.stepBean(name, stepTransitionInit)
        }
    }

    override fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        this.lazyFlowConfigurer.add {
            it.step(name, stepInit, stepTransitionInit)
        }
    }

    override fun step(step: Step, stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        this.lazyFlowConfigurer.add {
            it.step(step, stepTransitionInit)
        }
    }

    override fun flowBean(name: String) {
        this.lazyFlowConfigurer.add {
            it.flowBean(name)
        }
    }

    override fun flow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit) {
        this.lazyFlowConfigurer.add {
            it.flow(name, flowInit)
        }
    }

    override fun flow(flow: Flow) {
        this.lazyFlowConfigurer.add {
            it.flow(flow)
        }
    }

    override fun flowBean(name: String, flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        this.lazyFlowConfigurer.add {
            it.flowBean(name, flowTransitionInit)
        }
    }

    override fun flow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
        flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        this.lazyFlowConfigurer.add {
            it.flow(name, flowInit, flowTransitionInit)
        }
    }

    override fun flow(flow: Flow, flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        this.lazyFlowConfigurer.add {
            it.flow(flow, flowTransitionInit)
        }
    }

    override fun deciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        this.lazyFlowConfigurer.add {
            it.deciderBean(name, deciderTransitionInit)
        }
    }

    override fun decider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        this.lazyFlowConfigurer.add {
            it.decider(decider, deciderTransitionInit)
        }
    }

    override fun split(taskExecutor: TaskExecutor, splitInit: SplitBuilderDsl<FlowJobBuilder>.() -> Unit) {
        this.lazyFlowConfigurer.add {
            it.split(taskExecutor, splitInit)
        }
    }

    internal fun build(): Job {
        this.jobBuilder.apply(this.lazyConfigurer)
        val flowJobBuilder = FlowJobBuilder(this.jobBuilder)
        val jobFlowBuilder = JobFlowBuilder(flowJobBuilder)
        val delegate = ConcreteFlowBuilderDsl(this.dslContext, jobFlowBuilder)
        return FlowJobBuilderDsl(this.dslContext, delegate).apply(this.lazyFlowConfigurer)
            .build()
    }
}
