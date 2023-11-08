# Job

- [Job 생성 방법](#job-생성-방법)
- [Job Flow 설정](#job-flow-설정)
- [Job 설정 방법](#job-설정-방법)

Spring Batch 는 `Job` 단위로 수행됩니다. 한 `Job`은 한개 또는 여러개의 `Step`으로 구성되어 있습니다. `Step`은 순차적으로 수행될 수도 있지만 특정 조건에 따라 분기해서 수행될 수도 있습니다. Spring Batch Plus에서는 이 `Flow` 기능을 보다 편리하게 선언적으로 작성할 수 있는 기능을 제공합니다. 

## Job 생성 방법

`BatchDsl`을 활용하여 `Job`을 생성할 수 있습니다.

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
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            step("testStep2") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## Job Flow 설정

`Step`들은 순차적으로 수행할 수 있고 조건에 따라 분기하여 수행하거나, 여러 Thread에서 병렬로 수행할 수도 있습니다. `Job`의 `Flow`를 작성할 때 `Step` 뿐만 아니라 다른 `Flow`, `JobExecutionDecider`도 사용할 수 있습니다. 자세한 설정 방법은 다음을 참고하시길 바랍니다.

- [Step 사용](./job-flow-step.md)
- [다른 Flow 사용](./job-flow-flow.md)
- [JobExecutionDecider 사용](./job-flow-decider.md)
- [Flow에서 Transition 하는 방법](./job-flow-transition.md)
- [Flow 병렬처리](./job-split.md)

## Job 설정 방법

Spring Batch에서는 `Job`에 대한 listener, incrementer 등을 설정할 수 있는 기능을 제공합니다. Spring Batch Plus에서도 동일하게 Kotlin DSL로 해당 설정들을 할 수 있는 기능을 제공합니다. 자세한 설정 방법은 다음을 참고하시길 바랍니다.

- [Job Configuration](./job-configuration.md)