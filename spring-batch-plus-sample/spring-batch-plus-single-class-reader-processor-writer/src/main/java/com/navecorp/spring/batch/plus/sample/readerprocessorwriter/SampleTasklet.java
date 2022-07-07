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

package com.navecorp.spring.batch.plus.sample.readerprocessorwriter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

import com.navercorp.spring.batch.plus.item.ItemStreamReaderProcessorWriter;

@Component
@StepScope
class SampleTasklet implements ItemStreamReaderProcessorWriter<Integer, String> {
	private int count = 0;

	@NotNull
	@Override
	public Flux<Integer> readFlux(@NotNull ExecutionContext executionContext) {
		return Flux.generate(sink -> {
			if (count < 20) {
				sink.next(count);
				++count;
			} else {
				sink.complete();
			}
		});
	}

	@Override
	public String process(@NotNull Integer item) {
		return "'" + item.toString() + "'";
	}

	@Override
	public void write(@NotNull List<? extends String> items) {
		System.out.println(items);
	}
}
