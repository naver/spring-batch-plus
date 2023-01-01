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
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.SimpleJobBuilder
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.getBean

/**
 * A dsl for [SimpleJobBuilder][org.springframework.batch.core.job.builder.SimpleJobBuilder].
 */
@BatchDslMarker
internal class SimpleJobBuilderDsl internal constructor(
    private val dslContext: DslContext,
    private val simpleJobBuilder: SimpleJobBuilder
) {

    fun stepBean(name: String) {
        val step = this.dslContext.beanFactory.getBean<Step>(name)
        step(step)
    }

    fun step(name: String, stepInit: StepBuilderDsl.() -> Step) {
        val stepBuilder = StepBuilder(name, this.dslContext.jobRepository)
        val step = StepBuilderDsl(this.dslContext, stepBuilder).let(stepInit)
        step(step)
    }

    fun step(step: Step) {
        this.simpleJobBuilder.next(step)
    }

    internal fun build(): Job = this.simpleJobBuilder.build()
}
