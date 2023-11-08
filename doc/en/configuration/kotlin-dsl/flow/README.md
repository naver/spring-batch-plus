# Flow

- [Create a flow](#create-a-flow)

A `Flow` defines the flow of a `Job`. Although you can create a `Flow` when you define a `Job` as in [Set a job flow](../job/README.md#set-a-job-flow), you can also directly create it as another object. For more information about how a `Flow` works, including sequential and conditional execution, which is the same as how a `Job` works except that a `Flow` can be created as another object, see [Set a job flow](../job/README.md#set-a-job-flow).

## Create a flow

You can use `BatchDsl` to create a `Flow`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            flow(testFlow())
        }
    }

    @Bean
    open fun testFlow(): Flow = batch {
        flow("testFlow") {
            step(testStep()) {
                on("COMPLETED") {
                    stop()
                }
                on("*") {
                    fail()
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
