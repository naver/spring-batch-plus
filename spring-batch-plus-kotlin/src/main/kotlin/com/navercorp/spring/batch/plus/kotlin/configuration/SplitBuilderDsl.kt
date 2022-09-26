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
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.beans.factory.getBean

/**
 * A dsl for [FlowBuilder.SplitBuilder][org.springframework.batch.core.job.builder.FlowBuilder.SplitBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
class SplitBuilderDsl<T : Any> internal constructor(
    private val dslContext: DslContext,
    private val splitBuilder: FlowBuilder.SplitBuilder<T>
) {
    private var flows = mutableListOf<Flow>()

    /**
     * Add flow to split by bean name.
     */
    fun flowBean(name: String) {
        val flow = this.dslContext.beanFactory.getBean<Flow>(name)
        flow(flow)
    }

    /**
     * Add flow to split.
     */
    fun flow(name: String, flowInit: FlowBuilderDsl<Flow>.() -> Unit) {
        val flowBuilder = FlowBuilder<Flow>(name)
        val flow = FlowBuilderDsl(this.dslContext, flowBuilder).apply(flowInit)
            .build()
        flow(flow)
    }

    /**
     * Add flow to split.
     */
    fun flow(flow: Flow) {
        flows.add(flow)
    }

    internal fun build(): FlowBuilder<T> {
        check(this.flows.isNotEmpty()) {
            "should set at least one flow to split."
        }

        return this.splitBuilder.add(*flows.toTypedArray())
    }
}
