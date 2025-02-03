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

package com.navercorp.spring.boot.autoconfigure.batch.plus.kotlin

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.TransactionManager
import javax.sql.DataSource

private class BatchPlusAutoConfigurationIT {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(BatchPlusAutoConfiguration::class.java))

    @Test
    fun autoConfigure() {
        @EnableBatchProcessing(
            dataSourceRef = "metadataDataSource",
            transactionManagerRef = "metadataTransactionManager",
        )
        class BatchConfiguration {

            @Bean
            fun metadataTransactionManager(): TransactionManager {
                return DataSourceTransactionManager(metadataDataSource())
            }

            @Bean
            fun metadataDataSource(): DataSource {
                return EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("/org/springframework/batch/core/schema-h2.sql")
                    .generateUniqueName(true)
                    .build()
            }
        }

        contextRunner.withUserConfiguration(BatchConfiguration::class.java)
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasSingleBean(BatchDsl::class.java)
            }
    }

    @Test
    fun autoConfigureShouldNotRegisterWhenAlreadyRegisteredOneExists() {
        @EnableBatchProcessing(
            dataSourceRef = "metadataDataSource",
            transactionManagerRef = "metadataTransactionManager",
        )
        class BatchConfiguration {
            @Bean
            fun batchDsl(
                beanFactory: BeanFactory,
                jobRepository: JobRepository,
            ): BatchDsl {
                return BatchDsl(
                    beanFactory,
                    jobRepository,
                )
            }

            @Bean
            fun metadataTransactionManager(): TransactionManager {
                return DataSourceTransactionManager(metadataDataSource())
            }

            @Bean
            fun metadataDataSource(): DataSource {
                return EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("/org/springframework/batch/core/schema-h2.sql")
                    .generateUniqueName(true)
                    .build()
            }
        }

        contextRunner.withUserConfiguration(BatchConfiguration::class.java)
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasSingleBean(BatchDsl::class.java)
            }
    }

    @Test
    fun autoConfigureShouldNotRegisterWhenNoRequiredBeans() {
        contextRunner.run { context: AssertableApplicationContext ->
            assertThatThrownBy { context.getBean<BatchDsl>() }
                .hasMessageContaining("No qualifying bean")
        }
    }
}
