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

package com.navercorp.spring.batch.plus.kotlin.configuration.step

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.Step
import org.springframework.batch.core.step.builder.FlowStepBuilder

internal class FlowStepBuilderDslTest {

    @Test
    fun buildShouldReturnValueFromDelegate() {
        val mockStep = mockk<Step>()
        val flowStepBuilder = mockk<FlowStepBuilder>(relaxed = true) {
            every { build() } returns mockStep
        }

        val actual = FlowStepBuilderDsl(mockk(), flowStepBuilder).build()

        assertThat(actual).isEqualTo(mockStep)
    }
}
