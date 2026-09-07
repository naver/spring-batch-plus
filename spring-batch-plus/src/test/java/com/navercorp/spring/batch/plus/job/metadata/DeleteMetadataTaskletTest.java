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

import static com.navercorp.spring.batch.plus.job.metadata.DeleteMetadataTasklet.DELETION_RANGE_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
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
 * Covers deletion bound initialization, progression, and metadata removal ordering.
 *
 * <p>Classicist: the execution contexts are real.
 */
class DeleteMetadataTaskletTest {

	@Test
	void beforeStepShouldInitializeDeletionBoundsFromMinimumJobInstanceId() {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long minJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		long maxJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getJobExecution().getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		JobMetadataDao dao = mock(JobMetadataDao.class);
		when(dao.selectMinJobInstanceId()).thenReturn(Optional.of(minJobInstanceId));
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		sut.beforeStep(stepExecution);

		// then
		verify(dao).selectMinJobInstanceId();
		ExecutionContext actual = stepExecution.getExecutionContext();
		assertThat(actual.getLong("lowJobInstanceId")).isEqualTo(minJobInstanceId);
		assertThat(actual.getLong("maxJobInstanceId")).isEqualTo(maxJobInstanceId);
	}

	@Test
	void beforeStepShouldPreserveLowerBoundWhenRestarting() {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long lowJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		long maxJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getJobExecution().getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		stepExecution.getExecutionContext().putLong("lowJobInstanceId", lowJobInstanceId);
		JobMetadataDao dao = mock(JobMetadataDao.class);
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		sut.beforeStep(stepExecution);

		// then
		verifyNoInteractions(dao);
		ExecutionContext actual = stepExecution.getExecutionContext();
		assertThat(actual.getLong("lowJobInstanceId")).isEqualTo(lowJobInstanceId);
	}

	@Test
	void beforeStepShouldLeaveLowerBoundAbsentWhenMetadataDoesNotExist() {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long maxJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getJobExecution().getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		JobMetadataDao dao = mock(JobMetadataDao.class);
		when(dao.selectMinJobInstanceId()).thenReturn(Optional.empty());
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		sut.beforeStep(stepExecution);

		// then
		verify(dao).selectMinJobInstanceId();
		ExecutionContext actual = stepExecution.getExecutionContext();
		assertThat(actual.containsKey("lowJobInstanceId")).isFalse();
	}

	@Test
	void executeShouldDeleteWholeRangeAndContinueWhenMaximumIsBeyondCurrentRange() throws Exception {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long lowJobInstanceId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
		long highJobInstanceId = lowJobInstanceId + DELETION_RANGE_LENGTH - 1;
		long maxJobInstanceId = highJobInstanceId + ThreadLocalRandom.current().nextLong(1, 1_000_000);
		int deletedJobInstanceCount = ThreadLocalRandom.current().nextInt(1, DELETION_RANGE_LENGTH + 1);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getExecutionContext().putLong("lowJobInstanceId", lowJobInstanceId);
		stepExecution.getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		StepContribution contribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		JobMetadataDao dao = mock(JobMetadataDao.class);
		when(dao.deleteJobInstancesByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId))
			.thenReturn(deletedJobInstanceCount);
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		RepeatStatus actual = sut.execute(contribution, chunkContext);

		// then
		InOrder deletionOrder = inOrder(dao);
		deletionOrder.verify(dao)
			.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		deletionOrder.verify(dao).deleteStepExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		deletionOrder.verify(dao)
			.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		deletionOrder.verify(dao)
			.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		deletionOrder.verify(dao).deleteJobExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		deletionOrder.verify(dao).deleteJobInstancesByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		ExecutionContext actualContext = stepExecution.getExecutionContext();
		assertThat(actual).isEqualTo(RepeatStatus.CONTINUABLE);
		assertThat(actualContext.getLong("lowJobInstanceId")).isEqualTo(highJobInstanceId + 1);
		assertThat(contribution.getWriteCount()).isEqualTo(deletedJobInstanceCount);
	}

	@Test
	void executeShouldDeleteUpToMaximumAndFinishWhenMaximumIsWithinCurrentRange() throws Exception {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long lowJobInstanceId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
		long maxJobInstanceId = lowJobInstanceId
			+ ThreadLocalRandom.current().nextLong(DELETION_RANGE_LENGTH);
		int deletedJobInstanceCount = ThreadLocalRandom.current().nextInt(1, DELETION_RANGE_LENGTH + 1);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getExecutionContext().putLong("lowJobInstanceId", lowJobInstanceId);
		stepExecution.getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		StepContribution contribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		JobMetadataDao dao = mock(JobMetadataDao.class);
		when(dao.deleteJobInstancesByJobInstanceIdRange(lowJobInstanceId, maxJobInstanceId))
			.thenReturn(deletedJobInstanceCount);
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		RepeatStatus actual = sut.execute(contribution, chunkContext);

		// then
		verify(dao).deleteJobInstancesByJobInstanceIdRange(lowJobInstanceId, maxJobInstanceId);
		assertThat(actual).isEqualTo(RepeatStatus.FINISHED);
		assertThat(stepExecution.getExecutionContext().getLong("lowJobInstanceId")).isEqualTo(lowJobInstanceId);
		assertThat(contribution.getWriteCount()).isEqualTo(deletedJobInstanceCount);
	}

	@Test
	void executeShouldAdvanceWithoutDeletingWhenDryRunIsEnabled() throws Exception {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long lowJobInstanceId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
		long highJobInstanceId = lowJobInstanceId + DELETION_RANGE_LENGTH - 1;
		long maxJobInstanceId = highJobInstanceId + ThreadLocalRandom.current().nextLong(1, 1_000_000);
		JobParameters jobParameters = new JobParametersBuilder()
			.addString(dryRunParameterName, "true")
			.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		stepExecution.getExecutionContext().putLong("lowJobInstanceId", lowJobInstanceId);
		stepExecution.getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		StepContribution contribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		JobMetadataDao dao = mock(JobMetadataDao.class);
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		RepeatStatus actual = sut.execute(contribution, chunkContext);

		// then
		verifyNoInteractions(dao);
		ExecutionContext actualContext = stepExecution.getExecutionContext();
		assertThat(actual).isEqualTo(RepeatStatus.CONTINUABLE);
		assertThat(actualContext.getLong("lowJobInstanceId")).isEqualTo(highJobInstanceId + 1);
		assertThat(contribution.getWriteCount()).isZero();
	}

	@Test
	void executeShouldFinishWithoutDeletingWhenLowerBoundIsAbsent() throws Exception {
		// given
		String dryRunParameterName = UUID.randomUUID().toString();
		long maxJobInstanceId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getExecutionContext().putLong("maxJobInstanceId", maxJobInstanceId);
		StepContribution contribution = new StepContribution(stepExecution);
		ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
		JobMetadataDao dao = mock(JobMetadataDao.class);
		DeleteMetadataTasklet sut = new DeleteMetadataTasklet(dao, dryRunParameterName);

		// when
		RepeatStatus actual = sut.execute(contribution, chunkContext);

		// then
		verifyNoInteractions(dao);
		assertThat(actual).isEqualTo(RepeatStatus.FINISHED);
		assertThat(contribution.getWriteCount()).isZero();
	}
}
