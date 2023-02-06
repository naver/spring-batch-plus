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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

/**
 * A {@link StepScope} bound proxy implementation.
 *
 * @since 0.1.0
 */
public class StepScopeItemStreamReader<T> implements ItemStreamReader<T> {

	/**
	 * Create an {@link ItemStreamReader} instance bound to {@link StepScope}.
	 * It creates new instance for every {@link StepScope}.
	 *
	 * @param delegateSupplier a concrete instance supplier
	 * @return an adapted ItemStreamReader
	 * @param <T> a read item type
	 */
	public static <T> ItemStreamReader<T> of(Supplier<ItemStreamReader<T>> delegateSupplier) {
		return new StepScopeItemStreamReader<>(delegateSupplier);
	}

	protected static final String SCOPE_KEY = "StepScopeItemStreamReader@delegate";

	protected final Logger logger = getLogger(StepScopeItemStreamReader.class);

	protected final Supplier<ItemStreamReader<T>> delegateSupplier;

	protected StepScopeItemStreamReader(Supplier<ItemStreamReader<T>> readerGenerator) {
		this.delegateSupplier = Objects.requireNonNull(readerGenerator, "Reader generator must not be null");
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		getDelegate().open(executionContext);
	}

	@Override
	public T read() throws Exception {
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
	protected ItemStreamReader<T> getDelegate() {
		StepContext context = Objects.requireNonNull(StepSynchronizationManager.getContext(),
			"No step context is set. Make sure if it's invoked in a stepScope.");

		if (!context.hasAttribute(SCOPE_KEY)) {
			logger.info("No reader in a stepScope. Add a new one (stepExecutionId: {})",
				context.getStepExecution().getId());
			context.setAttribute(SCOPE_KEY, delegateSupplier.get());
		}

		return (ItemStreamReader<T>)context.getAttribute(SCOPE_KEY);
	}
}
