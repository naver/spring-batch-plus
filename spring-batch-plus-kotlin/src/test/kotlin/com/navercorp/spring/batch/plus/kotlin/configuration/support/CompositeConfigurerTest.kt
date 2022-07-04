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

package com.navercorp.spring.batch.plus.kotlin.configuration.support

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CompositeConfigurerTest {

    @Test
    fun testAdd() {
        // given
        var configurerCallCount = 0

        // when
        val compositeConfigurer = CompositeConfigurer<String>()
        compositeConfigurer.add {
            ++configurerCallCount
        }
        "test".apply(compositeConfigurer)

        // then
        assertThat(configurerCallCount).isEqualTo(1)
    }

    @Test
    fun testAddOther() {
        // given
        var configurer1CallCount = 0
        var configurer2CallCount = 0

        // when
        val compositeConfigurer = CompositeConfigurer<String>()
        compositeConfigurer.add(
            CompositeConfigurer<String>().apply {
                add {
                    ++configurer1CallCount
                }
                add {
                    ++configurer2CallCount
                }
            }
        )
        "test".apply(compositeConfigurer)

        // then
        assertThat(configurer1CallCount).isEqualTo(1)
        assertThat(configurer2CallCount).isEqualTo(1)
    }
}
