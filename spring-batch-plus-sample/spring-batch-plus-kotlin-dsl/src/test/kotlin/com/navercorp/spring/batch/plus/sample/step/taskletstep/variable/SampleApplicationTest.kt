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

package com.navercorp.spring.batch.plus.sample.step.taskletstep.variable

import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class SampleApplicationTest {
    @Test
    fun run() {
        val applicationContext = runApplication<SampleApplicationTest>()
        val jobLauncher = applicationContext.getBean<JobLauncher>()
        val job = applicationContext.getBean<Job>()

        val jobParameters = JobParametersBuilder()
            .toJobParameters()
        val jobExecution = jobLauncher.run(job, jobParameters)

        assert(BatchStatus.COMPLETED == jobExecution.status)
        println(jobExecution)
    }
}
