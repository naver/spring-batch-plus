# Job Flow - Using a JobExecutionDecider

- [Use a JobExecutionDecider](#use-a-jobexecutiondecider)
  - [Pass a JobExecutionDecider as a variable](#pass-a-jobexecutiondecider-as-a-variable)
  - [Get a JobExecutionDecider using the bean name](#get-a-jobexecutiondecider-using-the-bean-name)

In Spring Batch, a `Flow` can be run conditionally based on the result of the previous `Step`. For this, Spring Batch supports a `JobExecutionDecider`, which is also available with the Kotlin DSL.

## Use a JobExecutionDecider

You can set a `JobExecutionDecider` when setting a `Job`, to directly control the status of a `Flow`. For more information about how to decide what to run based on the result of a `JobExecutionDecider`, see [Job Flow - Transition from a Flow](./job-flow-transition.md).

### Pass a JobExecutionDecider as a variable

You can pass a predefined `JobExecutionDecider` as a variable to define a `Flow`.

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
    open fun testDecider(): JobExecutionDecider = JobExecutionDecider { _, _ ->
        FlowExecutionStatus.FAILED
    }
}
```

### Get a JobExecutionDecider using the bean name

You can get a `JobExecutionDecider` using the bean name when defining a `Job`.

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
