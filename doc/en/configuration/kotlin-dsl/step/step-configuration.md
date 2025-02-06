# Step Configuration

- [Set a BatchStepObservationConvention](#set-a-batchstepobservationconvention)
- [Set a ObservationRegistry](#set-a-observationregistry)
- [Set a MeterRegistry](#set-a-meterregistry)
- [Set startLimit](#set-startlimit)
- [Set a step listener](#set-a-step-listener)
  - [Set a listener using annotations](#set-a-listener-using-annotations)
  - [Set a listener using a StepExecutionListener object](#set-a-listener-using-a-stepexecutionlistener-object)
- [Set allowStartIfComplete](#set-allowstartifcomplete)

The functions that can be set with `StepBuilder` are also available with the Kotlin DSL. In this page, you will learn how to configure a `Step` using the Kotlin DSL.

## Set a BatchStepObservationConvention

The Kotlin DSL helps you set a `BatchStepObservationConvention` using `StepBuilder`.

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
                observationConvention(DefaultBatchStepObservationConvention())
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## Set a ObservationRegistry

The Kotlin DSL helps you set a `ObservationRegistry` using `StepBuilder`.

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

## Set a MeterRegistry

The Kotlin DSL helps you set a `MeterRegistry` using `StepBuilder`.

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

## Set startLimit

The Kotlin DSL helps you set startLimit using `StepBuilder`.

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

## Set a step listener

The Kotlin DSL helps you set a `Step` listener using `StepBuilder`. You can use annotations or a `StepExecutionListener` to set a listener.

### Set a listener using annotations

You can add `@BeforeStep` and `@AfterStep` annotations to an object to set a listener.

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

### Set a listener using a StepExecutionListener object

You can pass a `StepExecutionListener` object as an argument to set a listener.

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

## Set allowStartIfComplete

The Kotlin DSL helps you set allowStartIfComplete using `StepBuilder`.

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
