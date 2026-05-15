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
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.createJobExecution;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.createStepExecution;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateFrom;
import static com.navercorp.spring.batch.plus.job.metadata.MetadataTestSupports.dateTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestJobRepositoryConfig.class)
class JobMetadataDaoTest {

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobMetadataDao dao;

	@BeforeEach
	void setUp(@Autowired JobRepositoryTestUtils testUtils) {
		testUtils.removeJobExecutions();
	}

	@Test
	void selectMaxJobInstanceIdLessThanCreateTimeWhenEmpty() throws Exception {
		// given
		JobExecution execution = createJobExecution(jobRepository, "testJob1", buildJobParams());
		execution.setCreateTime(dateFrom(2022, 3, 14));
		jobRepository.update(execution);

		// when
		Optional<Long> maxJobInstanceId = dao.selectMaxJobInstanceIdLessThanCreateTime(LocalDate.of(2022, 3, 14));

		// then
		assertThat(maxJobInstanceId).isNotPresent();
	}

	@Test
	void selectMaxJobInstanceIdLessThanCreateTimeWhenExists() throws Exception {
		// given
		JobExecution execution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		execution1.setCreateTime(dateTo(2022, 3, 14));
		jobRepository.update(execution1);

		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		execution2.setCreateTime(dateFrom(2022, 3, 15));
		jobRepository.update(execution2);

		// when
		Optional<Long> maxJobInstanceId = dao.selectMaxJobInstanceIdLessThanCreateTime(LocalDate.of(2022, 3, 15));

		// then
		assertThat(maxJobInstanceId).isPresent();
		assertThat(maxJobInstanceId.get()).isEqualTo(execution1.getJobInstanceId());
	}

	@Test
	void testSelectMinJobInstanceId() {
		// given
		JobInstance jobInstance1 = jobRepository.createJobInstance("testJob1", buildJobParams());
		jobRepository.createJobInstance("testJob2", buildJobParams());

		// when
		Long minJobInstanceId = dao.selectMinJobInstanceId();

		// then
		assertThat(minJobInstanceId).isEqualTo(jobInstance1.getId());
	}

	@Test
	void testDeleteJobInstancesByJobInstanceIdRange() {
		// given
		JobInstance instance1 = jobRepository.createJobInstance("testJob1", buildJobParams());
		JobInstance instance2 = jobRepository.createJobInstance("testJob2", buildJobParams());

		// when
		int deletedCount = dao.deleteJobInstancesByJobInstanceIdRange(instance1.getId(), instance2.getId());

		// then
		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteJobExecutionsParamsByJobInstanceIdRange() throws Exception {
		// given
		JobExecution execution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		long lowJobInstanceId = execution1.getJobInstanceId();
		long highJobInstanceId = execution2.getJobInstanceId();

		// when
		int deletedCount = dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// then
		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteJobExecutionContextsByJobInstanceIdRange() throws Exception {
		// given
		JobExecution execution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		long lowJobInstanceId = execution1.getJobInstanceId();
		long highJobInstanceId = execution2.getJobInstanceId();

		// when
		int deletedCount = dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// then
		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteJobExecutionsByJobInstanceIdRange() throws Exception {
		// given
		JobExecution execution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		JobExecution execution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		createJobExecution(jobRepository, "testJob3", buildJobParams());

		long lowJobInstanceId = execution1.getJobInstanceId();
		long highJobInstanceId = execution2.getJobInstanceId();
		dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// when
		int deletedCount = dao.deleteJobExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// then
		assertThat(deletedCount).isEqualTo(2);
	}

	@Test
	void testDeleteStepExecutionContextsByJobInstanceIdRange() throws Exception {
		// given
		JobExecution jobExecution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		createStepExecution(jobRepository, "testStep1", jobExecution1);
		createStepExecution(jobRepository, "testStep2", jobExecution1);

		JobExecution jobExecution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		createStepExecution(jobRepository, "testStep3", jobExecution2);

		long lowJobInstanceId = jobExecution1.getJobInstanceId();
		long highJobInstanceId = jobExecution2.getJobInstanceId();

		// when
		int deletedCount = dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// then
		assertThat(deletedCount).isEqualTo(3);
	}

	@Test
	void testDeleteStepExecutionsByJobInstanceIdRange() throws Exception {
		// given
		JobExecution jobExecution1 = createJobExecution(jobRepository, "testJob1", buildJobParams());
		createStepExecution(jobRepository, "testStep1", jobExecution1);
		createStepExecution(jobRepository, "testStep2", jobExecution1);

		JobExecution jobExecution2 = createJobExecution(jobRepository, "testJob2", buildJobParams());
		createStepExecution(jobRepository, "testStep3", jobExecution2);

		JobExecution jobExecution3 = createJobExecution(jobRepository, "testJob3", buildJobParams());
		createStepExecution(jobRepository, "testStep4", jobExecution3);

		long lowJobInstanceId = jobExecution1.getJobInstanceId();
		long highJobInstanceId = jobExecution2.getJobInstanceId();
		dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// when
		int deletedCount = dao.deleteStepExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);

		// then
		assertThat(deletedCount).isEqualTo(3);
	}
}
