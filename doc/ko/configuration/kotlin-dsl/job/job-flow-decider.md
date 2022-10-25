# Job Flow - JobExecutionDecider 사용

- [JobExecutionDecider 사용하기](#jobexecutiondecider-사용하기)
  - [JobExecutionDecider를 변수로 넘기기](#jobexecutiondecider를-변수로-넘기기)
  - [Bean 이름으로 JobExecutionDecider를 가져오기](#bean-이름으로-jobexecutiondecider를-가져오기)

Spring Batch의 `Flow`는 이전 `Step`의 결과에 따라 분기해서 수행할 수 있습니다. 이를 직접 제어하기 위해 `JobExecutionDecider`를 설정할 수 있는데 Kotlin DSL도 동일한 설정을 할 수 있습니다.

## JobExecutionDecider 사용하기

`Job`을 설정할 때 `JobExecutionDecider`를 설정하면 `Flow`의 상태를 직접 제어할 수 있습니다. `JobExecutionDecider`의 결과에 따라 어떤 동작을 할지 설정하는 자세한 방법은 [Job Flow - Transition 하는 방법](./job-flow-transition.md)을 참고하기 바랍니다.

### JobExecutionDecider를 변수로 넘기기

`Job`을 정의할 때 미리 정의한 `JobExecutionDecider`을 변수로 넘겨서 `Flow`를 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            decider(testDecider()) {
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
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus.FAILED
    }
}
```

### Bean 이름으로 JobExecutionDecider를 가져오기

`Job`을 정의할 때 Bean 이름으로 `JobExecutionDecider`를 가져올 수도 있습니다.

```kotlin
@Component
class TestDecider : JobExecutionDecider {
    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        return FlowExecutionStatus.FAILED
    }
}

@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            deciderBean("testDecider") {
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
}
```