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

import com.navercorp.spring.batch.plus.kotlin.configuration.support.DslContext
import org.junit.jupiter.api.Test
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.springframework.batch.core.observability.BatchStepObservationConvention
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.transaction.PlatformTransactionManager
import java.util.UUID

internal class StepBuilderDslTest {

    private fun stepBuilderDsl(stepBuilder: StepBuilder): StepBuilderDsl {
        val dslContext = DslContext(
            beanFactory = mock(),
            jobRepository = mock(),
        )

        return StepBuilderDsl(dslContext, stepBuilder)
    }

    @Test
    fun testObservationConvention() {
        // given
        val stepBuilder = spy(StepBuilder(UUID.randomUUID().toString(), mock()))
        val stepBuilderDsl = stepBuilderDsl(stepBuilder)

        // when
        val observationConvention = mock<BatchStepObservationConvention>()
        stepBuilderDsl.apply {
            observationConvention(observationConvention)
        }.tasklet(mock(), mock<PlatformTransactionManager>())

        // then
        verify(stepBuilder, atLeastOnce()).observationConvention(observationConvention)
    }
}
