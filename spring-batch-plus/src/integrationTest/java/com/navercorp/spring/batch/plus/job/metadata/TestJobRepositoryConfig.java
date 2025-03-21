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

import static org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

public class TestJobRepositoryConfig {

	private final Logger logger = LoggerFactory.getLogger(TestJobRepositoryConfig.class);

	@Bean
	DataSource dataSource(String tablePrefix) throws Exception {
		String originalDdl = readFromClassPath("org/springframework/batch/core/schema-h2.sql");
		String customDdl = StringUtils.replace(originalDdl, DEFAULT_TABLE_PREFIX, tablePrefix);
		File customDdlFile = File.createTempFile("schema-", ".sql");
		logger.info("Writing custom DDL script to [{}]", customDdlFile);
		FileCopyUtils.copy(customDdl.getBytes(), customDdlFile);

		return new EmbeddedDatabaseBuilder()
			.generateUniqueName(true)
			.setType(EmbeddedDatabaseType.H2)
			.addScript("file:" + customDdlFile.getAbsolutePath())
			.build();
	}

	@Bean
	String tablePrefix() {
		int randomNumber = new Random().nextInt(1000);
		return "BATCH" + randomNumber + "_";
	}

	@Bean
	JobRepository jobRepository(DataSource dataSource, String tablePrefix) throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(new DataSourceTransactionManager(dataSource));
		factory.setTablePrefix(tablePrefix);
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	JobMetadataDao jobMetadataDao(DataSource dataSource, String tablePrefix) {
		return new JobMetadataDao(dataSource, tablePrefix);
	}

	@Bean
	JobMetadataCountDao jobMetadataCountDao(DataSource dataSource, String tablePrefix) {
		return new JobMetadataCountDao(dataSource, tablePrefix);
	}

	@Bean
	JobRepositoryTestUtils jobRepositoryTestUtils(JobRepository jobRepository) {
		return new JobRepositoryTestUtils(jobRepository);
	}

	private String readFromClassPath(String path) throws IOException {
		Resource originalScript = new ClassPathResource(path);
		try (InputStream scriptStream = originalScript.getInputStream()) {
			return StreamUtils.copyToString(scriptStream, StandardCharsets.UTF_8);
		}
	}
}
