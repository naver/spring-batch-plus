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
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowJobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.core.task.TaskExecutor

/**
 * A dsl for [SimpleJobBuilder][org.springframework.batch.core.job.builder.SimpleJobBuilder].
 */
@BatchDslMarker
internal class SimpleJobBuilderDslAdapter internal constructor(
    private val simpleJobBuilderDsl: SimpleJobBuilderDsl
) : FlowBuilderDsl<FlowJobBuilder> {

    override fun stepBean(name: String) {
        this.simpleJobBuilderDsl.stepBean(name)
    }

    override fun step(name: String, stepInit: StepBuilderDsl.() -> Step) {
        this.simpleJobBuilderDsl.step(name, stepInit)
    }

    override fun step(step: Step) {
        this.simpleJobBuilderDsl.step(step)
    }

    override fun stepBean(name: String, stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun step(step: Step, stepTransitionInit: StepTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun flowBean(name: String) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun flow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun flow(flow: Flow) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun flowBean(name: String, flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun flow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
        flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun flow(flow: Flow, flowTransitionInit: FlowTransitionBuilderDsl<FlowJobBuilder>.() -> Unit) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun deciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun decider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<FlowJobBuilder>.() -> Unit
    ) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    override fun split(taskExecutor: TaskExecutor, splitInit: SplitBuilderDsl<FlowJobBuilder>.() -> Unit) {
        throw UnsupportedOperationException("SimpleJob can't process flow.")
    }

    internal fun build(): Job = this.simpleJobBuilderDsl.build()
}
