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
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.core.task.TaskExecutor

/**
 * A dsl for [FlowBuilder][org.springframework.batch.core.job.builder.FlowBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
interface FlowBuilderDsl<T : Any> {

    /**
     * Add step by bean name.
     */
    fun stepBean(name: String)

    /**
     * Add step.
     */
    fun step(name: String, stepInit: StepBuilderDsl.() -> Step)

    /**
     * Add step.
     */
    fun step(step: Step)

    /**
     * Add step by bean name with transition.
     */
    fun stepBean(name: String, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit)

    /**
     * Add step with transition.
     */
    fun step(
        name: String,
        stepInit: StepBuilderDsl.() -> Step,
        stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit
    )

    /**
     * Add step with transition.
     */
    fun step(step: Step, stepTransitionInit: StepTransitionBuilderDsl<T>.() -> Unit)

    /**
     * Add flow by bean name.
     */
    fun flowBean(name: String)

    /**
     * Add flow.
     */
    fun flow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit)

    /**
     * Add flow.
     */
    fun flow(flow: Flow)

    /**
     * Add flow by bean name with transition.
     */
    fun flowBean(name: String, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit)

    /**
     * Add flow with transition.
     */
    fun flow(
        name: String,
        flowInit: FlowBuilderDsl<Flow>.() -> Unit,
        flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit
    )

    /**
     * Add flow with transition.
     */
    fun flow(flow: Flow, flowTransitionInit: FlowTransitionBuilderDsl<T>.() -> Unit)

    /**
     * Add decider by bean name with transition.
     */
    fun deciderBean(
        name: String,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit
    )

    /**
     * Add decider with transition.
     */
    fun decider(
        decider: JobExecutionDecider,
        deciderTransitionInit: DeciderTransitionBuilderDsl<T>.() -> Unit
    )

    /**
     * Split flow.
     *
     * @see [FlowBuilder.split][org.springframework.batch.core.job.builder.FlowBuilder.split]
     */
    fun split(taskExecutor: TaskExecutor, splitInit: SplitBuilderDsl<T>.() -> Unit)
}
