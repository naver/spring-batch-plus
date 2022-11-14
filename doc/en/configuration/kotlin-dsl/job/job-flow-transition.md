# Job Flow - Transition from a Flow

- [Run another step](#run-another-step)
  - [Pass a step as a variable](#pass-a-step-as-a-variable)
  - [Initialize a step internally](#initialize-a-step-internally)
  - [Get a step using the bean name](#get-a-step-using-the-bean-name)
  - [Define a transition in a step](#define-a-transition-in-a-step)
- [Run another flow](#run-another-flow)
  - [Pass a flow as a variable](#pass-a-flow-as-a-variable)
  - [Initialize a flow internally](#initialize-a-flow-internally)
  - [Get a flow using the bean name](#get-a-flow-using-the-bean-name)
  - [Define a transition in a flow](#define-a-transition-in-a-flow)
- [Run another JobExecutionDecider](#run-another-jobexecutiondecider)
  - [Pass a JobExecutionDecider as a variable](#pass-a-jobexecutiondecider-as-a-variable)
  - [Get a JobExecutionDecider using the bean name](#get-a-jobexecutiondecider-using-the-bean-name)
- [End a batch](#end-a-batch)
  - [End](#end)
  - [Fail](#fail)
  - [Stop](#stop)
- [Run another step, flow or decider on stop and restart](#run-another-step-flow-or-decider-on-stop-and-restart)
  - [Run a step](#run-a-step)
  - [Run a flow](#run-a-flow)
  - [Run a decider](#run-a-decider)

A transition is deciding what to run based on `ExitStatus` after Spring Batch runs a `Step`, `Flow`, or `JobExecutionDecider`. The Kotlin DSL helps you declaratively define a transition.

## Run another step

You can run a different `Step` based on `ExitStatus`, by declaring it as a variable and passing it, directly initializing it, or selecting it with the bean name.

### Pass a step as a variable

You can define a `Step` in advance and pass it for a transition.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

### Initialize a step internally

You can initialize a `Step` when you define a transition.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }
}
```

### Get a step using the bean name

You can get a `Step` to run using the bean name when you define a transition.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

### Define a transition in a step

You can also define a transition in a `Step` to run for a transition. The following example runs nestedStep last and ends the job.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
                                tasklet { _, _ ->
                                    RepeatStatus.FINISHED
                                }
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                throw IllegalStateException("transitionStep failed")
            }
        }
    }
}
```

## Run another flow

You can run a different `Flow` based on `ExitStatus`, by declaring it as a variable and passing it, directly initializing it, or selecting it with the bean name.

### Pass a flow as a variable

You can define a `Flow` in advance and pass it for a transition.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun transitionFlow(): Flow = batch {
        flow("transitionFlow") {
            step("transitionStep") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```

### Initialize a flow internally

You can initialize a `Flow` when you define a transition.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
                            tasklet { _, _ ->
                                RepeatStatus.FINISHED
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }
}
```

### Get a flow using the bean name

You can get a `Flow` to run using the bean name when you define a transition.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun transitionFlow(): Flow = batch {
        flow("transitionFlow") {
            step("transitionStep") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```

### Define a transition in a flow

You can define a transition in a `Flow` to run for a transition. The following example runs nestedStep last and ends the job.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
                                tasklet { _, _ ->
                                    RepeatStatus.FINISHED
                                }
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun transitionFlow(): Flow = batch {
        flow("transitionFlow") {
            step("transitionStep") {
                tasklet { _, _ ->
                    throw IllegalStateException("transitionStep failed")
                }
            }
        }
    }
}
```

## Run another JobExecutionDecider

You can run a different `JobExecutionDecider` based on `ExitStatus`. When you define a `JobExecutionDecider`, you need to define a transition based on the result of the `JobExecutionDecider`.

### Pass a JobExecutionDecider as a variable

You can define a `JobExecutionDecider` as a variable in advance and pass it for a transition. When you define a `JobExecutionDecider`, you need to define a subsequent transition. The following example runs transitionStep last based on the result of testDecider and ends the job.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus("BATCH TEST")
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

### Get a JobExecutionDecider using the bean name

You can pass a `JobExecutionDecider` using the bean name, for a transition. When you define a `JobExecutionDecider`, you need to define a subsequent transition. The following example runs transitionStep last based on the result of testDecider and ends the job.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }

    @Bean
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus("BATCH TEST")
    }

    @Bean
    open fun transitionStep(): Step = batch {
        step("transitionStep") {
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

## End a batch

You can end a batch based on `ExitStatus`: you can specify a new value for `ExitStatus` to end a `Job`, forcibly fail or stop it.

### End

The `end()` ends a job with a new value for `ExitStatus` specified when called. The following example ends the job, returning `ExitStatus` SKIPPED.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }
}
```

### Fail

The `fail()` forcibly ends a `Job` when called. The following example ends the job, returning `ExitStatus` FAILED.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

### Stop

The `fail()` forcibly ends a `Job` when called. The following example ends the job, returning `ExitStatus` STOPPED.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

## Run another step, flow or decider on stop and restart

You can stop a batch based on `ExitStatus`. Spring Batch helps you decide what to run after a job is stopped and restarted. The Kotlin DSL also helps you do so with the `stopAndRestartToXXX` method.

### Run a step

You can specify a `Step` to run when you stop a batch and restart it with the same `JobParameters` based on `ExitStatus`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(testStep()) {
                on("COMPLETED") {
                    stopAndRestartToStep("restartStep") {
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

### Run a flow

You can specify a `Flow` to run when you stop a batch and restart it with the same `JobParameters` based on `ExitStatus`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(testStep()) {
                on("COMPLETED") {
                    stopAndRestartToFlow("restartFlow") {
                        flow("restartFlow") {
                            step("restartStep") {
                                tasklet { _, _ ->
                                    RepeatStatus.FINISHED
                                }
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
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }
}
```

### Run a decider

You can specify a `JobExecutionDecider` to run when you stop a batch and restart it with the same `JobParameters` based on `ExitStatus`. When you specify a `JobExecutionDecider`, you need to specify a subsequent transition. The following example ends the job with `ExitStatus` STOPPED at the first execution, and with `ExitStatus` FAILED after testDecider is run at the second.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
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
            tasklet { _, _ ->
                RepeatStatus.FINISHED
            }
        }
    }

    @Bean
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus("BATCH TEST")
    }
}
```
