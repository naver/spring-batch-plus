# Kotlin DSL

- [BatchDsl](#batchdsl)
  - [빈 등록](#빈-등록)
  - [사용 방법](#사용-방법)
- [Job, Step, Flow](#job-step-flow)

Spring Batch 에서는 `Job`, `Step`, `Flow` 객체를 생성하기 위한 DSL을 제공합니다. Spring Batch 에서는 `JobBuilder`, `StepBuilder`, `FlowBuilder`를 통해 `Job`, `Step`, `Flow`를 만들 수 있습니다. 아래는 Kotlin 코드 예시입니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job {
        return JobBuilder("testJob", jobRepository)
            .start(
                StepBuilder("testStep1", jobRepository)
                    .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    .build(),
            )
            .next(testStep2())
            .on("COMPLETED").to(testStep3())
            .from(testStep2())
            .on("FAILED").to(testStep4())
            .end()
            .build()
    }

    @Bean
    open fun testStep2(): Step {
        return StepBuilder("testStep2", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun testStep3(): Step {
        return StepBuilder("testStep3", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun testStep4(): Step {
        return StepBuilder("testStep4", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }
}
```

하지만 이 방식은 Boilerplate code가 많고 Kotlin 스럽지 않다는 문제점이 있습니다. Spring Batch Plus는 이런 문제점을 해결하여 `Job`, `Step` `Flow`설정을 Kotlin DSL을 활용하여 할 수 있는 기능을 제공합니다. 아래는 Kotlin DSL을 사용하여 동일한 동작을 하는 코드를 작성한 예시입니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            step(testStep2()) {
                on("COMPLETED") {
                    step(testStep3())
                }
                on("FAILED") {
                    step(testStep4())
                }
            }
        }
    }

    @Bean
    open fun testStep2(): Step = batch {
        step("testStep2") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }

    @Bean
    open fun testStep3(): Step = batch {
        step("testStep3") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }

    @Bean
    open fun testStep4(): Step = batch {
        step("testStep4") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

Kotlin DSL은 Spring Batch의 기능을 바꾸는 것이 아닌 편하게 감싸주는게 목적이므로 기존 Java 기반의 DSL에서 설정 불가능한 것은 여전히 설정할 수 없습니다. Spring Batch의 상세한 설정 정보는 [Spring Batch Docs](https://spring.io/projects/spring-batch)를 참고바랍니다.

## BatchDsl

Spring Batch Plus에서는 `BatchDsl` class를 제공합니다. `BatchDsl` class를 invoke 해서 Kotlin DSL을 사용할 수 있습니다. `BatchDsl` Bean으로 등록하고 사용하는걸 권장합니다.

### 빈 등록

`spring-boot-starter-batch-plus-kotlin`을 사용하면 `BatchDsl` 빈을 자동으로 등록해줘서 별도의 Bean 등록이 필요하지는 않습니다. 하지만 명시적으로 등록이 필요한 경우 다음의 예시처럼 등록할 수 있습니다.

```kotlin
@Configuration
open class BatchConfig {

    @Bean
    open fun batchDsl(
        beanFactory: BeanFactory,
        jobRepository: JobRepository
    ): BatchDsl {
        return BatchDsl(
            beanFactory,
            jobRepository,
        )
    }
}
```

### 사용 방법

class의 property로 binding한 경우 함수 인자로 binding 하지 않고 바로 사용할 수 있습니다.

```kotlin
@Configuration
class TestJobConfig(
    private val batch: BatchDsl
) {
    @Bean
    fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```

Bean으로 등록된 `BatchDsl`을 함수의 인자로 binding해서 사용할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```


## Job, Step, Flow

`BatchDsl`을 활용하여 `Job`, `Step`, `Flow` 객체를 생성할 수 있습니다. 상세한 방법은 다음 문서들을 참고 바랍니다.

- [Job](./job/README.md)
- [Step](./step/README.md)
- [Flow](./flow/README.md)