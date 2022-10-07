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
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.repeat.RepeatStatus

internal class SimpleJobBuilderDslAdapterTest {

    @Test
    fun testUnsupportedCall() {
        // given
        val simpleJobBuilderDsl = SimpleJobBuilderDsl(DslContext(mock(), mock(), mock()), mock())
        val simpleJobBuilderDslAdapter = SimpleJobBuilderDslAdapter(simpleJobBuilderDsl)

        // when, then
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.stepBean("testStep") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.step("testStep", { tasklet { _, _ -> RepeatStatus.FINISHED } }) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.step(mock<Step>()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flowBean("testFlow")
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow("testFlow") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow(mock())
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flowBean("testFlow") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow("testFlow", {}) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.flow(mock<Flow>()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.deciderBean("testDecider") {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
        assertThatThrownBy {
            simpleJobBuilderDslAdapter.decider(mock()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)

        assertThatThrownBy {
            simpleJobBuilderDslAdapter.split(mock()) {
            }
        }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}
