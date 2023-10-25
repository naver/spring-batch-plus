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

/**
 * A dsl for [FlowBuilder.TransitionBuilder][org.springframework.batch.core.job.builder.FlowBuilder.TransitionBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class TransitionBuilderDsl<T : Any> internal constructor(
    private val dslContext: DslContext,
    private val baseTransitionBuilder: FlowBuilder.TransitionBuilder<T>,
) {
    private var flowBuilder: FlowBuilder<T>? = null

    /**
     * Transition to step by bean name.
     */
    fun stepBean(name: String) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        step(step)
    }

    /**
     * Transition to step.
     */
    fun step(name: String, stepInit: StepBuilderDsl.() -> Step) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        step(step)
    }

    /**
     * Transition to step.
     */
    fun step(step: Step) {
        this.flowBuilder = this.baseTransitionBuilder.to(step)
            .from(step)
    }

    /**
     * Transition to step by bean name and set another transition.
     */
    fun stepBean(name: String, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        step(step, stepTransitionInit)
    }

    /**
     * Transition to step and set another transition.
     */
    fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        step(step, stepTransitionInit)
    }

    /**
     * Transition to step and set another transition.
     */
    fun step(step: Step, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit) {
        val baseFlowBuilder = this.baseTransitionBuilder.to(step).from(step)
        this.flowBuilder = StepTransitionBuilderDsl<T>(this.dslContext, step, baseFlowBuilder)
            .apply(stepTransitionInit)
            .build()
    }

    /**
     * Transition to flow by bean name.
     */
    fun flowBean(name: String) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        flow(flow)
    }

    /**
     * Transition to flow.
     */
    fun flow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit) {
        val flowBuilder = FlowBuilder<Flow>(name)
        val flow = ConcreteFlowBuilderDsl(this.dslContext, flowBuilder).apply(flowInit)
            .build()
        flow(flow)
    }

    /**
     * Transition to flow.
     */
    fun flow(flow: Flow) {
        this.flowBuilder = this.baseTransitionBuilder.to(flow)
            .from(flow)
    }

    /**
     * Transition to flow by bean name and set another transition.
     */
    fun flowBean(name: String, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        flow(flow, flowTransitionInit)
    }

    /**
     * Transition to flow and set another transition.
     */
    fun flow(
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
     * Transition to flow and set another transition.
     */
    fun flow(flow: Flow, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit) {
        val baseFlowBuilder = this.baseTransitionBuilder.to(flow)
            .from(flow)
        this.flowBuilder = FlowTransitionBuilderDsl<T>(this.dslContext, flow, baseFlowBuilder)
            .apply(flowTransitionInit)
            .build()
    }

    /**
     * Transition to decider by bean name and set another transition.
     */
    fun deciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val decider = this.dslContext.beanFactory.getBean<JobExecutionDecider>(name)
        decider(decider, deciderTransitionInit)
    }

    /**
     * Transition to decider and set another transition.
     */
    fun decider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val baseUnterminatedFlowBuilder = this.baseTransitionBuilder.to(decider)
            .from(decider)
        this.flowBuilder = DeciderTransitionBuilderDsl<T>(
            this.dslContext,
            decider,
            baseUnterminatedFlowBuilder,
        )
            .apply(deciderTransitionInit)
            .build()
    }

    /**
     * Transition to stop.
     */
    fun stop() {
        this.flowBuilder = this.baseTransitionBuilder.stop()
    }

    /**
     * Transition to stop and restart with flow by bean name if the flow is restarted.
     */
    fun stopAndRestartToFlowBean(name: String) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        stopAndRestartToFlow(flow)
    }

    /**
     * Transition to stop and restart with flow if the flow is restarted.
     */
    fun stopAndRestartToFlow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit) {
        val flowBuilder = FlowBuilder<Flow>(name)
        val flow = ConcreteFlowBuilderDsl(this.dslContext, flowBuilder).apply(flowInit)
            .build()
        stopAndRestartToFlow(flow)
    }

    /**
     * Transition to stop and restart with flow if the flow is restarted.
     */
    fun stopAndRestartToFlow(flow: Flow) {
        this.flowBuilder = this.baseTransitionBuilder.stopAndRestart(flow)
    }

    /**
     * Transition to stop and restart with flow if the flow is restarted.
     */
    fun stopAndRestartToFlowBean(
        name: String,
        flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        stopAndRestartToFlow(flow, flowTransitionInit)
    }

    /**
     * Transition to stop and restart with flow if the flow is restarted.
     */
    fun stopAndRestartToFlow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
        flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val flowBuilder = FlowBuilder<Flow>(name)
        val flow = ConcreteFlowBuilderDsl(this.dslContext, flowBuilder).apply(flowInit)
            .build()
        stopAndRestartToFlow(flow, flowTransitionInit)
    }

    /**
     * Transition to stop and restart with flow if the flow is restarted.
     */
    fun stopAndRestartToFlow(flow: Flow, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit) {
        val baseFlowBuilder = this.baseTransitionBuilder.stopAndRestart(flow)
            .from(flow)

        this.flowBuilder = FlowTransitionBuilderDsl<T>(this.dslContext, flow, baseFlowBuilder)
            .apply(flowTransitionInit)
            .build()
    }

    /**
     * Transition to stop and restart with decider by bean name if the flow is restarted.
     */
    fun stopAndRestartToDeciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val decider = this.dslContext.beanFactory.getBean<JobExecutionDecider>(name)
        stopAndRestartToDecider(decider, deciderTransitionInit)
    }

    /**
     * Transition to stop and restart with decider if the flow is restarted.
     */
    fun stopAndRestartToDecider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val baseFlowBuilder = this.baseTransitionBuilder.stopAndRestart(decider).from(decider)
        this.flowBuilder = DeciderTransitionBuilderDsl(this.dslContext, decider, baseFlowBuilder)
            .apply(deciderTransitionInit)
            .build()
    }

    /**
     * Transition to stop and restart with step by bean name if the flow is restarted.
     */
    fun stopAndRestartToStepBean(name: String) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        stopAndRestartToStep(step)
    }

    /**
     * Transition to stop and restart with step if the flow is restarted.
     */
    fun stopAndRestartToStep(name: String, stepInit: StepBuilderDsl.() -> Step) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        stopAndRestartToStep(step)
    }

    /**
     * Transition to stop and restart with step if the flow is restarted.
     */
    fun stopAndRestartToStep(step: Step) {
        this.flowBuilder = this.baseTransitionBuilder.stopAndRestart(step)
    }

    /**
     * Transition to stop and restart with step if the flow is restarted.
     */
    fun stopAndRestartToStepBean(
        name: String,
        stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        stopAndRestartToStep(step, stepTransitionInit)
    }

    /**
     * Transition to stop and restart with flow if the flow is restarted.
     */
    fun stopAndRestartToStep(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit,
    ) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        stopAndRestartToStep(step, stepTransitionInit)
    }

    /**
     * Transition to stop and restart with step if the flow is restarted.
     */
    fun stopAndRestartToStep(step: Step, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit) {
        val baseFlowBuilder = this.baseTransitionBuilder.stopAndRestart(step)
            .from(step)

        this.flowBuilder = StepTransitionBuilderDsl<T>(this.dslContext, step, baseFlowBuilder)
            .apply(stepTransitionInit)
            .build()
    }

    /**
     * Transition to successful end.
     */
    fun end() {
        this.flowBuilder = this.baseTransitionBuilder.end()
    }

    /**
     * Transition to successful end with the status provided.
     */
    fun end(status: String) {
        this.flowBuilder = this.baseTransitionBuilder.end(status)
    }

    /**
     * Transition to fail.
     */
    fun fail() {
        this.flowBuilder = this.baseTransitionBuilder.fail()
    }

    internal fun build(): FlowBuilder<T> {
        return checkNotNull(this.flowBuilder) {
            "should set transition."
        }
    }
}
