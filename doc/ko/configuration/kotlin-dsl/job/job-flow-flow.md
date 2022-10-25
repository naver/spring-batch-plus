# Job Flow - 다른 Flow 사용

- [Flow 순차 수행](#flow-순차-수행)
  - [Flow를 변수로 넘기기](#flow를-변수로-넘기기)
  - [Job을 정의할 때 Flow를 초기화하기](#job을-정의할-때-flow를-초기화하기)
  - [Bean 이름으로 Flow 가져오기](#bean-이름으로-flow-가져오기)
- [Flow 분기 수행](#flow-분기-수행)
  - [변수로 Flow 넘기기](#변수로-flow-넘기기)
  - [Job을 정의할 때 Flow를 초기화하기](#job을-정의할-때-flow를-초기화하기-1)
  - [Bean 이름으로 Flow 가져오기](#bean-이름으로-flow-가져오기-1)

Spring Batch의 `Job`은 `Step`들로 구성되어 있으나 다른 `Flow`로도 구성할 수 있습니다. `Flow`는 순차 수행 뿐만 아니라 분기 수행도 할 수 있습니다. 분기 수행의 경우 다음 동작을 정의 할 수 있습니다.

## Flow 순차 수행

Kotlin DSL은 `Flow`들을 순차 수행할 수 있습니다. 수행할 `Flow`는 method로 등록해서 변수를 넘겨서 호출할 수도 있고 `Job`을 정의할 때 초기화를 하거나, Bean 이름으로 가져올 수도 있습니다.

### Flow를 변수로 넘기기

`Job`을 정의할 때 미리 정의한 `Flow`를 변수로 넘겨서 `Job`을 정의할 수 있습니다. testFlow1, testFlow2처럼 별도의 method로 선언하여 넘길 수도 있지만 testFlow3처럼 내부에서 따로 변수로 선언하고 넘길 수도 있습니다.

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
                    step("testFlow3Step1") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
            }

            flow(testFlow1())
            flow(testFlow2())
            flow(testFlow3)
        }
    }

    @Bean
    open fun testFlow1(): Flow = batch {
        flow("testFlow1") {
            step("testFlow1Step1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("testFlow1Step2") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }

    @Bean
    open fun testFlow2(): Flow = batch {
        flow("testFlow2") {
            step("testFlow2Step1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

### Job을 정의할 때 Flow를 초기화하기

`Job`을 정의할 때 `Flow`를 내부에서 초기화하여 사용할 수도 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            flow("testFlow1") {
                step("testFlow1Step1") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
                step("testFlow1Step2") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
            flow("testFlow2") {
                step("testFlow2Step1") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }
        }
    }
}
```

### Bean 이름으로 Flow 가져오기

`Job`을 정의할 때 Bean 이름으로 `Flow`를 가져올 수도 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            flowBean("testFlow1")
            flowBean("testFlow2")
        }
    }

    @Bean
    open fun testFlow1(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow1") {
            step("testFlow1Step1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("testFlow1Step2") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }

    @Bean
    open fun testFlow2(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow2") {
            step("testFlow2Step1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

## Flow 분기 수행

Kotlin DSL은 `Flow`의 결과에 따라 분기하는 방식을 제공합니다. `Flow`를 순차 수행할 때와 마찬가지로 수행할 `Flow`를 method로 등록해서 변수를 넘겨서 호출할 수도 있고 `Job`을 정의할 때 초기화를 하거나, Bean 이름으로 가져올 수도 있습니다. 앞선 `Flow`의 결과에 따라 stop할 수도 있고 다른 `Step`이나 `Flow`를 수행할 수도 있습니다. `Flow`의 결과에 따라 어떤 동작을 할지 설정하는 자세한 방법은 [Job Flow - Transition 하는 방법](./job-flow-transition.md)을 참고하기 바랍니다.
 
### 변수로 Flow 넘기기

`Job`을 정의할 때 미리 정의한 `Flow`를 변수로 넘길 수 있습니다. `Flow`를 설정할 때 trailing lambda를 사용하여 `Job`의 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            flow(testFlow()) {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet { _, _ ->
                            RepeatStatus.FINISHED
                        }
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    open fun testFlow(): Flow = batch {
        flow("testFlow") {
            step("testStep") {
                tasklet { _, _ ->
                    throw IllegalStateException("testStep failed")
                }
            }
        }
    }
}
```

### Job을 정의할 때 Flow를 초기화하기

`Job`을 정의할 때 `Flow`를 직접 초기화하여 사용할 수도 있습니다. `Flow`를 설정할 때 trailing lambda를 사용하여 `Job`의 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            flow(
                "testFlow",
                {
                    step("testStep") {
                        tasklet { _, _ ->
                            throw IllegalStateException("testStep failed")
                        }
                    }
                }
            ) {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet { _, _ ->
                            RepeatStatus.FINISHED
                        }
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

### Bean 이름으로 Flow 가져오기

`Job`을 정의할 때 Bean 이름으로 `Flow`를 가져올 수도 있습니다. `Flow`를 설정할 때 trailing lambda를 사용하여 `Job`의 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            flowBean("testFlow") {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet { _, _ ->
                            RepeatStatus.FINISHED
                        }
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    open fun testFlow(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow") {
            step("testStep") {
                tasklet { _, _ ->
                    throw IllegalStateException("testStep failed")
                }
            }
        }
    }
}
```