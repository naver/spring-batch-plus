# Kotlin DSL

- [BatchDsl](#batchdsl)
  - [Add a bean](#add-a-bean)
  - [How to use](#how-to-use)
- [Job, step, flow](#job-step-flow)

Spring Batch provides DSL for creating a `Job`, `Step`, and `Flow` object. Spring Batch lets you create a `Job`, `Step`, and `Flow` object using `JobBuilder`, `StepBuilder`, and `FlowBuilder`. Here is a Kotlin code example.

```kotlin
open class TestJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job {
        return JobBuilder("testJob", jobRepository)
            .start(
                StepBuilder("testStep1", jobRepository)
                    .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    .build(),
            )
            .next(testStep2())
            .on("COMPLETED").to(testStep3())
            .from(testStep2())
            .on("FAILED").to(testStep4())
            .end()
            .build()
    }

    @Bean
    open fun testStep2(): Step {
        return StepBuilder("testStep2", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun testStep3(): Step {
        return StepBuilder("testStep3", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }

    @Bean
    open fun testStep4(): Step {
        return StepBuilder("testStep4", jobRepository)
            .tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            .build()
    }
}
```

However, this has boilerplate code and is not Kotlin-like. To avoid such a problem, Spring Batch Plus helps you set a `Job`, `Step`, and `Flow` object using the Kotlin DSL. The following code example using the Kotlin DSL works the same.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep1") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
            step(testStep2()) {
                on("COMPLETED") {
                    step(testStep3())
                }
                on("FAILED") {
                    step(testStep4())
                }
            }
        }
    }

    @Bean
    open fun testStep2(): Step = batch {
        step("testStep2") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }

    @Bean
    open fun testStep3(): Step = batch {
        step("testStep3") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }

    @Bean
    open fun testStep4(): Step = batch {
        step("testStep4") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```

Because the Kotlin DSL is designed to act as a wrapper for Spring Batch functions, not to change them, configurations not available in JAVA-based DSL are not applicable to Spring Batch in the Kotlin DSL as well. For more information about Spring Batch configurations, see [Spring Batch Docs](https://spring.io/projects/spring-batch).

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
        jobRepository: JobRepository
    ): BatchDsl {
        return BatchDsl(
            beanFactory,
            jobRepository
        )
    }
}
```

### How to use

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

Also you can bind a `BatchDsl` bean as an argument of a function.

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

## Job, step, flow

You can use `BatchDsl` to create a `Job`, `Step`, and `Flow` object. For more information, see the following topics:

- [Job](./job/README.md)
- [Step](./step/README.md)
- [Flow](./flow/README.md)
