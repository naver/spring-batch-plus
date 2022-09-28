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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

class MetadataTestSupports {
	static Date getDate(int year, int month, int day) {
		ZonedDateTime dateTime = LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault());
		return Date.from(dateTime.toInstant());
	}

	static JobParameters buildJobParams() {
		return new JobParametersBuilder()
			.addLong("timestamp", Instant.now().toEpochMilli())
			.toJobParameters();
	}
}
