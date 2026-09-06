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
import io.micrometer.observation.ObservationRegistry
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.FlowJobBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.builder.JobBuilderHelper
import org.springframework.batch.core.job.builder.JobFlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.job.parameters.JobParametersIncrementer
import org.springframework.batch.core.job.parameters.JobParametersValidator
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.batch.core.step.Step
import org.springframework.core.task.TaskExecutor

/**
 * Entry DSL for a job block.
 * Common job settings are applied to the base [JobBuilder] before the block is
 * replayed into either the simple-job or flow-job builder path.
 *
 * @since 0.1.0
 */
@BatchDslMarker
class JobBuilderDsl internal constructor(
    private val dslContext: DslContext,
    private val jobBuilder: JobBuilder,
) : FlowBuilderDsl<FlowJobBuilder> {
    private val lazyConfigurer = LazyConfigurer<JobBuilderHelper<*>>()

    private val lazyFlowConfigurer = LazyConfigurer<FlowBuilderDsl<FlowJobBuilder>>()

    private var requiresFlowJob = false

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
     * Set for [JobBuilder.observationRegistry][org.springframework.batch.core.job.builder.JobBuilderHelper.observationRegistry].
     */
    fun observationRegistry(observationRegistry: ObservationRegistry) {
        lazyConfigurer.add {
            it.observationRegistry(observationRegistry)
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

    override fun stepBean(name: String) {
        this.lazyFlowConfigurer.add {
            it.stepBean(name)
        }
    }

    override fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
    ) {
        this.lazyFlowConfigurer.add {
            it.step(name, stepInit)
        }
    }

    override fun step(step: Step) {
        this.lazyFlowConfigurer.add {
            it.step(step)
        }
    }

    override fun stepBean(
        name: String,
        stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.stepBean(name, stepTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.step(name, stepInit, stepTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun step(
        step: Step,
        stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.step(step, stepTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun flowBean(name: String) {
        this.lazyFlowConfigurer.add {
            it.flowBean(name)
        }
        this.requiresFlowJob = true
    }

    override fun flow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.flow(name, flowInit)
        }
        this.requiresFlowJob = true
    }

    override fun flow(flow: Flow) {
        this.lazyFlowConfigurer.add {
            it.flow(flow)
        }
        this.requiresFlowJob = true
    }

    override fun flowBean(
        name: String,
        flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.flowBean(name, flowTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun flow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
        flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.flow(name, flowInit, flowTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun flow(
        flow: Flow,
        flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.flow(flow, flowTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun deciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.deciderBean(name, deciderTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun decider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.decider(decider, deciderTransitionInit)
        }
        this.requiresFlowJob = true
    }

    override fun split(
        taskExecutor: TaskExecutor,
        splitInit: SplitBuilderDsl<FlowJobBuilder>.() -> Unit,
    ) {
        this.lazyFlowConfigurer.add {
            it.split(taskExecutor, splitInit)
        }
        this.requiresFlowJob = true
    }

    internal fun build(): Job {
        this.jobBuilder.apply(this.lazyConfigurer)

        /**
         * A plain sequence of steps can use a simple job. A flow, decider, split, or explicit
         * transition requires a flow job because it introduces a flow graph.
         */
        return if (!requiresFlowJob) {
            /**
             * The first step is unknown until deferred declarations are replayed, so
             * [JobBuilder.start] cannot be called yet. `SimpleJobBuilder(JobBuilderHelper)` leaves
             * the step list open for replay.
             */
            val simpleJobBuilder = BatchBuilderBridge.toSimpleJobBuilder(this.jobBuilder)
            val simpleJobBuilderDsl = SimpleJobBuilderDsl(this.dslContext, simpleJobBuilder)
            SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)
                .apply(this.lazyFlowConfigurer)
                .build()
        } else {
            /**
             * The first state is unknown until deferred declarations are replayed, so
             * [JobBuilder.start] or [JobBuilder.flow] cannot be called yet.
             * `JobFlowBuilder(FlowJobBuilder)` leaves the initial state open for replay.
             */
            val flowJobBuilder = BatchBuilderBridge.toFlowJobBuilder(this.jobBuilder)
            val jobFlowBuilder = JobFlowBuilder(flowJobBuilder)
            val delegate = ConcreteFlowBuilderDsl(this.dslContext, jobFlowBuilder)
            FlowJobBuilderDsl(this.dslContext, delegate)
                .apply(this.lazyFlowConfigurer)
                .build()
        }
    }
}
