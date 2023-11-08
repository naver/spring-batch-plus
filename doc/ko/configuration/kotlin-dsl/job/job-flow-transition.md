# Job Flow - Flow에서 Transition 하는 방법

- [다른 Step 수행](#다른-step-수행)
  - [Step을 변수로 넘기기](#step을-변수로-넘기기)
  - [내부에서 Step를 초기화 하기](#내부에서-step를-초기화-하기)
  - [Bean 이름으로 Step을 가져오기](#bean-이름으로-step을-가져오기)
  - [Transition할 Step에서 Transition하기](#transition할-step에서-transition하기)
- [다른 Flow 수행](#다른-flow-수행)
  - [Flow를 변수로 넘기기](#flow를-변수로-넘기기)
  - [내부에서 Flow를 초기화 하기](#내부에서-flow를-초기화-하기)
  - [Bean 이름으로 Flow를 가져오기](#bean-이름으로-flow를-가져오기)
  - [Transition할 Flow에서 Transition하기](#transition할-flow에서-transition하기)
- [다른 JobExecutionDecider 수행하기](#다른-jobexecutiondecider-수행하기)
  - [JobExecutionDecider를 변수로 넘기기](#jobexecutiondecider를-변수로-넘기기)
  - [Bean 이름으로 JobExecutionDecider를 가져오기](#bean-이름으로-jobexecutiondecider를-가져오기)
- [종료하기](#종료하기)
  - [End](#end)
  - [Fail](#fail)
  - [Stop](#stop)
- [Stop하고 재수행 시 다른 작업 수행하게 설정](#stop하고-재수행-시-다른-작업-수행하게-설정)
  - [Step 수행](#step-수행)
  - [Flow 수행](#flow-수행)
  - [Decider 수행](#decider-수행)

Spring Batch에서 `Step`, `Flow`, `JobExecutionDecider`를 수행 후 `ExitStatus`에 따라 어떤 작업을 수행할지 결정하는 것을 Transition 이라 합니다. Kotlin DSL에서는 Transition 을 선언형으로 정의하는 기능을 제공합니다.

## 다른 Step 수행

`ExitStatus`에 따라 다른 `Step`을 수행할 수 있습니다. 변수로 미리 선언하고 넘기거나 직접 `Step`을 초기화 하거나, Bean 이름으로 Transition할 `Step`을 선택할 수도 있습니다.

### Step을 변수로 넘기기

`Step`을 미리 정의하고 이를 Transition 할 때 전달할 수 있습니다.

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
                    step(transitionStep())
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

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### 내부에서 Step를 초기화 하기

Transition을 정의할 때 `Step`을 초기화 할 수 있습니다.

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

### Bean 이름으로 Step을 가져오기

Transition을 정의할 때 수행할 `Step`을 Bean으로 가져올 수 있습니다.

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
                    stepBean("transitionStep")
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

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Transition할 Step에서 Transition하기

Transition을 통해 수행할 `Step`에서도 Transition을 정의할 수 있습니다. 다음의 예시는 마지막에 nestedStep을 수행하고 종료합니다.

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
                    step(transitionStep()) {
                        on("COMPLETED") {
                            fail()
                        }
                        on("*") {
                            step("nestedStep") {
                                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                            }
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("testStep failed") },
                transactionManager,
            )
        }
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("transitionStep failed") },
                transactionManager,
            )
        }
    }
}
```

## 다른 Flow 수행

`ExitStatus`에 따라 다른 `Flow`를 수행할 수 있습니다. 변수로 미리 선언하고 넘기거나 직접 `Flow`를 초기화 하거나, Bean 이름으로 Transition할 `Flow`를 선택할 수도 있습니다.

### Flow를 변수로 넘기기

`Flow`를 미리 정의하고 이를 Transition 할 때 전달할 수 있습니다.

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
                    flow(transitionFlow())
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

    @Bean
    open fun transitionFlow(): Flow = batch {
        flow("transitionFlow") {
            step("transitionStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

### 내부에서 Flow를 초기화 하기

Transition을 정의할 때 `Flow`를 초기화 할 수 있습니다.

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
                    flow("transitionFlow") {
                        step("transitionStep") {
                            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
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

### Bean 이름으로 Flow를 가져오기

Transition을 정의할 때 수행할 `Flow`를 Bean으로 가져올 수 있습니다.

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
                    flowBean("transitionFlow")
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

    @Bean
    open fun transitionFlow(): Flow = batch {
        flow("transitionFlow") {
            step("transitionStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

### Transition할 Flow에서 Transition하기

Transition을 통해 수행할 `Flow`에서도 Transition을 정의할 수 있습니다. 다음의 예시는 마지막에 nestedStep을 수행하고 종료합니다.

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
                    flow(transitionFlow()) {
                        on("COMPLETED") {
                            fail()
                        }
                        on("*") {
                            step("nestedStep") {
                                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                            }
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("testStep failed") },
                transactionManager,
            )
        }
    }

    @Bean
    open fun transitionFlow(): Flow = batch {
        flow("transitionFlow") {
            step("transitionStep") {
                tasklet(
                    { _, _ -> throw IllegalStateException("transitionStep failed") },
                    transactionManager,
                )
            }
        }
    }
}
```

## 다른 JobExecutionDecider 수행하기

`ExitStatus`에 따라 다른 `JobExecutionDecider`를 수행할 수 있습니다. 다른 `JobExecutionDecider`를 정의할 경우 `JobExecutionDecider`의 결과에 따른 Transition을 정의해야 합니다.

### JobExecutionDecider를 변수로 넘기기

`JobExecutionDecider`를 미리 변수로 정의하고 이를 Transition 할 때 전달할 수 있습니다. `JobExecutionDecider`를 정의할 경우 이어지는 Transition을 정의해야 합니다. 다음 예시는 testDecider의 결과에 따라 마지막에 transitionStep을 수행하고 종료합니다.

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
                    decider(testDecider()) {
                        on("COMPLETED") {
                            stop()
                        }
                        on("BATCH TEST") {
                            step(transitionStep())
                        }
                        on("*") {
                            fail()
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("testStep failed") },
                transactionManager,
            )
        }
    }

    @Bean
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus("BATCH TEST")
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Bean 이름으로 JobExecutionDecider를 가져오기

`JobExecutionDecider`를 Bean으로 Bean 이름을 통해 Transition 할 때 전달할 수 있습니다. `JobExecutionDecider`를 정의할 경우 이어지는 Transition을 정의해야 합니다. 다음 예시는 testDecider의 결과에 따라 마지막에 transitionStep을 수행하고 종료합니다.

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
                    deciderBean("testDecider") {
                        on("COMPLETED") {
                            stop()
                        }
                        on("BATCH TEST") {
                            step(transitionStep())
                        }
                        on("*") {
                            fail()
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet(
                { _, _ -> throw IllegalStateException("testStep failed") },
                transactionManager,
            )
        }
    }

    @Bean
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus("BATCH TEST")
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

## 종료하기

`ExitStatus`에 따라 배치를 종료하는 방법이 있습니다. 종료를 할 때 새로운 `ExitStatus`를 지정하여 종료하거나 강제로 실패시킬 수 있고, `Job`을 강제로 멈추게 할 수 있습니다.

### End

end를 호출할 경우 새로운 `ExitStatus`를 지정하며 종료됩니다. 다음의 예시는 마지막에 SKIPPED `ExitStatus`를 리턴하며 종료합니다.

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
                    stop()
                }
                on("FAILED") {
                    end("SKIPPED")
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

### Fail

fail을 호출할 경우 강제로 `Job`을 종료시킬 수 있습니다. 다음의 예시는 마지막에 FAILED `ExitStatus`를 리턴하며 종료합니다.

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
                    fail()
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
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Stop

fail을 호출할 경우 강제로 `Job`을 종료시킬 수 있습니다. 다음의 예시는 마지막에 STOPPED `ExitStatus`를 리턴하며 종료합니다.

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
                    stop()
                }
                on("*") {
                    end()
                }
            }
        }
    }

    @Bean
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

## Stop하고 재수행 시 다른 작업 수행하게 설정

`ExitStatus`에 따라 배치를 stop시킬 수 있습니다. Spring Batch에서는 stop 이후 재수행시 어떤 작업을 할지 결정할 수 있습니다. Kotlin DSL에서도 이 방법을 `stopAndRestartToXXX` method로 제공합니다.

### Step 수행

`ExitStatus`에 따라 배치를 stop시키고 같은 `JobParameters`로 다시 수행시켰을 때 수행할 `Step`을 지정할 수 있습니다.

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
                    stopAndRestartToStep("restartStep") {
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
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Flow 수행

`ExitStatus`에 따라 배치를 stop시키고 같은 `JobParameters`로 다시 수행시켰을 때 수행할 `Flow`를 지정할 수 있습니다.

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
                    stopAndRestartToFlow("restartFlow") {
                        flow("restartFlow") {
                            step("restartStep") {
                                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                            }
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

### Decider 수행

`ExitStatus`에 따라 배치를 stop시키고 같은 `JobParameters`로 다시 수행시켰을 때 수행할 `JobExecutionDecider`를 지정할 수 있습니다. `JobExecutionDecider`를 지정할 경우 이어지는 Transtion을 명시해야 합니다. 다음의 예시는 첫 수행에서는 STOPPED로 종료하고 두 번째 수행에서는 testDecider가 수행되어서 `ExitStatus`에 따라 FAILED로 종료합니다.

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
                    stopAndRestartToDecider(testDecider()) {
                        on("COMPLETED") {
                            end()
                        }
                        on("BATCH TEST") {
                            fail()
                        }
                        on("*") {
                            end()
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }

    @Bean
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus("BATCH TEST")
    }
}
```
