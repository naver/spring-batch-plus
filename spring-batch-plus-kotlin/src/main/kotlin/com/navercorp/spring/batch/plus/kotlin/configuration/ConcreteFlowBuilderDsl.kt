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
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.getBean
import org.springframework.core.task.TaskExecutor

/**
 * A concrete implementation for [FlowBuilder][org.springframework.batch.core.job.builder.FlowBuilder].
 */
@BatchDslMarker
internal class ConcreteFlowBuilderDsl<T : Any> internal constructor(
    private val dslContext: DslContext,
    private var flowBuilder: FlowBuilder<T>,
) : FlowBuilderDsl<T> {
    private var started = false

    /**
     * Add step by bean name.
     */
    override fun stepBean(name: String) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        step(step)
    }

    /**
     * Add step.
     */
    override fun step(name: String, stepInit: StepBuilderDsl.() -> Step) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        step(step)
    }

    /**
     * Add step.
     */
    override fun step(step: Step) {
        val baseFlowBuilder = if (!this.started) {
            this.started = true
            this.flowBuilder.start(step)
        } else {
            this.flowBuilder.next(step)
        }

        this.flowBuilder = baseFlowBuilder
    }

    /**
     * Add step by bean name with transition.
     */
    override fun stepBean(name: String, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        step(step, stepTransitionInit)
    }

    /**
     * Add step with transition.
     */
    override fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        step(step, stepTransitionInit)
    }

    /**
     * Add step with transition.
     */
    override fun step(step: Step, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit) {
        val baseFlowBuilder = if (!this.started) {
            this.started = true
            this.flowBuilder.start(step)
        } else {
            this.flowBuilder.next(step)
        }

        this.flowBuilder = StepTransitionBuilderDsl<T>(this.dslContext, step, baseFlowBuilder)
            .apply(stepTransitionInit)
            .build()
    }

    /**
     * Add flow by bean name.
     */
    override fun flowBean(name: String) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        flow(flow)
    }

    /**
     * Add flow.
     */
    override fun flow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit) {
        val flowBuilder = FlowBuilder<Flow>(name)
        val flow = ConcreteFlowBuilderDsl(this.dslContext, flowBuilder).apply(flowInit)
            .build()
        flow(flow)
    }

    /**
     * Add flow.
     */
    override fun flow(flow: Flow) {
        val baseFlowBuilder = if (!this.started) {
            this.started = true
            this.flowBuilder.start(flow)
        } else {
            this.flowBuilder.next(flow)
        }

        this.flowBuilder = baseFlowBuilder
    }

    /**
     * Add flow by bean name with transition.
     */
    override fun flowBean(name: String, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        flow(flow, flowTransitionInit)
    }

    /**
     * Add flow with transition.
     */
    override fun flow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
        flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val flowBuilder = FlowBuilder<Flow>(name)
        val flow = ConcreteFlowBuilderDsl(this.dslContext, flowBuilder).apply(flowInit)
            .build()
        flow(flow, flowTransitionInit)
    }

    /**
     * Add flow with transition.
     */
    override fun flow(flow: Flow, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit) {
        val baseFlowBuilder = if (!this.started) {
            this.started = true
            this.flowBuilder.start(flow)
        } else {
            this.flowBuilder.next(flow)
        }

        this.flowBuilder = FlowTransitionBuilderDsl<T>(this.dslContext, flow, baseFlowBuilder)
            .apply(flowTransitionInit)
            .build()
    }

    /**
     * Add decider by bean name with transition.
     */
    override fun deciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val decider = this.dslContext.beanFactory.getBean<JobExecutionDecider>(name)
        decider(decider, deciderTransitionInit)
    }

    /**
     * Add decider with transition.
     */
    override fun decider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val baseUnterminatedFlowBuilder = if (!started) {
            this.started = true
            this.flowBuilder.start(decider)
        } else {
            this.flowBuilder.next(decider)
        }

        this.flowBuilder = DeciderTransitionBuilderDsl<T>(this.dslContext, decider, baseUnterminatedFlowBuilder)
            .apply(deciderTransitionInit)
            .build()
    }

    /**
     * Split flow.
     *
     * @see [FlowBuilder.split][org.springframework.batch.core.job.builder.FlowBuilder.split]
     */
    override fun split(taskExecutor: TaskExecutor, splitInit: SplitBuilderDsl<T>.() -> Unit) {
        val splitBuilder = this.flowBuilder.split(taskExecutor)
        this.flowBuilder = SplitBuilderDsl<T>(this.dslContext, splitBuilder).apply(splitInit)
            .build()
    }

    internal fun build(): T = this.flowBuilder.build()
}
