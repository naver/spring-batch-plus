# Job Flow - Processing Flows in Parallel

- [Process flows in parallel](#process-flows-in-parallel)
  - [Pass a flow as a variable](#pass-a-flow-as-a-variable)
  - [Initialize a flow internally](#initialize-a-flow-internally)
  - [Get a flow using the bean name](#get-a-flow-using-the-bean-name)

In Spring Batch, you can run multiple `Flows` at the same time. The Kotlin DSL helps you do the same.

## Process flows in parallel

Specify a `TaskExecutor` to run `Flows` in parallel. You can pass `Flows` as variables, directly define them, or get them by specifying the bean name.

### Pass a flow as a variable

When you define a `Flow` to run by a `TaskExecutor`, you can pass a predefined `Flow` as a variable. You can declare a flow as a method like testFlow1 and testFlow2, or as a variable to pass it as a parameter of the flow like testFlow3.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            val testFlow3 = batch {
                flow("testFlow3") {
                    step("testStep3") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
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
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }

    @Bean
    open fun testFlow2(): Flow = batch {
        flow("testFlow2") {
            step("testStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

### Initialize a flow internally

When you define a `Flow` to run by a `TaskExecutor`, you can initialize the flow internally.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            split(SimpleAsyncTaskExecutor()) {
                flow("testFlow1") {
                    step("testStep1") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
                flow("testFlow2") {
                    step("testStep2") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
                flow("testFlow3") {
                    step("testStep3") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
            }
        }
    }
}
```

### Get a flow using the bean name

When you define a `Flow` to run by a `TaskExecutor`, you can get the flow using the bean name.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            split(SimpleAsyncTaskExecutor()) {
                flowBean("testFlow1")
                flowBean("testFlow2")
                flowBean("testFlow3")
            }
        }
    }

    @Bean
    open fun testFlow1(): Flow = batch {
        flow("testFlow1") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }

    @Bean
    open fun testFlow2(): Flow = batch {
        flow("testFlow2") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }

    @Bean
    open fun testFlow3(): Flow = batch {
        flow("testFlow3") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```
