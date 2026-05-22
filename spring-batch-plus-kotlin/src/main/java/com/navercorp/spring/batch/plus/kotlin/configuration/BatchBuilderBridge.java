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

package com.navercorp.spring.batch.plus.kotlin.configuration;

import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;

// Java bridge for Spring Batch builder constructors whose F-bounded wildcard
// signatures cannot be matched by the Kotlin K2 compiler. See KT-66570.
final class BatchBuilderBridge {

	static SimpleJobBuilder toSimpleJobBuilder(JobBuilder jobBuilder) {
		return new SimpleJobBuilder(jobBuilder);
	}

	static FlowJobBuilder toFlowJobBuilder(JobBuilder jobBuilder) {
		return new FlowJobBuilder(jobBuilder);
	}

	private BatchBuilderBridge() {
	}
}
