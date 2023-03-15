# Job Flow - Using Other Flows

- [Sequential execution of flows](#sequential-execution-of-flows)
  - [Pass a flow as a variable](#pass-a-flow-as-a-variable)
  - [Initialize a flow when defining a job](#initialize-a-flow-when-defining-a-job)
  - [Get a flow using the bean name](#get-a-flow-using-the-bean-name)
- [Conditional execution of flows](#conditional-execution-of-flows)
  - [Pass a flow as a variable](#pass-a-flow-as-a-variable-1)
  - [Initialize a flow when defining a job](#initialize-a-flow-when-defining-a-job-1)
  - [Get a flow using the bean name](#get-a-flow-using-the-bean-name-1)

In Spring Batch, a `Job` usually consists of `Steps` but can also contain other `Flows`, which can be run sequentially or conditionally. For conditional execution, you can define the next action.

## Sequential execution of flows

The Kotlin DSL helps you run `Flows` sequentially. You can add a `Flow` using a method and pass it as a variable, initialize it or get it using the bean name when you define a `Job`.

### Pass a flow as a variable

You can pass a predefined `Flow` as a variable to define a `Job`. You can declare a flow as a method like testFlow1 and testFlow2, or as a variable to pass like testFlow3.

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
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
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
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
            step("testFlow1Step2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    open fun testFlow2(): Flow = batch {
        flow("testFlow2") {
            step("testFlow2Step1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

### Initialize a flow when defining a job

You can initialize a `Flow` when you define a `Job`.

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
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                }
                step("testFlow1Step2") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                }
            }
            flow("testFlow2") {
                step("testFlow2Step1") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
                }
            }
        }
    }
}
```

### Get a flow using the bean name

You can also get a `Flow` using the bean name when you define a `Job`.

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
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
            step("testFlow1Step2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    open fun testFlow2(
        batch: BatchDsl
    ): Flow = batch {
        flow("testFlow2") {
            step("testFlow2Step1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

## Conditional execution of flows

The Kotlin DSL helps you run `Flows` conditionally based on the result of the previous flow. As in the sequential execution of flows, you can add a `Flow` using a method and pass it as a variable, initialize it or get it using the bean name when you define a `Job`. A `Flow` stops or another `Step` or `Flow` is run based on the result of the previous `Flow`. For more information about how to decide what to run based on the result of a `Flow`, see [Job Flow - Transition from a Flow](./job-flow-transition.md).
 
### Pass a flow as a variable

You can pass a predefined `Flow` as a variable when defining a `Job`. You can use a trailing lambda to define a `Flow` of the `Job`.

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
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
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
                tasklet(
                    { _, _ -> throw IllegalStateException("testStep failed") },
                    ResourcelessTransactionManager()
                )
            }
        }
    }
}
```

### Initialize a flow when defining a job

You can initialize a `Flow` when defining a `Job`. You can use a trailing lambda to define a `Flow` of the `Job`.

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
                        tasklet(
                            { _, _ -> throw IllegalStateException("testStep failed") },
                            ResourcelessTransactionManager()
                        )
                    }
                }
            ) {
                on("COMPLETED") {
                    end()
                }
                on("FAILED") {
                    step("transitionStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
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

### Get a flow using the bean name

You can also get a `Flow` using the bean name when defining a `Job`. You can use a trailing lambda to define a `Flow` of the `Job`.

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
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
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
                tasklet(
                    { _, _ -> throw IllegalStateException("testStep failed") },
                    ResourcelessTransactionManager()
                )
            }
        }
    }
}
```
