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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Covers how the metadata deletion boundary controls the next job-flow state.
 *
 * <p>Classicist: the execution contexts are real.
 */
class CheckMaxJobInstanceIdToDeleteTaskletTest {

	@Test
	void executeShouldStoreMaximumJobInstanceIdWhenDeletableMetadataExists() {
		// given
		String baseDateParameterName = UUID.randomUUID().toString();
		String baseDateValue = "2022-03-15";
		LocalDate baseDate = LocalDate.parse(baseDateValue);
		DateTimeFormatter baseDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
		long maxJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(baseDateParameterName, baseDateValue)
			.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepContribution contribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		JobMetadataDao dao = mock(JobMetadataDao.class);
		when(dao.selectMaxJobInstanceIdLessThanCreateTime(baseDate)).thenReturn(Optional.of(maxJobInstanceId));
		CheckMaxJobInstanceIdToDeleteTasklet sut = new CheckMaxJobInstanceIdToDeleteTasklet(
			dao,
			baseDateParameterName,
			baseDateFormatter
		);

		// when
		RepeatStatus actual = sut.execute(contribution, chunkContext);

		// then
		verify(dao).selectMaxJobInstanceIdLessThanCreateTime(baseDate);
		ExecutionContext actualContext = stepExecution.getJobExecution().getExecutionContext();
		assertThat(actual).isEqualTo(RepeatStatus.FINISHED);
		assertThat(actualContext.getLong("maxJobInstanceId")).isEqualTo(maxJobInstanceId);
	}

	@Test
	void executeShouldLeaveMaximumIdAbsentWhenDeletableMetadataDoesNotExist() {
		// given
		String baseDateParameterName = UUID.randomUUID().toString();
		String baseDateValue = "2022-03-15";
		LocalDate baseDate = LocalDate.parse(baseDateValue);
		DateTimeFormatter baseDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(baseDateParameterName, baseDateValue)
			.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepContribution contribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		JobMetadataDao dao = mock(JobMetadataDao.class);
		when(dao.selectMaxJobInstanceIdLessThanCreateTime(baseDate)).thenReturn(Optional.empty());
		CheckMaxJobInstanceIdToDeleteTasklet sut = new CheckMaxJobInstanceIdToDeleteTasklet(
			dao,
			baseDateParameterName,
			baseDateFormatter
		);

		// when
		RepeatStatus actual = sut.execute(contribution, chunkContext);

		// then
		verify(dao).selectMaxJobInstanceIdLessThanCreateTime(baseDate);
		ExecutionContext actualContext = stepExecution.getJobExecution().getExecutionContext();
		assertThat(actual).isEqualTo(RepeatStatus.FINISHED);
		assertThat(actualContext.containsKey("maxJobInstanceId")).isFalse();
	}

	@Test
	void afterStepShouldReturnCompletedWhenMaximumJobInstanceIdIsPresent() {
		// given
		String baseDateParameterName = UUID.randomUUID().toString();
		long maxJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getJobExecution().getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		JobMetadataDao dao = mock(JobMetadataDao.class);
		CheckMaxJobInstanceIdToDeleteTasklet sut = new CheckMaxJobInstanceIdToDeleteTasklet(
			dao,
			baseDateParameterName,
			DateTimeFormatter.ISO_LOCAL_DATE
		);

		// when
		ExitStatus actual = sut.afterStep(stepExecution);

		// then
		assertThat(actual).isEqualTo(ExitStatus.COMPLETED);
	}

	@Test
	void afterStepShouldReturnEmptyWhenMaximumJobInstanceIdIsAbsent() {
		// given
		String baseDateParameterName = UUID.randomUUID().toString();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		JobMetadataDao dao = mock(JobMetadataDao.class);
		CheckMaxJobInstanceIdToDeleteTasklet sut = new CheckMaxJobInstanceIdToDeleteTasklet(
			dao,
			baseDateParameterName,
			DateTimeFormatter.ISO_LOCAL_DATE
		);

		// when
		ExitStatus actual = sut.afterStep(stepExecution);

		// then
		assertThat(actual).isEqualTo(new ExitStatus("EMPTY"));
	}

	@Test
	void afterStepShouldReturnFailedWhenStepExecutionFailed() {
		// given
		String baseDateParameterName = UUID.randomUUID().toString();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.setStatus(BatchStatus.FAILED);
		JobMetadataDao dao = mock(JobMetadataDao.class);
		CheckMaxJobInstanceIdToDeleteTasklet sut = new CheckMaxJobInstanceIdToDeleteTasklet(
			dao,
			baseDateParameterName,
			DateTimeFormatter.ISO_LOCAL_DATE
		);

		// when
		ExitStatus actual = sut.afterStep(stepExecution);

		// then
		assertThat(actual).isEqualTo(ExitStatus.FAILED);
	}
}
