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

package com.navercorp.spring.batch.plus.sample.deletemedadata.customdryrun

import com.navercorp.spring.batch.plus.job.metadata.DeleteMetadataJobBuilder
import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.Job
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun removeJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository,
    ): Job {
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .dryRunParameterName("customDryRunParam")
            .build()
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(
                    { _, _ -> RepeatStatus.FINISHED },
                    transactionManager,
                )
            }
        }
    }
}
