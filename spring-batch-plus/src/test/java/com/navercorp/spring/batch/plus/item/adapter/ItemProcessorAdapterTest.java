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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;

// note: it's deprecated. Do not change it.
@SuppressWarnings({"unchecked", "deprecation"})
class ItemProcessorAdapterTest {

	@Test
	void testProcess() throws Exception {
		// given
		Integer expected = ThreadLocalRandom.current().nextInt();
		ItemProcessorDelegate<Integer, Integer> delegate = mock(ItemProcessorDelegate.class);
		when(delegate.process(any())).thenReturn(expected);
		ItemProcessor<Integer, Integer> itemProcessorAdaptor = ItemProcessorAdapter.of(delegate);

		// when
		Integer actual = itemProcessorAdaptor.process(ThreadLocalRandom.current().nextInt());

		// then
		assertThat(actual).isEqualTo(expected);
	}

	@SuppressWarnings({"ConstantConditions"})
	@Test
	void testPassingNull() {
		// when, then
		assertThatThrownBy(() -> ItemProcessorAdapter.of(null));
	}
}
