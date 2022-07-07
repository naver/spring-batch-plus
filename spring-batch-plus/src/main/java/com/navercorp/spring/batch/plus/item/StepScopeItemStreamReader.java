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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

class StepScopeItemStreamReader<I> implements ItemStreamReader<I> {

	static <I> ItemStreamReader<I> with(Supplier<ItemStreamReader<I>> delegateSupplier) {
		return new StepScopeItemStreamReader<>(delegateSupplier);
	}

	private static final String SCOPE_KEY = "StepScopeItemStreamReader@delegate";

	private final Logger logger = getLogger(StepScopeItemStreamReader.class);

	private final Supplier<ItemStreamReader<I>> delegateSupplier;

	private StepScopeItemStreamReader(Supplier<ItemStreamReader<I>> readerGenerator) {
		this.delegateSupplier = readerGenerator;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		getDelegate().open(executionContext);
	}

	@Override
	public I read() throws Exception {
		return getDelegate().read();
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		getDelegate().update(executionContext);
	}

	@Override
	public void close() throws ItemStreamException {
		getDelegate().close();
	}

	@SuppressWarnings("unchecked")
	private ItemStreamReader<I> getDelegate() {
		StepContext context = Objects.requireNonNull(StepSynchronizationManager.getContext(),
			"No step context is set. Make sure if it's invoked in a stepScope.");

		if (!context.hasAttribute(SCOPE_KEY)) {
			logger.info("No reader in a stepScope. Add new one (stepExecutionId: {})",
				context.getStepExecution().getId());
			context.setAttribute(SCOPE_KEY, delegateSupplier.get());
		}

		return (ItemStreamReader<I>)context.getAttribute(SCOPE_KEY);
	}
}
