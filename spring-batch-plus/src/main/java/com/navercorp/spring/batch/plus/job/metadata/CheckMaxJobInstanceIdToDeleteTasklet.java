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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * A tasklet to check metadata of job instances to delete.
 */
class CheckMaxJobInstanceIdToDeleteTasklet implements Tasklet, StepExecutionListener {

	static final String MAX_ID_KEY = "maxJobInstanceId";

	static final ExitStatus EMPTY = new ExitStatus("EMPTY");

	private final Logger logger = LoggerFactory.getLogger(CheckMaxJobInstanceIdToDeleteTasklet.class);

	private final JobMetadataDao dao;

	private final String baseDateParameterName;

	private final DateTimeFormatter baseDateFormatter;

	CheckMaxJobInstanceIdToDeleteTasklet(
		JobMetadataDao dao,
		String baseDateParameterName,
		DateTimeFormatter baseDateFormatter
	) {
		this.dao = dao;
		this.baseDateParameterName = baseDateParameterName;
		this.baseDateFormatter = baseDateFormatter;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		StepContext stepContext = chunkContext.getStepContext();
		Map<String, Object> jobParameters = stepContext.getJobParameters();
		String baseDateStr = (String)jobParameters.get(baseDateParameterName);
		LocalDate baseDate = LocalDate.parse(baseDateStr, baseDateFormatter);
		Optional<Long> maxJobInstanceId = this.dao.selectMaxJobInstanceIdLessThanCreateTime(baseDate);

		if (!maxJobInstanceId.isPresent()) {
			logger.info("There is no record of job instance executed before {}. No need to delete metadata", baseDate);
			return RepeatStatus.FINISHED;
		}

		JobExecution jobExecution = stepContext.getStepExecution().getJobExecution();
		ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
		logger.info("Putting [" + MAX_ID_KEY + "={}] to job execution context", maxJobInstanceId.get());
		jobExecutionContext.putLong(MAX_ID_KEY, maxJobInstanceId.get());

		return RepeatStatus.FINISHED;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (stepExecution.getStatus() == BatchStatus.FAILED) {
			return ExitStatus.FAILED;
		}

		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		long maxJobInstanceId = jobExecutionContext.getLong(MAX_ID_KEY, 0L);
		if (maxJobInstanceId == 0L) {
			return EMPTY;
		}
		return ExitStatus.COMPLETED;
	}
}
