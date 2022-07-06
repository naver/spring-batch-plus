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

package com.navercorp.spring.boot.autoconfigure.batch.plus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl;

class BatchPlusAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(BatchPlusAutoConfiguration.class));

	@Test
	void testRegister() {
		contextRunner.withUserConfiguration(BatchConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(BatchDsl.class);
		});
	}

	@Test
	void testNotRegisterWhenAlreadyRegistered() {
		contextRunner.withUserConfiguration(BatchDslConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(BatchDsl.class);
		});
	}

	@Test
	void testNotRegisterOnNoRequiredBeans() {
		contextRunner.run(context -> {
			assertThatThrownBy(
				() -> context.getBean(BatchDsl.class)
			).hasMessageContaining("No qualifying bean");
		});
	}

	@EnableBatchProcessing
	static class BatchConfiguration {
	}

	@EnableBatchProcessing
	static class BatchDslConfiguration {

		@Bean
		public BatchDsl batchDsl(
			BeanFactory beanFactory,
			JobBuilderFactory jobBuilderFactory,
			StepBuilderFactory stepBuilderFactory
		) {
			return new BatchDsl(
				beanFactory,
				jobBuilderFactory,
				stepBuilderFactory
			);
		}
	}

}
