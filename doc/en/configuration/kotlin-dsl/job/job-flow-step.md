# Job Flow - Using Steps

- [Sequential execution of steps](#sequential-execution-of-steps)
  - [Pass a step as a variable](#pass-a-step-as-a-variable)
  - [Initialize a step when defining a job](#initialize-a-step-when-defining-a-job)
  - [Get a step using the bean name](#get-a-step-using-the-bean-name)
- [Conditional execution of steps](#conditional-execution-of-steps)
  - [Pass a step as a variable](#pass-a-step-as-a-variable-1)
  - [Initialize a step when defining a job](#initialize-a-step-when-defining-a-job-1)
  - [Get a step using the bean name](#get-a-step-using-the-bean-name-1)

In Spring Batch, a `Job` consists of one or more `Steps`, which can be run sequentially or conditionally based on the result of the previous `Step`. However, using `JobBuilderFactory` and `StepBuilderFactory` in Spring Batch has a problem. Here is an example of setting a job flow using `JobBuilderFactory` and `StepBuilderFactory`.

```kotlin
@Configuration
open class TestJobConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    @Bean
    open fun testJob(): Job {
        return jobBuilderFactory.get("testJob")
            .start(testStep1()).on("COMPLETED").to(successStep())
            .from(testStep1()).on("FAILED").to(failureStep())
            .from(testStep1()).on("*").stop()
            .end()
            .build()
    }

    @Bean
    open fun testStep1(): Step {
        return stepBuilderFactory.get("testStep1")
            .tasklet { _, _ ->
                throw IllegalStateException("step failed")
            }
            .build()
    }

    @Bean
    open fun successStep(): Step {
        return stepBuilderFactory.get("successStep")
            .tasklet { _, _ -> RepeatStatus.FINISHED }
            .build()
    }

    @Bean
    open fun failureStep(): Step {
        return stepBuilderFactory.get("failureStep")
            .tasklet { _, _ -> RepeatStatus.FINISHED }
            .build()
    }
}
```

This example has several problems. First, you need to create a method for each `Step` and use it to set a job flow. Second, indentation or line breaks in `.from().on(..)` can affect readability. For example, the following code block works as expected but has poor readability. This can be found and fixed during code review, but cannot be by auto formatting of IDE.

```kotlin
@Bean
open fun testJob(): Job {
    return jobBuilderFactory.get("testJob")
        .start(testStep1())
        .on("COMPLETED")
        .to(successStep())
        .from(testStep1())
        .on("FAILED")
        .to(failureStep())
        .from(testStep1())
        .on("*")
        .stop()
        .end()
        .build()
}
```

With the Kotlin DSL, however, you can declaratively set a job flow, avoiding such a problem. Here is an example using the Kotlin DSL of Spring Batch Plus.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(testStep1()) {
                on("COMPLETED") {
                    step(testStep1())
                }
                on("FAILED") {
                    step("failureStep") {
                        tasklet { _, _ -> RepeatStatus.FINISHED }
                    }
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    open fun testStep1(): Step = batch {
        step("testStep1") {
            tasklet { _, _ ->
                throw IllegalStateException("step failed")
            }
        }
    }

    @Bean
    open fun successStep(): Step = batch {
        step("successStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

The Kotlin DSL does not require boilerplate code such as `.build()`. As you can see in the code example, you can use a method like testStep1 and successStep, or define a `Step` in a job flow like failureStep.

## Sequential execution of steps 

The Kotlin DSL helps you run `Steps` sequentially. You can add a `Step` using a method and pass it as a variable, initialize it or get it using the bean name when you define a `Job`.

### Pass a step as a variable

You can pass a predefined `Step` as a variable to define a `Job`. You can declare a step as a method like testStep1 and testStep2, or as a variable to pass like testStep3.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            val testStep3 = batch {
                step("testStep3") {
                    tasklet { _, _ -> RepeatStatus.FINISHED }
                }
            }

            step(testStep1())
            step(testStep2())
            step(testStep3)
        }
    }

    @Bean
    open fun testStep1(): Step = batch {
        step("testStep1") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }

    @Bean
    open fun testStep2(): Step = batch {
        step("testStep2") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

### Initialize a step when defining a job

You can initialize a `Step` when you define a `Job`.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep1") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("testStep2") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
            step("testStep3") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

### Get a step using the bean name

You can also get a `Step` using the bean name when you define a `Job`.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            stepBean("testStep1")
            stepBean("testStep2")
            stepBean("testStep3")
        }
    }

    @Bean
    open fun testStep1(
        batch: BatchDsl
    ): Step = batch {
        step("testStep1") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }

    @Bean
    open fun testStep2(
        batch: BatchDsl
    ): Step = batch {
        step("testStep2") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }

    @Bean
    open fun testStep3(
        batch: BatchDsl
    ): Step = batch {
        step("testStep3") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
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
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step(testStep()) {
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
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }
}
```

### Initialize a step when defining a job

You can initialize a `Step` when you define a `Job`. You can use a trailing lambda to define a `Step` of the `Job`.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step(
                "testStep",
                {
                    tasklet { _, _ ->
                        throw IllegalStateException("testStep failed")
                    }
                }
            ) {
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

### Get a step using the bean name

You can also get a `Step` using the bean name when you define a `Job`. You can use a trailing lambda to define a `Step` of the `Job`.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            stepBean("testStep") {
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
    open fun testStep(
        batch: BatchDsl
    ): Step = batch {
        step("testStep") {
            tasklet { _, _ ->
                throw IllegalStateException("testStep failed")
            }
        }
    }
}
```

