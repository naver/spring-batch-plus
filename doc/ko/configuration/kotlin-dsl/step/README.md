# Step

- [Step 생성 방법](#step-생성-방법)
- [Step 종류별 생성 방법](#step-종류별-생성-방법)
- [Step 설정 방법](#step-설정-방법)

Spring Batch의 `Job`은 `Step` 단위로 수행됩니다. Spring Batch에는 5가지 종류의 `Step`이 있습니다. Spring Batch Plus는 Kotlin DSL을 활용해서 5가지 `Step`을 만드는 방법을 제공합니다.

## Step 생성 방법

`Step`을 생성하는 방법은 크게 두 가지가 있습니다. 첫 번째로 `BatchDsl`에서 step을 호출하여 생성하는 방법입니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testStep() = batch {
        step("testStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

`Job`, `Flow`의 구성요소로 `Step`이 있으므로 `Job`, `Flow`를 생성하는 과정에서 `Step`을 생성 가능합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testFlow() = batch {
        flow("testFlow") {
            // within flow
            step("flowStep1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("flowStep2") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            // within job
            step("testStep1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("testStep2") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            flow(testFlow())
        }
    }
}
```

`Step`을 생성하는 두 가지 방법은 5가지 `Step`에 모두 적용됩니다.

## Step 종류별 생성 방법

Spring Batch에서는 5가지 종류의 `Step`이 있습니다. `TaskletStep`은 단일 `Tasklet`으로 구성된 `Step` 입니다. `SimpleStep`은 `ItemReader`, `ItemProcessor`, `ItemWriter`로 구성된 `Step`입니다. `PartitionStep`은 한 `Step`을 여러 task로 나누어서 수행할 수 있는 `Step`입니다. `JobStep`, `FlowStep`은 각각 단일 `Job`, 단일 `Flow`로 구성되어 있는 `Step`입니다. 각 `Step`에 대한 상세한 생성 방법은 다음을 참고 바랍니다.

- [TaskletStep](./tasklet-step.md)
- [SimpleStep](./simple-step.md)
- [PartitionStep](./partition-step.md)
- [JobStep](./job-step.md)
- [FlowStep](./flow-step.md)

## Step 설정 방법

Spring Batch에서는 `Step`에 대한 listener, transactionManager 등을 설정할 수 있는 기능을 제공합니다. Spring Batch Plus에서도 동일하게 Kotlin DSL로 해당 설정들을 할 수 있는 기능을 제공합니다. 자세한 정보는 다음을 참고하시길 바랍니다.

- [Step Configuration](./step-configuration.md)