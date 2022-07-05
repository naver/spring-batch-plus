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

/**
 * A dsl for [FlowBuilder][org.springframework.batch.core.job.builder.FlowBuilder].
 *
 * @since 0.1.0
 */
@BatchDslMarker
open class FlowBuilderDsl<T : Any> internal constructor(
    @Suppress("unused")
    private val dslContext: DslContext,
    private var flowBuilder: FlowBuilder<T>
) {
    fun build(): T {
        TODO()
    }
}
