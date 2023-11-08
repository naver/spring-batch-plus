# Flow Step

- [Pass a flow as a variable](#pass-a-flow-as-a-variable)
- [Initialize a flow when defining a step](#initialize-a-flow-when-defining-a-step)
- [Get a flow using the bean name](#get-a-flow-using-the-bean-name)

A flow step consists of another single `Flow`.

## Pass a flow as a variable

You can pass a predefined `Flow` as a variable to define a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                flow(anotherFlow())
            }
        }
    }

    @Bean
    open fun anotherFlow(): Flow = batch {
        flow("anotherFlow") {
            step("anotherFlowStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## Initialize a flow when defining a step

You can initialize a `Flow` to when you define a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                flow("anotherFlow") {
                    step("anotherFlowStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
            }
        }
    }
}
```

## Get a flow using the bean name

You can define a `Flow` as a bean and use the bean name to get the `Flow`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                flowBean("anotherFlow")
            }
        }
    }

    @Bean
    open fun anotherFlow(): Flow = batch {
        flow("anotherFlow") {
            step("anotherFlowStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```
