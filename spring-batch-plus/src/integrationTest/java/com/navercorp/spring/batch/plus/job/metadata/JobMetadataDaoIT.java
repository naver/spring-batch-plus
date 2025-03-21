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

package com.navercorp.spring.batch.plus.job.metadata;

import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.buildJobParams;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateFrom;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestJobRepositoryConfig.class)
class JobMetadataDaoIT {

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobMetadataDao dao;

	@AfterEach
	void tearDown(@Autowired JobRepositoryTestUtils testUtils) {
		testUtils.removeJobExecutions();
	}

	@Test
	void selectMaxJobInstanceIdLessThanCreateTimeWhenEmpty() throws Exception {
		JobExecution execution = jobRepository.createJobExecution("testJob1", buildJobParams());
		execution.setCreateTime(dateFrom(2022, 3, 14));
		jobRepository.update(execution);

		Optional<Long> maxJobInstanceId = dao.selectMaxJobInstanceIdLessThanCreateTime(LocalDate.of(2022, 3, 14));

		assertThat(maxJobInstanceId).isNotPresent();
	}

	@Test
	void selectMaxJobInstanceIdLessThanCreateTimeWhenExists() throws Exception {
		JobExecution execution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		execution1.setCreateTime(dateTo(2022, 3, 14));
		jobRepository.update(execution1);
		JobExecution execution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		execution2.setCreateTime(dateFrom(2022, 3, 15));
		jobRepository.update(execution2);

		Optional<Long> maxJobInstanceId = dao.selectMaxJobInstanceIdLessThanCreateTime(LocalDate.of(2022, 3, 15));

		assertThat(maxJobInstanceId).isPresent();
		assertThat(maxJobInstanceId.get()).isEqualTo(execution1.getJobId());
	}

	@Test
	void testSelectMinJobInstanceId() {
		JobInstance jobInstance1 = jobRepository.createJobInstance("testJob1", buildJobParams());
		jobRepository.createJobInstance("testJob2", buildJobParams());
		Long minJobInstanceId = dao.selectMinJobInstanceId();

		assertThat(minJobInstanceId).isEqualTo(jobInstance1.getId());
	}

	@Test
	void testDeleteJobInstancesByJobInstanceIdRange() {
		JobInstance instance1 = jobRepository.createJobInstance("testJob1", buildJobParams());
		JobInstance instance2 = jobRepository.createJobInstance("testJob2", buildJobParams());
		int deletedCount = dao.deleteJobInstancesByJobInstanceIdRange(instance1.getId(), instance2.getId());

		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteJobExecutionsParamsByJobInstanceIdRange() throws Exception {
		JobExecution execution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		JobExecution execution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		long lowJobInstanceId = execution1.getJobId();
		long highJobInstanceId = execution2.getJobId();
		int deletedCount = dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteJobExecutionContextsByJobInstanceIdRange() throws Exception {
		JobExecution execution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		JobExecution execution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		long lowJobInstanceId = execution1.getJobId();
		long highJobInstanceId = execution2.getJobId();
		int deletedCount = dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteJobExecutionsByJobInstanceIdRange() throws Exception {
		JobExecution execution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		JobExecution execution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		jobRepository.createJobExecution("testJob3", buildJobParams());
		long lowJobInstanceId = execution1.getJobId();
		long highJobInstanceId = execution2.getJobId();
		dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		int deletedCount = dao.deleteJobExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteStepExecutionContextsByJobInstanceIdRange() throws Exception {
		JobExecution jobExecution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		jobExecution1.createStepExecution("testStep1");
		jobExecution1.createStepExecution("testStep2");
		jobRepository.add(new StepExecution("testStep1", jobExecution1));
		jobRepository.add(new StepExecution("testStep2", jobExecution1));
		JobExecution jobExecution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		jobRepository.add(new StepExecution("testStep3", jobExecution2));

		long lowJobInstanceId = jobExecution1.getJobId();
		long highJobInstanceId = jobExecution2.getJobId();
		int deletedCount = dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(deletedCount).isEqualTo(3);
	}

	@Test
	void testDeleteStepExecutionsByJobInstanceIdRange() throws Exception {
		JobExecution jobExecution1 = jobRepository.createJobExecution("testJob1", buildJobParams());
		jobExecution1.createStepExecution("testStep1");
		jobExecution1.createStepExecution("testStep2");
		jobRepository.add(new StepExecution("testStep1", jobExecution1));
		jobRepository.add(new StepExecution("testStep2", jobExecution1));
		JobExecution jobExecution2 = jobRepository.createJobExecution("testJob2", buildJobParams());
		jobRepository.add(new StepExecution("testStep3", jobExecution2));
		JobExecution jobExecution3 = jobRepository.createJobExecution("testJob3", buildJobParams());
		jobRepository.add(new StepExecution("testStep4", jobExecution3));

		long lowJobInstanceId = jobExecution1.getJobId();
		long highJobInstanceId = jobExecution2.getJobId();
		dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		int deletedCount = dao.deleteStepExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		assertThat(deletedCount).isEqualTo(3);
	}
}
