# Job

- [How to create a job](#how-to-create-a-job)
- [Set a job flow](#set-a-job-flow)
- [How to set a job](#how-to-set-a-job)

Spring Batch runs a batch by `Job`. A `Job` consists of one or more `Steps`, which can be run sequentially or conditionally. Spring Batch Plus helps you easily create a `Flow` declaratively. 

## How to create a job

You can use `BatchDsl` to create a `Job`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("testStep2") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

## Set a job flow

`Steps` can be run sequentially, conditionally, or in parallel in multiple threads. To create a `Flow` of a `Job`, you can use another `Flow` or a `JobExecutionDecider` as well as a `Step`. For more information, see the following topics:

- [Using Steps](./job-flow-step.md)
- [Using Other Flows](./job-flow-flow.md)
- [Using a JobExecutionDecider](./job-flow-decider.md)
- [Transition from a Flow](./job-flow-transition.md)
- [Processing Flows in Parallel](./job-flow-split.md)

## How to set a job

Spring Batch helps you set a listener or an incrementer for a `Job`. The Kotlin DSL for Spring Batch Plus also helps you do so. For more information, see the following topic:

- [Job Configuration](./job-configuration.md)
