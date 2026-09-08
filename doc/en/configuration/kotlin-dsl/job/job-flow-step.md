# Job Flow - Using Steps

- [Sequential execution of steps](#sequential-execution-of-steps)
  - [Pass a step as a variable](#pass-a-step-as-a-variable)
  - [Initialize a step when defining a job](#initialize-a-step-when-defining-a-job)
  - [Get a step using the bean name](#get-a-step-using-the-bean-name)
- [Conditional execution of steps](#conditional-execution-of-steps)
  - [Pass a step as a variable](#pass-a-step-as-a-variable-1)
  - [Initialize a step when defining a job](#initialize-a-step-when-defining-a-job-1)
  - [Get a step using the bean name](#get-a-step-using-the-bean-name-1)

In Spring Batch, a `Job` consists of one or more `Steps`, which can be run sequentially or conditionally based on the result of the previous `Step`. The Kotlin DSL lets you set this declaratively. For a comparison with `JobBuilder` and `StepBuilder`, see [Kotlin DSL](../README.md).

## Sequential execution of steps

The Kotlin DSL helps you run `Steps` sequentially. You can add a `Step` using a method and pass it as a variable, initialize it or get it using the bean name when you define a `Job`.

### Pass a step as a variable

You can pass a predefined `Step` as a variable to define a `Job`. You can declare a step as a method like testStep1 and testStep2, or as a variable to pass like testStep3.

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

### Initialize a step when defining a job

You can initialize a `Step` when you define a `Job`.

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

### Get a step using the bean name

You can also get a `Step` using the bean name when you define a `Job`.

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

## Conditional execution of steps

The Kotlin DSL helps you run `Steps` conditionally based on the result of the previous one. As in the sequential execution of `Steps`, you can add a `Step` using a method and pass it as a variable, initialize it or get it using the bean name when you define a `Job`. A `Step` stops or another `Step` or `Flow` is run based on the result of the previous `Step`. For more information about how to decide what to run based on the result of a `Step`, see [Job Flow - Transition from a Flow](./job-flow-transition.md).

### Pass a step as a variable

You can pass a predefined `Step` as a variable when defining a `Job`. You can use a trailing lambda to define a `Step` of the `Job`.

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

### Initialize a step when defining a job

You can initialize a `Step` when you define a `Job`. You can use a trailing lambda to define a `Step` of the `Job`.

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

### Get a step using the bean name

You can also get a `Step` using the bean name when you define a `Job`. You can use a trailing lambda to define a `Step` of the `Job`.

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
