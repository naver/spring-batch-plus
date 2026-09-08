# Job Flow - Step 사용

- [Step 순차 수행](#step-순차-수행)
  - [Step을 변수로 넘기기](#step을-변수로-넘기기)
  - [Job을 정의할 때 Step을 초기화하기](#job을-정의할-때-step을-초기화하기)
  - [Bean 이름으로 Step을 가져오기](#bean-이름으로-step을-가져오기)
- [Step 분기 수행](#step-분기-수행)
  - [Step을 변수로 넘기기](#step을-변수로-넘기기-1)
  - [Job을 정의할 때 Step을 초기화하기](#job을-정의할-때-step을-초기화하기-1)
  - [Bean 이름으로 Step을 가져오기](#bean-이름으로-step을-가져오기-1)

Spring Batch의 `Job`은 하나 또는 여러개의 `Step`으로 구성됩니다. `Step`는 순차 수행 뿐만 아니라 이전 `Step`의 결과에 따른 분기 수행도 가능합니다. Kotlin DSL은 이를 선언형으로 설정하는 방법을 제공합니다. `JobBuilder`, `StepBuilder`를 사용하는 방식과의 비교는 [Kotlin DSL](../README.md)을 참고 바랍니다.

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
    open fun testJob(): Job =
        batch {
            job("testJob") {
                val testStep3 =
                    batch {
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
    open fun testStep1(): Step =
        batch {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }

    @Bean
    open fun testStep2(): Step =
        batch {
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
    open fun testJob(): Job =
        batch {
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
    open fun testJob(): Job =
        batch {
            job("testJob") {
                stepBean("testStep1")
                stepBean("testStep2")
                stepBean("testStep3")
            }
        }

    @Bean
    open fun testStep1(): Step =
        batch {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }

    @Bean
    open fun testStep2(): Step =
        batch {
            step("testStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }

    @Bean
    open fun testStep3(): Step =
        batch {
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
    open fun testJob(): Job =
        batch {
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
    open fun testStep(): Step =
        batch {
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
    open fun testJob(): Job =
        batch {
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
    open fun testJob(): Job =
        batch {
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
    open fun testStep(): Step =
        batch {
            step("testStep") {
                tasklet(
                    { _, _ -> throw IllegalStateException("testStep failed") },
                    transactionManager,
                )
            }
        }
}
```
