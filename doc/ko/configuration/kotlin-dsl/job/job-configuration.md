# Job Configuration

- [JobParameterIncrementer 설정](#jobparameterincrementer-설정)
- [Job Listener 설정](#job-listener-설정)
  - [Annotation을 사용해서 Listener 설정하기](#annotation을-사용해서-listener-설정하기)
  - [JobExecutionListener 객체를 사용하여 Listener 설정하기](#jobexecutionlistener-객체를-사용하여-listener-설정하기)
- [MeterRegistry 설정하기](#meterregistry-설정하기)
- [BatchJobObservationConvention 설정하기](#batchjobobservationconvention-설정하기)
- [ObservationRegistry 설정하기](#observationregistry-설정하기)
- [PreventRestart 설정하기](#preventrestart-설정하기)
- [JobParametersValidator 설정하기](#jobparametersvalidator-설정하기)

Kotlin DSL은 `JobBuilder`에서 설정할 수 있는 기능을 모두 제공합니다. 이 문서에서는 Kotlin DSL을 활용해서 `Job` 을 설정하는 방법에 대해서 다룹니다.

## JobParameterIncrementer 설정

Kotlin DSL은 `JobBuilder`를 사용해서 `JobParameterIncrementer`를 설정하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            incrementer(
                object : JobParametersIncrementer {
                    override fun getNext(parameters: JobParameters?): JobParameters {
                        val nextValue = parameters?.getLong("param")?.plus(1L) ?: 0L
                        return JobParametersBuilder(parameters ?: JobParameters())
                            .addLong("param", nextValue)
                            .toJobParameters()
                    }
                }
            )
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

Kotlin의 Trailing Lambda를 활용하면 보다 간략하게 작성할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            incrementer {
                val nextValue = it?.getLong("param")?.plus(1L) ?: 0L
                JobParametersBuilder(it ?: JobParameters())
                    .addLong("param", nextValue)
                    .toJobParameters()
            }
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## Job Listener 설정

Kotlin DSL은 `JobBuilder`를 사용해서 Job에 대한 Listener를 설정하는 방법을 제공합니다. Listener 설정에는 Annotation을 사용하는 방법과 `JobExecutionListener` 객체를 넘기는 두 가지 방법이 있습니다.

### Annotation을 사용해서 Listener 설정하기

임의의 객체에 `@BeforeJob`, `@AfterJob` Annotation을 붙여서 Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    class TestListener {
        @BeforeJob
        fun beforeJob() {
            println("beforeJob")
        }

        @AfterJob
        fun afterJob() {
            println("afterJob")
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            listener(TestListener())
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

### JobExecutionListener 객체를 사용하여 Listener 설정하기

`JobExecutionListener` 객체를 직접 인자로 넘겨서 Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            listener(
                object : JobExecutionListener {
                    override fun beforeJob(jobExecution: JobExecution) {
                        println("before $jobExecution")
                    }

                    override fun afterJob(jobExecution: JobExecution) {
                        println("after $jobExecution")
                    }
                },
            )
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## MeterRegistry 설정하기

Kotlin DSL은 `JobBuilder`를 사용해서 `MeterRegistry` 설정을 하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            meterRegistry(SimpleMeterRegistry())
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}

```

## BatchJobObservationConvention 설정하기

Kotlin DSL은 `JobBuilder`를 사용해서 `BatchJobObservationConvention` 설정을 하는 방법을 제공합니다.

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

## ObservationRegistry 설정하기

Kotlin DSL은 `JobBuilder`를 사용해서 `ObservationRegistry` 설정을 하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            observationRegistry(ObservationRegistry.create())
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## PreventRestart 설정하기

Kotlin DSL은 `JobBuilder`를 사용해서 preventRestart 설정을 하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        var isFirst = true

        job("testJob") {
            preventRestart()
            step("testStep") {
                tasklet(
                    { _, _ ->
                        if (isFirst) {
                            isFirst = false
                            throw RuntimeException("First try should be failed")
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

## JobParametersValidator 설정하기

Kotlin DSL은 `JobBuilder`를 사용해서 `JobParametersValidator` 설정을 하는 방법을 제공합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            validator(
                object : JobParametersValidator {
                    override fun validate(parameters: JobParameters?) {
                        val value = parameters?.getLong("param")
                        if (value == null || value < 0L) {
                            throw JobParametersInvalidException("param is < 0")
                        }
                    }
                }
            )
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

Kotlin의 Trailing Lambda를 활용하면 보다 간략하게 작성할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            validator {
                val value = it?.getLong("param")
                if (value == null || value < 0L) {
                    throw JobParametersInvalidException("param is null or less than 0")
                }
            }
            step("testStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```
