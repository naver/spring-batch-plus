# Job Flow - Step 사용

- [Step 순차 수행](#step-순차-수행)
  - [Step을 변수로 넘기기](#step을-변수로-넘기기)
  - [Job을 정의할 때 Step을 초기화하기](#job을-정의할-때-step을-초기화하기)
  - [Bean 이름으로 Step을 가져오기](#bean-이름으로-step을-가져오기)
- [Step 분기 수행](#step-분기-수행)
  - [Step을 변수로 넘기기](#step을-변수로-넘기기-1)
  - [Job을 정의할 때 Step을 초기화하기](#job을-정의할-때-step을-초기화하기-1)
  - [Bean 이름으로 Step을 가져오기](#bean-이름으로-step을-가져오기-1)

Spring Batch의 `Job`은 하나 또는 여러개의 `Step`으로 구성됩니다. `Step`는 순차 수행 뿐만 아니라 이전 `Step`의 결과에 따른 분기 수행도 가능합니다. 하지만 Spring Batch에서 제공하는 `JobBuilder`, `StepBuilder` 사용 방식에는 일부 문제가 있습니다. 다음은 `JobBuilder`, `StepBuilder`를 사용하여 Job Flow를 설정한 예입니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job {
        return JobBuilder("testJob", jobRepository)
            .start(testStep1()).on("COMPLETED").to(successStep())
            .from(testStep1()).on("FAILED").to(failureStep())
            .from(testStep1()).on("*").stop()
            .build()
            .build()
    }

    @Bean
    open fun testStep1(): Step {
        return StepBuilder("testStep1", jobRepository)
            .tasklet(
                { _, _ ->
                    throw IllegalStateException("step failed")
                },
                transactionManager,
            )
            .build()
    }

    @Bean
    open fun successStep(): Step {
        return StepBuilder("successStep", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun failureStep(): Step {
        return StepBuilder("failureStep", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }
}
```

이 방식에는 몇가지 문제점이 있습니다. 첫 번째로 각 `Step`을 위한 method를 만들고 이걸 이용해야 Job Flow 설정이 가능합니다. 두 번째로 `.from().on(..)` 과정에서 indent나 줄바꿈을 잘못하는 경우 가독성을 해칠 수가 있습니다. 예를들어 다음과 같이 설정하면 동작에는 문제가 없지만 가독성은 심하게 떨어집니다. 이런 방식을 코드 리뷰 과정에서 잡아낼 수도 있지만 IDE의 auto formatting 기능으로는 잡아낼 수가 없습니다.

```kotlin
@Bean
open fun testJob(jobRepository: JobRepository): Job {
    return JobBuilder("testJob", jobRepository)
        .start(testStep1())
        .on("COMPLETED")
        .to(successStep())
        .from(testStep1())
        .on("FAILED")
        .to(failureStep())
        .from(testStep1())
        .on("*")
        .stop()
        .end()
        .build()
}
```

Kotlin DSL을 사용하면 이런 문제들을 해결해서 선언형으로 Job Flow를 설정할 수 있습니다. 다음은 Spring Batch Plus가 제공하는 Kotlin DSL을 활용해서 동일한 동작을 하는 코드를 작성한 예시입니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(testStep1()) {
                on("COMPLETED") {
                    step(successStep())
                }
                on("FAILED") {
                    step("failureStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    open fun testStep1(): Step = batch {
        step("testStep1") {
            tasklet(
                { _, _ ->
                    throw IllegalStateException("step failed")
                },
                transactionManager,
            )
        }
    }

    @Bean
    open fun successStep(): Step = batch {
        step("successStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

Kotlin DSL을 활용하면 `.build()`, 같은 BoilerPlate code를 사용하지 않고 선언형으로 작성할 수 있습니다. 코드 예시를 보면 testStep1, successStep의 경우 기존 코드처럼 method 방식을 활용했지만 failureStep 처럼 Jow Flow 안에서 `Step` 정의를 할 수도 있습니다.

## Step 순차 수행

Kotlin DSL은 `Step`들을 순차 수행할 수 있습니다. 수행할 `Step`은 method로 등록해서 변수를 넘겨서 호출할 수도 있고 `Job`을 정의할 때 초기화를 하거나, Bean 이름으로 가져올 수도 있습니다.

### Step을 변수로 넘기기

`Job`을 정의할 때 미리 정의한 `Step`을 변수로 넘겨서 `Job`을 정의할 수 있습니다. testStep1, testStep2처럼 별도의 method로 선언하여 넘길 수도 있지만 testStep3처럼 내부에서 따로 변수로 선언하고 넘길 수도 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            val testStep3 = batch {
                step("testStep3") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }

            step(testStep1())
            step(testStep2())
            step(testStep3)
        }
    }

    @Bean
    open fun testStep1(): Step = batch {
        step("testStep1") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }

    @Bean
    open fun testStep2(): Step = batch {
        step("testStep2") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Job을 정의할 때 Step을 초기화하기

`Job`을 정의할 때 `Step`을 내부에서 초기화하여 사용할 수도 있습니다.

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
            step("testStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            step("testStep3") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

### Bean 이름으로 Step을 가져오기

`Job`을 정의할 때 Bean 이름으로 `Step`을 가져올 수도 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            stepBean("testStep1")
            stepBean("testStep2")
            stepBean("testStep3")
        }
    }

    @Bean
    open fun testStep1(): Step = batch {
        step("testStep1") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
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
}
```

## Step 분기 수행

Kotlin DSL은 `Step`의 결과에 따라 분기하는 방식을 제공합니다. `Step`을 순차 수행할 때와 마찬가지로 수행할 `Step`은 method로 등록해서 변수를 넘겨서 호출할 수도 있고 `Job`을 정의할 때 초기화를 하거나, Bean 이름으로 가져올 수도 있습니다. 앞선 `Step`의 결과에 따라 stop할 수도 있고 다른 `Step`이나 `Flow`를 수행할 수도 있습니다. `Step`의 결과에 따라 어떤 동작을 할지 설정하는 자세한 방법은 [Job Flow - Transition 하는 방법](./job-flow-transition.md)을 참고하기 바랍니다.

### Step을 변수로 넘기기

`Job`을 정의할 때 미리 정의한 `Step`을 변수로 넘길 수 있습니다. `Step`을 설정할 때 trailing lambda를 사용하여 `Job`의 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(testStep()) {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("testStep failed") },
                transactionManager,
            )
        }
    }
}
```

### Job을 정의할 때 Step을 초기화하기

`Job`을 정의할 때 `Step`을 내부에서 초기화하여 사용할 수도 있습니다. `Step`을 설정할 때 trailing lambda를 사용하여 `Job`의 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(
                "testStep",
                {
                    tasklet(
                        { _, _ -> throw IllegalStateException("testStep failed") },
                        transactionManager,
                    )
                },
            ) {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }
}
```

### Bean 이름으로 Step을 가져오기

`Job`을 정의할 때 Bean 이름으로 `Step`을 가져올 수도 있습니다. `Step`을 설정할 때 trailing lambda를 사용하여 `Job`의 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            stepBean("testStep") {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("testStep failed") },
                transactionManager,
            )
        }
    }
}
```
