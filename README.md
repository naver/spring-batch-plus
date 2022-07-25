# Spring Batch Plus

Spring Batch Plus provides extension features to [Spring Batch](https://github.com/spring-projects/spring-batch).

## Features

### Kotlin Dsl for builders

```kotlin
@Bean
fun subJob1(batch: BatchDsl): Job = batch {
    job("subJob1") {
      steps {
        step("testStep1") {
          tasklet { _, _ ->
            RepeatStatus.FINISHED
          }
        }
      }
    }
  }

@Bean
fun subJob2(batch: BatchDsl): Job = batch {
  job("subJob2") {
    steps {
      step("testStep2") {
        tasklet { _, _ ->
          RepeatStatus.FINISHED
        }
      }
    }
  }
}

@Bean
fun testJob(batch: BatchDsl): Job = batch {
    job("testJob") {
        steps {
            step("jobStep1") {
                jobBean("subJob1")
            }
            step("jobStep2") {
                jobBean("subJob2")
            }
        }
    }
}
```

### Single class reader-processor-writer

```kotlin
// single class
@Component
@StepScope
class SampleTasklet : ItemStreamReaderProcessorWriter<Int, String> {
    private var count = 0

    override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
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

    override fun write(items: List<String>) {
        println(items)
    }
}

// usage
@Bean
fun testJob(
    sampleTasklet: SampleTasklet,
    batch: BatchDsl,
): Job = batch {
    job("testJob") {
        steps {
            step("testStep") {
                chunk<Int, String>(3) {
                    reader(sampleTasklet.asItemStreamReader())
                    processor(sampleTasklet.asItemProcessor())
                    writer(sampleTasklet.asItemStreamWriter())
                }
            }
        }
    }
}
```

## Samples

We provide sample project for some use cases.

- [Kotlin Dsl Sample](./spring-batch-plus-sample/spring-batch-plus-kotlin-dsl-sample/src/main/kotlin)
- Single class reader-processor-writer sample
    - [Kotlin version](./spring-batch-plus-sample/spring-batch-plus-single-class-reader-processor-writer-kotlin/src/main/kotlin)
    - [Java version](./spring-batch-plus-sample/spring-batch-plus-single-class-reader-processor-writer/src/main/java)

## Compatibility

We've tested following versions only. Other versions may not work.

| Spring Batch Version | Compatible Plus Version |
|----------------------|-------------------------|
| 4.3.x                | 0.1.x                   |

## Download

Since it provides extension features to spring batch, need to used with [spring batch](https://github.com/spring-projects/spring-batch).

### Gradle

Kotlin

```kotlin
implementation("org.springframework.batch:spring-batch-core:${springBatchVersion}") // need spring batch
implementation("com.navercorp.spring:spring-boot-starter-batch-plus-kotlin:${springBatchPlusVersion}")
```

Java

```kotlin
implementation("org.springframework.batch:spring-batch-core:${springBatchVersion}") // need spring batch
implementation("com.navercorp.spring:spring-boot-starter-batch-plus:${springBatchPlusVersion}")
```

### Maven

Kotlin

```xml
<!-- need spring batch -->
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-core</artifactId>
    <version>{springBatchVersion}</version>
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
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-core</artifactId>
    <version>{springBatchVersion}</version>
</dependency>
<dependency>
    <groupId>com.navercorp.spring</groupId>
    <artifactId>spring-boot-starter-batch-plus</artifactId>
    <version>{springBatchPlusVersion}</version>
</dependency>
```

## Build from source

### Prerequisites

- Jdk8 or higher

### Build

- Clean: `./gradlew clean`
- Check: `./gradlew check`
- Assemble: `./gradlew build`
- Install to local: `./gradlew install`
- Publish: `./gradlew publish`

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
