# Kotlin DSL

- [BatchDsl](#batchdsl)
  - [Add a bean](#add-a-bean)
  - [How to use](#how-to-use)
- [Job, step, flow](#job-step-flow)

Spring Batch provides DSL for creating a `Job`, `Step`, and `Flow` object. Spring Batch lets you create a `Job`, `Step`, and `Flow` object using `JobBuilderFactory`, `StepBuilderFactory`, and `FlowBuilder`. Here is a Kotlin code example.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory
    ): Job {
        return jobBuilderFactory.get("testJob")
            .start(
                stepBuilderFactory.get("testStep1")
                    .tasklet { _, _ -> RepeatStatus.FINISHED }
                    .build()
            )
            .next(
                stepBuilderFactory.get("testStep2")
                    .tasklet { _, _ -> RepeatStatus.FINISHED }
                    .build()
            )
            .next(
                stepBuilderFactory.get("testStep3")
                    .tasklet { _, _ -> RepeatStatus.FINISHED }
                    .build()
            )
            .next(
                stepBuilderFactory.get("testStep4")
                    .tasklet { _, _ -> RepeatStatus.FINISHED }
                    .build()
            )
            .build()
    }
}
```

However, this has boilerplate code and is not Kotlin-like. To avoid such a problem, Spring Batch Plus helps you set a `Job`, `Step`, and `Flow` object using the Kotlin DSL. The following code example using the Kotlin DSL works the same.

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
            step("testStep4") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

Because the Kotlin DSL is designed to act as a wrapper for Spring Batch functions, not to change them, configurations not available in JAVA-based DSL are not applicable to Spring Batch in the Kotlin DSL as well. For more information about Spring Batch configurations, see [Spring Batch Docs](https://docs.spring.io/spring-batch/docs/current/reference/html/).

## BatchDsl

Spring Batch Plus provides `BatchDsl`. Invoke the `BatchDsl` class to use the Kotlin DSL. We recommend that you add a `BatchDsl` bean to use the class.

### Add a bean

Using `spring-boot-starter-batch-plus-kotlin` automatically adds a `BatchDsl` bean. If you need to explicitly add one, however, add it as shown in the following example.

```kotlin
@Configuration
open class BatchConfig {

    @Bean
    open fun batchDsl(
        beanFactory: BeanFactory,
        jobBuilderFactory: JobBuilderFactory,
        stepBuilderFactory: StepBuilderFactory
    ): BatchDsl {
        return BatchDsl(
            beanFactory,
            jobBuilderFactory,
            stepBuilderFactory
        )
    }
}
```

### How to use

You can bind a `BatchDsl` bean as an argument of a function.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```

If it is bound as a property of the class, you can use it without binding it as a function parameter.

```kotlin
@Configuration
class TestJobConfig(
    private val batch: BatchDsl
) {
    @Bean
    fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet { _, _ ->
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```

## Job, step, flow

You can use `BatchDsl` to create a `Job`, `Step`, and `Flow` object. For more information, see the following topics:

- [Job](./job/README.md)
- [Step](./step/README.md)
- [Flow](./flow/README.md)
