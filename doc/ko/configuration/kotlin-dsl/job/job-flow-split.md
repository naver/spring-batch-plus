# Job Flow - Flow 병렬처리

- [Flow 병렬 처리하기](#flow-병렬-처리하기)
  - [Flow를 변수로 넘기기](#flow를-변수로-넘기기)
  - [내부에서 Flow를 초기화 하기](#내부에서-flow를-초기화-하기)
  - [Bean 이름으로 Flow를 가져오기](#bean-이름으로-flow를-가져오기)

Spring Batch는 여러 `Flow`를 동시에 수행할 수 있습니다. Kotlin DSL도 동일한 기능을 제공합니다.

## Flow 병렬 처리하기

`TaskExecutor`를 지정하면 해당 Executor에서 `Flow`를 병렬 수행할 수 있습니다. 수행할 `Flow`는 변수로 넘길 수도 있고 `Flow`를 직접 정의하거나, Bean 이름으로 가져올 수도 있습니다.

### Flow를 변수로 넘기기

`TaskExecutor`에서 수행할 `Flow`을 정의할 때 미리 정의한 `Flow`을 변수로 넘길 수 있습니다. testFlow1, testFlow2처럼 별도의 method로 선언하여 넘길 수도 있지만 testFlow3처럼 내부에서 따로 변수로 선언하고 flow의 파라미터로 전달할 수도 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            val testFlow3 = batch {
                flow("testFlow3") {
                    step("testStep3") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    }
                }
            }
            split(SimpleAsyncTaskExecutor()) {
                flow(testFlow1())
                flow(testFlow2())
                flow(testFlow3)
            }
        }
    }

    @Bean
    open fun testFlow1(): Flow = batch {
        flow("testFlow1") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    open fun testFlow2(): Flow = batch {
        flow("testFlow2") {
            step("testStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

### 내부에서 Flow를 초기화 하기

`TaskExecutor`에서 수행할 `Flow`를 정의할 때 내부에서 초기화 할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            split(SimpleAsyncTaskExecutor()) {
                flow("testFlow1") {
                    step("testStep1") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    }
                }
                flow("testFlow2") {
                    step("testStep2") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    }
                }
                flow("testFlow3") {
                    step("testStep3") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                    }
                }
            }
        }
    }
}
```

### Bean 이름으로 Flow를 가져오기

`TaskExecutor`에서 수행할 `Flow`를 정의할 때 Bean 이름으로 `Flow`를 가져올 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            split(SimpleAsyncTaskExecutor()) {
                flowBean("testFlow1")
                flowBean("testFlow2")
                flowBean("testFlow3")
            }
        }
    }

    @Bean
    open fun testFlow1(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow1") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    open fun testFlow2(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow2") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    open fun testFlow3(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow3") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```
