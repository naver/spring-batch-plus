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

package com.navercorp.spring.batch.plus.item;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ItemProcessorAdaptorTest {

	@Test
	void testProcess() {
		// when
		ItemProcessorAdaptor<Integer, String> itemProcessorAdaptor = ItemProcessorAdaptor.withDelegate(
			Object::toString);
		String actual = itemProcessorAdaptor.process(1234);

		// then
		assertThat(actual).isEqualTo("1234");
	}

}
