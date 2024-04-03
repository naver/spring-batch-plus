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

package com.navercorp.spring.batch.plus.sample.iterable.callback

import com.navercorp.spring.batch.plus.item.adapter.ItemStreamIterableReaderProcessorWriter
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
open class SampleTasklet(
    @Value("#{jobParameters['totalCount']}") private var totalCount: Long,
) : ItemStreamIterableReaderProcessorWriter<Int, String> {
    private var count = 0

    override fun onOpenRead(executionContext: ExecutionContext) {
        println("onOpenRead")
    }

    override fun readIterable(executionContext: ExecutionContext): Iterable<Int> {
        println("totalCount: $totalCount")
        return Iterable {
            object : Iterator<Int> {
                override fun hasNext(): Boolean {
                    return count < totalCount
                }

                override fun next(): Int {
                    return count++
                }
            }
        }
    }

    override fun onUpdateRead(executionContext: ExecutionContext) {
        println("onUpdateRead")
    }

    override fun onCloseRead() {
        println("onCloseRead")
    }

    override fun process(item: Int): String? {
        return "'$item'"
    }

    override fun onOpenWrite(executionContext: ExecutionContext) {
        println("onOpenWrite")
    }

    override fun write(chunk: Chunk<out String>) {
        println(chunk.items)
    }

    override fun onUpdateWrite(executionContext: ExecutionContext) {
        println("onUpdateWrite")
        executionContext.putString("samplekey", "samplevalue")
    }

    override fun onCloseWrite() {
        println("onCloseWrite")
    }
}
