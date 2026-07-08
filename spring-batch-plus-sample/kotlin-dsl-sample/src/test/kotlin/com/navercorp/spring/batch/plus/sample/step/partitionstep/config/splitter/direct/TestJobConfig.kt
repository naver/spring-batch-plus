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

package com.navercorp.spring.batch.plus.sample.step.partitionstep.config.splitter.direct

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.partition.StepExecutionSplitter
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    partitioner {
                        partitionHandler {
                            taskExecutor(SimpleAsyncTaskExecutor())
                            step(actualStep())
                            gridSize(4)
                        }
                        splitter(
                            object : StepExecutionSplitter {
                                override fun getStepName(): String = "workerStep"

                                override fun split(
                                    stepExecution: StepExecution,
                                    gridSize: Int,
                                ): Set<StepExecution> {
                                    val jobExecution = stepExecution.jobExecution
                                    return (0 until gridSize)
                                        .map {
                                            jobRepository.createStepExecution(
                                                "$stepName:partition-$it",
                                                jobExecution,
                                            )
                                        }.toSet()
                                }
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun actualStep(): Step =
        batch {
            step("actualStep") {
                tasklet(
                    { contribution, _ ->
                        println("[${Thread.currentThread().name}][${contribution.stepExecution.stepName}] run actual tasklet")
                        RepeatStatus.FINISHED
                    },
                    transactionManager,
                )
            }
        }
}
