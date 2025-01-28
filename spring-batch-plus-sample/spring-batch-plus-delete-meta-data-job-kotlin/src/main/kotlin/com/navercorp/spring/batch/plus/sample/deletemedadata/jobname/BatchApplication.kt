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

package com.navercorp.spring.batch.plus.sample.deletemedadata.jobname

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@SpringBootApplication
open class BatchApplication : ApplicationRunner {

    @Autowired
    lateinit var jobLauncher: JobLauncher

    @Autowired
    lateinit var jobRepository: JobRepository

    @Autowired
    lateinit var testJob: Job

    override fun run(args: ApplicationArguments) {
        // run testJob 350 times
        val jobExecutions = (0L until 350L).map {
            val jobParameters = JobParametersBuilder()
                .addLong("longValue", it)
                .toJobParameters()
            jobLauncher.run(testJob, jobParameters)
        }

        // change previous execution date for test
        jobExecutions.forEach { jobExecution ->
            val createTime = jobExecution.createTime
            val startTime = jobExecution.startTime
            val endTime = jobExecution.endTime
            val updateCreateTime = createTime.minus(1, ChronoUnit.DAYS)
            val updateStartTime = startTime!!.minus(1, ChronoUnit.DAYS)
            val updateEndTime = endTime!!.minus(1, ChronoUnit.DAYS)

            val updateJobExecution = JobExecution(jobExecution)
            updateJobExecution.createTime = updateCreateTime
            updateJobExecution.setStartTime(updateStartTime)
            updateJobExecution.setEndTime(updateEndTime)
            jobRepository.update(updateJobExecution)
        }
    }
}

fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameters = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameters)
}
