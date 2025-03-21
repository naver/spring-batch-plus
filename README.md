# Spring Batch Plus

![maven central version](https://maven-badges.herokuapp.com/maven-central/com.navercorp.spring/spring-batch-plus-kotlin/badge.svg)
[![build](https://github.com/naver/spring-batch-plus/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/naver/spring-batch-plus/actions/workflows/build.yml?query=branch%3Amain)
[![coverage](https://codecov.io/github/naver/spring-batch-plus/branch/main/graph/badge.svg)](https://codecov.io/github/naver/spring-batch-plus)
[![license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/naver/spring-batch-plus/blob/main/LICENSE)

Spring Batch Plus provides extension features to [Spring Batch](https://github.com/spring-projects/spring-batch).

## Features

### Kotlin DSL

```kotlin
@Bean
fun testJob(batch: BatchDsl): Job = batch {
    job("testJob") {
        step("jobStep1") {
            jobBean("subJob1")
        }
        step("jobStep2") {
            jobBean("subJob2")
        }
    }
}

@Bean
fun subJob1(batch: BatchDsl, transactionManager: PlatformTransactionManager): Job = batch {
    job("subJob1") {
        step("testStep1") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}

@Bean
fun subJob2(batch: BatchDsl, transactionManager: PlatformTransactionManager): Job = batch {
    job("subJob2") {
        step("testStep2") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Single class reader-processor-writer

- ItemStreamFluxReaderProcessorWriter
- ItemStreamIterableReaderProcessorWriter
- ItemStreamIterableReaderProcessorWriter
- ItemStreamSimpleReaderProcessorWriter

```kotlin
// single class
@Component
@StepScope
class SampleTasklet : ItemStreamFluxReaderProcessorWriter<Int, String> {
    private var count = 0

    override fun readFlux(executionContext: ExecutionContext): Flux<out Int> {
        return Flux.generate { sink ->
            if (count < 20) {
                sink.next(count)
                ++count
            } else {
                sink.complete()
            }
        }
    }

    override fun process(item: Int): String? {
        return "'$item'"
    }

    override fun write(chunk: Chunk<String>) {
        println(chunk.items)
    }
}

// usage
@Bean
fun testJob(
    sampleTasklet: SampleTasklet,
    batch: BatchDsl,
): Job = batch {
    job("testJob") {
        step("testStep") {
            chunk<Int, String>(3, ResourcelessTransactionManager()) {
                reader(sampleTasklet.asItemStreamReader())
                processor(sampleTasklet.asItemProcessor())
                writer(sampleTasklet.asItemStreamWriter())
            }
        }
    }
}
```

### Other Useful Classes

- ClearRunIdIncrementer
- DeleteMetadataJob

## Compatibility

We've tested following versions only. Other versions may not work.

| Batch Plus (Latest) | Batch | Boot Starter  | Kotlin        | Java          | Status     | Samples                                                                                    |
|---------------------|-------|---------------|---------------|---------------|------------|--------------------------------------------------------------------------------------------|
| 1.2.x (1.2.0)       | 5.2.x | 3.4.x ~ 3.4.x | 1.6 or higher | 17 or higher  | Maintained | [Samples](https://github.com/naver/spring-batch-plus/tree/main/spring-batch-plus-sample)   |
| 1.1.x (1.1.0)       | 5.1.x | 3.2.x ~ 3.3.x | 1.5 or higher | 17 or higher  | Maintained | [Samples](https://github.com/naver/spring-batch-plus/tree/1.1.x/spring-batch-plus-sample)  |
| 1.0.x (1.0.1)       | 5.0.x | 3.0.x ~ 3.1.x | 1.5 or higher | 17 or higher  | Maintained | [Samples](https://github.com/naver/spring-batch-plus/tree/1.0.x/spring-batch-plus-sample)  |
| 0.3.x (0.3.1)       | 4.3.x | 2.4.x ~ 2.7.x | 1.5 or higher | 1.8 or higher | Maintained | [Samples](https://github.com/naver/spring-batch-plus/tree/0.3.x/spring-batch-plus-sample)  |
| 0.2.x (0.2.0)       | 4.3.x | 2.4.x ~ 2.7.x | 1.5 or higher | 1.8 or higher | Freezed    | [Samples](https://github.com/naver/spring-batch-plus/tree/v0.2.0/spring-batch-plus-sample) |
| 0.1.x (0.1.0)       | 4.3.x | 2.4.x ~ 2.7.x | 1.5 or higher | 1.8 or higher | Freezed    | [Samples](https://github.com/naver/spring-batch-plus/tree/v0.1.0/spring-batch-plus-sample) |

## Download

Since it provides extension features to spring batch, need to used with [spring batch](https://github.com/spring-projects/spring-batch).

### Gradle

Kotlin

```kotlin
implementation("org.springframework.boot:spring-boot-starter-batch:${springBootVersion}") // need spring batch
implementation("com.navercorp.spring:spring-boot-starter-batch-plus-kotlin:${springBatchPlusVersion}")
```

Java

```kotlin
implementation("org.springframework.boot:spring-boot-starter-batch:${springBootVersion}") // need spring batch
implementation("com.navercorp.spring:spring-boot-starter-batch-plus:${springBatchPlusVersion}")
```

### Maven

Kotlin

```xml
<!-- need spring batch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
    <version>{springBootVersion}</version>
</dependency>
<dependency>
    <groupId>com.navercorp.spring</groupId>
    <artifactId>spring-boot-starter-batch-plus-kotlin</artifactId>
    <version>{springBatchPlusVersion}</version>
</dependency>
```

Java

```xml
<!-- need spring batch -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
    <version>{springBootVersion}</version>
</dependency>
<dependency>
    <groupId>com.navercorp.spring</groupId>
    <artifactId>spring-boot-starter-batch-plus</artifactId>
    <version>{springBatchPlusVersion}</version>
</dependency>
```

## User Guide

- 1.1.x
    - [Korean](https://github.com/naver/spring-batch-plus/tree/1.1.x/doc/ko)
    - [English](https://github.com/naver/spring-batch-plus/tree/1.1.x/doc/en)
- 1.0.x
    - [Korean](https://github.com/naver/spring-batch-plus/tree/1.0.x/doc/ko)
    - [English](https://github.com/naver/spring-batch-plus/tree/1.0.x/doc/en)
- 0.3.x
    - [Korean](https://github.com/naver/spring-batch-plus/tree/0.3.x/doc/ko)
    - [English](https://github.com/naver/spring-batch-plus/tree/0.3.x/doc/en)

## Build from source

### Prerequisites

- Jdk 17 or higher
- Kotlin 1.5 or higher

### Build

- Clean: `./gradlew clean`
- Check: `./gradlew check`
    - Coverage report will be generated in `${project}/build/jacoco/html/index.html`
- Assemble: `./gradlew build`
- Install to local: `./gradlew install`
- Publish: `./gradlew publish --no-parallel`

## License

```
   Copyright (c) 2022-present NAVER Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
