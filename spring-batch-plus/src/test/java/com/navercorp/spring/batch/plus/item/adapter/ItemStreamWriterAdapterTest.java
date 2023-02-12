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

package com.navercorp.spring.batch.plus.item.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.lang.NonNull;

class ItemStreamWriterAdapterTest {

	@Test
	void testOpen() {
		// given
		AtomicInteger onOpenWriteCallCount = new AtomicInteger();
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(
			new ItemStreamWriterDelegate<Integer>() {
				@Override
				public void onOpenWrite(@NonNull ExecutionContext executionContext) {
					onOpenWriteCallCount.incrementAndGet();
				}

				@Override
				public void write(@NonNull Chunk<? extends Integer> chunk) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void onCloseWrite() {
					throw new UnsupportedOperationException();
				}
			});

		// when
		itemStreamWriterAdaptor.open(new ExecutionContext());

		// then
		assertThat(onOpenWriteCallCount.get()).isEqualTo(1);
	}

	@Test
	void testWrite() throws Exception {
		// given
		AtomicInteger writeCallCount = new AtomicInteger();
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(
			new ItemStreamWriterDelegate<Integer>() {
				@Override
				public void onOpenWrite(@NonNull ExecutionContext executionContext) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void write(@NonNull Chunk<? extends Integer> chunk) {
					writeCallCount.incrementAndGet();
				}

				@Override
				public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void onCloseWrite() {
					throw new UnsupportedOperationException();
				}
			});

		// when
		itemStreamWriterAdaptor.write(Chunk.of());

		// then
		assertThat(writeCallCount.get()).isEqualTo(1);
	}

	@Test
	void testUpdate() {
		// given
		AtomicInteger onUpdateWriteCallCount = new AtomicInteger();
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(
			new ItemStreamWriterDelegate<Integer>() {
				@Override
				public void onOpenWrite(@NonNull ExecutionContext executionContext) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void write(@NonNull Chunk<? extends Integer> chunk) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
					onUpdateWriteCallCount.incrementAndGet();
				}

				@Override
				public void onCloseWrite() {
					throw new UnsupportedOperationException();
				}
			});

		// when
		itemStreamWriterAdaptor.update(new ExecutionContext());

		// then
		assertThat(onUpdateWriteCallCount.get()).isEqualTo(1);
	}

	@Test
	void testClose() {
		// given
		AtomicInteger onCloseCallCount = new AtomicInteger();
		ItemStreamWriter<Integer> itemStreamWriterAdaptor = ItemStreamWriterAdapter.of(
			new ItemStreamWriterDelegate<Integer>() {
				@Override
				public void onOpenWrite(@NonNull ExecutionContext executionContext) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void write(@NonNull Chunk<? extends Integer> chunk) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void onCloseWrite() {
					onCloseCallCount.incrementAndGet();
				}
			});

		// when
		itemStreamWriterAdaptor.close();

		// then
		assertThat(onCloseCallCount.get()).isEqualTo(1);
	}

	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ItemStreamWriterAdapter.of(null));
	}
}
