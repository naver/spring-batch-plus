# Step Configuration

- [Job Repository 설정](#job-repository-설정)
- [BatchStepObservationConvention 설정](#batchstepobservationconvention-설정)
- [ObservationRegistry 설정](#observationregistry-설정)
- [MeterRegistry 설정](#meterregistry-설정)
- [StartLimit 설정](#startlimit-설정)
- [Step Listener 설정](#step-listener-설정)
  - [Annotation을 사용하여 Listener 설정하기](#annotation을-사용하여-listener-설정하기)
  - [StepExecutionListener 객체를 사용하여 Listener 설정하기](#stepexecutionlistener-객체를-사용하여-listener-설정하기)
- [allowStartIfComplete 설정](#allowstartifcomplete-설정)

Kotlin DSL에서는 `StepBuilder`에서 설정할 수 있는 기능을 모두 제공합니다. 이 문서에서는 Kotlin DSL을 활용해서 `Step` 관련 설정들을 하는 방법에 대해서 다룹니다.

## Job Repository 설정

Kotlin DSL은 `StepBuilder`를 사용하여 `JobRepository`를 설정하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
    private val jobRepository: JobRepository,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                repository(
                    object : JobRepository by jobRepository {
                        override fun update(stepExecution: StepExecution) {
                            println("update stepExecution to $stepExecution")
                            jobRepository.update(stepExecution)
                        }
                    },
                )
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## BatchStepObservationConvention 설정

Kotlin DSL은 `StepBuilder`를 사용하여 `BatchStepObservationConvention`을 설정하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            observationConvention(DefaultBatchJobObservationConvention())
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## ObservationRegistry 설정

Kotlin DSL은 `StepBuilder`를 사용하여 `ObservationRegistry`을 설정하는 방법을 제공합니다.

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
                observationRegistry(ObservationRegistry.create())
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## MeterRegistry 설정

Kotlin DSL은 `StepBuilder`를 사용하여 `MeterRegistry`을 설정하는 방법을 제공합니다.

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
                meterRegistry(SimpleMeterRegistry())
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## StartLimit 설정

Kotlin DSL은 `StepBuilder`를 사용하여 startLimit을 설정하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    private var count = 0

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                startLimit(2)
                tasklet(
                    { _, _ ->
                        if (count < 2) {
                            throw IllegalStateException("count is less than 2 (count: ${count++})")
                        }
                        RepeatStatus.FINISHED
                    },
                    transactionManager,
                )
            }
        }
    }
}
```

## Step Listener 설정

Kotlin DSL은 `StepBuilder`를 사용하여 `Step`에 대한 Listener를 설정하는 방법을 제공합니다. Listener를 설정하려면 Annotation을 이용하거나 `StepExecutionListener`를 이용할 수 있니다.

### Annotation을 사용하여 Listener 설정하기

임의의 객체에 `@BeforeStep`, `@AfterStep` Annotation을 붙여서 Listener를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    class TestListener {
        @BeforeStep
        fun beforeStep() {
            println("beforeStep")
        }

        @AfterStep
        fun afterStep() {
            println("afterStep")
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                listener(TestListener())
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

### StepExecutionListener 객체를 사용하여 Listener 설정하기

`StepExecutionListener` 객체를 직접 인자로 넘겨서 Listener 설정할 수 있습니다.

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
                listener(
                    object : StepExecutionListener {
                        override fun beforeStep(stepExecution: StepExecution) {
                            println("beforeStep")
                        }

                        override fun afterStep(stepExecution: StepExecution): ExitStatus? {
                            println("afterStep")
                            return null
                        }
                    },
                )
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## allowStartIfComplete 설정

Kotlin DSL은 `StepBuilder`를 사용하여 allowStartIfComplete를 설정하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("alwaysRunStep") {
                allowStartIfComplete(true)
                tasklet(
                    { _, _ ->
                        println("always run")
                        RepeatStatus.FINISHED
                    },
                    transactionManager,
                )
            }
            step("alwaysFailsStep") {
                tasklet(
                    { _, _ ->
                        throw IllegalStateException("always failed")
                    },
                    transactionManager,
                )
            }
        }
    }
}
```
