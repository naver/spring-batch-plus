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

package com.navercorp.spring.batch.plus.sample.readerwriter

import com.navercorp.spring.batch.plus.item.ItemStreamReaderWriter
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
@StepScope
class SampleTasklet : ItemStreamReaderWriter<Int> {
    private var count = 0

    override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
        return Flux.generate { sink ->
            if (count < 20) {
                sink.next(count)
                ++count
            } else {
                sink.complete()
            }
        }
    }

    override fun write(items: List<Int>) {
        println(items)
    }
}
