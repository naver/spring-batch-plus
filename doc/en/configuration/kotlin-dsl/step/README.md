# Step

- [How to create a step](#how-to-create-a-step)
- [How to create a step by type](#how-to-create-a-step-by-type)
- [How to set a step](#how-to-set-a-step)

In Spring Batch, a `Job` runs by `Step`. There are 5 types of `Steps`. Spring Batch Plus helps you create 5 types of `Steps` using the Kotlin DSL.

## How to create a step

There are two ways to create a `Step`. The first way is to call a step in `BatchDsl`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testStep() = batch {
        step("testStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

The second way is to create a `Step` during the process of creating a `Job` or a `Flow` that basically contains `Steps`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            // within job
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            step("testStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            flow(testFlow())
            step(testStep())
        }
    }

    @Bean
    open fun testFlow() = batch {
        flow("testFlow") {
            // within flow
            step("flowStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            step("flowStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

You can use either way to create any type of `Steps`.

## How to create a step by type

There are 5 types of `Steps` in Spring Batch. `TaskletStep` is a `Step` that consists of a single `Tasklet`. `ChunkOrientedStep` is a `Step` that consists of `ItemReader`, `ItemProcessor`, and `ItemWriter`. `PartitionStep` is a `Step` that partitions a `Step` into multiple tasks and runs them. `JobStep` and `FlowStep` are a `Step` that consists of a single `Job` and a single `Flow`, respectively. For more information about how to create each `Step`, see the following topics:

- [TaskletStep](./tasklet-step.md)
- [ChunkOrientedStep](./chunk-oriented-step.md)
- [PartitionStep](./partition-step.md)
- [JobStep](./job-step.md)
- [FlowStep](./flow-step.md)

## How to set a step

Spring Batch helps you set a listener or transactionManager for a `Step`. The Kotlin DSL for Spring Batch Plus also helps you do so. For more information, see the following topic:

- [Step Configuration](./step-configuration.md)
