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
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

class MetadataTestSupports {

	static LocalDateTime dateTo(int year, int month, int day) {
		LocalDate to = LocalDate.of(year, month, day);
		LocalDate from = to.minusDays(10);
		return dateBetween(from, to);
	}

	static LocalDateTime dateFrom(int year, int month, int day) {
		LocalDate from = LocalDate.of(year, month, day);
		LocalDate to = from.plusDays(10);
		return dateBetween(from, to);
	}

	static LocalDateTime dateBetween(LocalDate from, LocalDate to) {
		assert from.isBefore(to);
		long gap = to.toEpochDay() - from.toEpochDay();
		LocalDate randomDay = from.plusDays(randomBetween(0L, gap));
		return randomDay.atStartOfDay();
	}

	static JobParameters buildJobParams() {
		return new JobParametersBuilder()
			.addLong("timestamp", Instant.now().toEpochMilli())
			.toJobParameters();
	}

	static int randomBetween(int lowerInclusive, int higherInclusive) {
		return ThreadLocalRandom.current().nextInt(lowerInclusive, higherInclusive + 1);
	}

	static long randomBetween(long lowerInclusive, long higherInclusive) {
		return ThreadLocalRandom.current().nextLong(lowerInclusive, higherInclusive + 1L);
	}
}
