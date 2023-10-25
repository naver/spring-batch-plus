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

/**
 * A dsl for step transition.
 *
 * @since 0.1.0
 */
@Suppress("DuplicatedCode")
@BatchDslMarker
class StepTransitionBuilderDsl<T : Any> internal constructor(
    private val dslContext: DslContext,
    private val step: Step,
    private val baseFlowBuilder: FlowBuilder<T>,
) {
    private var flowBuilder: FlowBuilder<T>? = null

    /**
     * Set transition for state.
     *
     * @see [org.springframework.batch.core.job.builder.FlowBuilder.on]
     */
    fun on(pattern: String, init: TransitionBuilderDsl<T>.() -> Unit) {
        val flowBuilder = this.flowBuilder

        val transitionBuilder = if (flowBuilder == null) {
            this.baseFlowBuilder.on(pattern)
        } else {
            flowBuilder.from(this.step)
                .on(pattern)
        }

        this.flowBuilder = TransitionBuilderDsl(this.dslContext, transitionBuilder).apply(init)
            .build()
    }

    internal fun build(): FlowBuilder<T> {
        return checkNotNull(this.flowBuilder) {
            "should set transition for step ${step.name}."
        }
    }
}
