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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * A tasklet to delete metadata of Spring Batch.
 * This tasklet must be executed after execution of {@link CheckMaxJobInstanceIdToDeleteTasklet}
 */
class DeleteMetadataTasklet extends StepExecutionListenerSupport implements Tasklet {

	static final String LOW_ID_KEY = "lowJobInstanceId";

	static final int DELETION_RANGE_LENGTH = 100;

	private final Logger logger = LoggerFactory.getLogger(DeleteMetadataTasklet.class);

	private final JobMetadataDao dao;

	private long maxJobInstanceId;

	DeleteMetadataTasklet(JobMetadataDao dao) {
		this.dao = dao;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		this.maxJobInstanceId = jobExecutionContext.getLong(CheckMaxJobInstanceIdToDeleteTasklet.MAX_ID_KEY);
		long minJobInstanceId = this.dao.selectMinJobInstanceId();

		ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
		if (stepExecutionContext.containsKey(LOW_ID_KEY)) { // in case of restart
			return;
		}

		putLowJobInstanceId(stepExecution, minJobInstanceId);
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		StepExecution stepExecution = contribution.getStepExecution();

		long lowJobInstanceId = getLowJobInstanceId(stepExecution);
		long highJobInstanceId = Math.min(lowJobInstanceId + DELETION_RANGE_LENGTH - 1, maxJobInstanceId);
		logger.info("Deleting job instances by ID from [{}] to [{}]", lowJobInstanceId, highJobInstanceId);

		int deletedJobInstances = deleteJobMetadata(lowJobInstanceId, highJobInstanceId);
		contribution.incrementWriteCount(deletedJobInstances);

		long nextLowJobInstanceId = highJobInstanceId + 1;
		if (nextLowJobInstanceId > this.maxJobInstanceId) {
			return RepeatStatus.FINISHED;
		}
		putLowJobInstanceId(stepExecution, nextLowJobInstanceId);

		return RepeatStatus.CONTINUABLE;
	}

	private void putLowJobInstanceId(StepExecution stepExecution, long lowJobInstanceId) {
		ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
		stepExecutionContext.put(LOW_ID_KEY, lowJobInstanceId);
	}

	private long getLowJobInstanceId(StepExecution stepExecution) {
		ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
		return stepExecutionContext.getLong(LOW_ID_KEY);
	}

	private int deleteJobMetadata(long lowJobInstanceId, long highJobInstanceId) {
		dao.deleteStepExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteStepExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteJobExecutionContextsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteJobExecutionParamsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		dao.deleteJobExecutionsByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
		return dao.deleteJobInstancesByJobInstanceIdRange(lowJobInstanceId, highJobInstanceId);
	}
}
