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

package com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.listener.bean.steplistener

import org.springframework.batch.core.listener.ChunkListener
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.stereotype.Component

@Component
class TestListener : ChunkListener<Int, String> {
    override fun beforeChunk(chunk: Chunk<Int>) {
        println("beforeChunk: $chunk")
    }

    override fun afterChunk(chunk: Chunk<String>) {
        println("afterChunk: $chunk")
    }

    override fun onChunkError(
        exception: Exception,
        chunk: Chunk<String>,
    ) {
    }
}
