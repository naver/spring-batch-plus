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

package com.navercorp.spring.batch.plus.sample.deletemedadata.prefix

import org.junit.jupiter.api.Test
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

@SpringBootApplication
open class SampleApplicationTest {
    @Test
    fun run() {
        // launch with custom prefix
        val application = SpringApplication(SampleApplicationTest::class.java).apply {
            val properties = Properties().apply {
                this["spring.batch.jdbc.table-prefix"] = "CUSTOM_"
            }
            setDefaultProperties(properties)
        }
        val applicationContext = application.run()
        val jobLauncher = applicationContext.getBean<JobLauncher>()
        val jobRepository = applicationContext.getBean<JobRepository>()

        // prepare job instances
        val testJob = applicationContext.getBean<Job>("testJob")
        val testJobParameterList = (0L..250L).map {
            JobParametersBuilder()
                .addLong("longValue", it)
                .toJobParameters()
        }
        for (testJobParameters in testJobParameterList) {
            // change create time date for test
            val jobExecution = jobLauncher.run(testJob, testJobParameters)
            jobExecution.createTime = jobExecution.createTime.minusDays(1)
            jobRepository.update(jobExecution)
        }

        // launch removeJob
        val removeJob = applicationContext.getBean<Job>("removeJob")
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val jobParameters = JobParametersBuilder()
            .addString("baseDate", now.format(formatter))
            .toJobParameters()
        jobLauncher.run(removeJob, jobParameters)

        // all instances are removed
        for (testJobParameters in testJobParameterList) {
            val jobInstance = jobRepository.getJobInstance("testJob", testJobParameters)
            assert(null == jobInstance)
        }
    }
}
