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

package com.navercorp.spring.batch.plus.sample.deletemedadata.prefixfromvariable

import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import javax.sql.DataSource

@Configuration
open class JdbcConfig {

    /*
        localhost:8080/h2-console
            jdbc url : jdbc:h2:mem:jobdb
            user : "sa"
            pw   : ""
     */
    @BatchDataSource
    @Bean
    open fun dataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setName("jobdb")
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:sql/schema-h2-custom.sql")
            .ignoreFailedDrops(true)
            .build()
    }
}
