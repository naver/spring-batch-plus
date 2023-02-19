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

package com.navercorp.spring.batch.plus.sample.clearwithid

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class BatchApplication

fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    val jobExplorer = applicationContext.getBean<JobExplorer>()
    val job = applicationContext.getBean<Job>()

    val firstJobParameter = JobParametersBuilder(jobExplorer)
        .getNextJobParameters(job)
        .toJobParameters()
    val firstJobExecution = jobLauncher.run(job, firstJobParameter)

    val secondJobParameter = JobParametersBuilder(jobExplorer)
        .getNextJobParameters(job)
        .toJobParameters()
    val secondJobExecution = jobLauncher.run(job, secondJobParameter)

    // first: COMPLETED, jobParameters: {testId=1}
    println("first: ${firstJobExecution.exitStatus.exitCode}, jobParameters: ${firstJobExecution.jobParameters}")

    // second: COMPLETED, jobParameters: {testId=2}
    println("first: ${secondJobExecution.exitStatus.exitCode}, jobParameters: ${secondJobExecution.jobParameters}")
}
