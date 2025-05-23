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

package com.navercorp.spring.batch.plus.sample.iterable.readerwriter;

import java.util.Iterator;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderWriter;

@Component
@StepScope
class SampleTasklet implements ItemStreamIterableReaderWriter<Integer> {

	@Value("#{jobParameters['totalCount']}")
	private long totalCount;

	private int count = 0;

	@NonNull
	@Override
	public Iterable<? extends Integer> readIterable(@NonNull ExecutionContext executionContext) {
		System.out.println("totalCount: " + totalCount);
		return () -> new Iterator<>() {
			@Override
			public boolean hasNext() {
				return count < totalCount;
			}

			@Override
			public Integer next() {
				return count++;
			}
		};
	}

	@Override
	public void write(@NonNull Chunk<? extends Integer> chunk) {
		System.out.println(chunk.getItems());
	}
}
