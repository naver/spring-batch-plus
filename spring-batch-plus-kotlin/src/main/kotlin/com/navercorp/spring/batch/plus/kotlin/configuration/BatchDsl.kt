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
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.BeanFactory

/**
 * A dsl for spring batch job, step, flow.
 *
 * @since 0.1.0
 */
@BatchDslMarker
class BatchDsl internal constructor(
    private val dslContext: DslContext,
) {
    constructor(
        beanFactory: BeanFactory,
        jobRepository: JobRepository,
    ) : this(
        DslContext(
            beanFactory,
            jobRepository,
        ),
    )

    operator fun <T : Any> invoke(init: BatchDsl.() -> T): T = init()

    /**
     * Make a new job.
     */
    fun job(name: String, init: JobBuilderDsl.() -> Unit): Job {
        val jobRepository = this.dslContext.jobRepository
        val jobBuilder = JobBuilder(name, jobRepository)
        return JobBuilderDsl(this.dslContext, jobBuilder).apply(init).build()
    }

    /**
     * Make a new step.
     */
    fun step(name: String, init: StepBuilderDsl.() -> Step): Step {
        val jobRepository = this.dslContext.jobRepository
        val stepBuilder = StepBuilder(name, jobRepository)
        return StepBuilderDsl(this.dslContext, stepBuilder).let(init)
    }

    /**
     * Make a new flow.
     */
    fun flow(name: String, init: FlowBuilderDsl<Flow>.() -> Unit): Flow {
        val flowBuilder = FlowBuilder<Flow>(name)
        return ConcreteFlowBuilderDsl(this.dslContext, flowBuilder).apply(init).build()
    }
}
