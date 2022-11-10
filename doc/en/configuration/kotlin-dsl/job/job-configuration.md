# Job Configuration

- [Set a JobParameterIncrementer](#set-a-jobparameterincrementer)
- [Set a job listener](#set-a-job-listener)
  - [Set a listener using annotations](#set-a-listener-using-annotations)
  - [Set a listener using a JobExecutionListener object](#set-a-listener-using-a-jobexecutionlistener-object)
- [Set preventRestart](#set-preventrestart)
- [Set a repository](#set-a-repository)
- [Set a JobParametersValidator](#set-a-jobparametersvalidator)

The functions that can be set with `JobBuilder` are also available with the Kotlin DSL. In this page, you will learn how to set a `Job` using the Kotlin DSL.

## Set a JobParameterIncrementer

The Kotlin DSL helps you set a `JobParameterIncrementer` using `JobBuilder`.

```kotlin
@Bean
open fun testJob(
    batch: BatchDsl
): Job = batch {
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
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Bean
open fun testJob(
    batch: BatchDsl
): Job = batch {
    job("testJob") {
        incrementer {
            val nextValue = it?.getLong("param")?.plus(1L) ?: 0L
            JobParametersBuilder(it ?: JobParameters())
                .addLong("param", nextValue)
                .toJobParameters()
        }
        step("testStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

## Set a job listener

The Kotlin DSL also helps you set a job listener using `JobBuilder`. To set a listener, you can either use annotations or pass a `JobExecutionListener` object.

### Set a listener using annotations

You can add `A@BeforeJob` and `@AfterJob` annotations to an object to set a listener.

```kotlin
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
open fun testJob(
    batch: BatchDsl
): Job = batch {
    job("testJob") {
        listener(TestListener())
        step("testStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

### Set a listener using a JobExecutionListener object

You can directly pass a `JobExecutionListener` object as an argument to set a listener.

```kotlin
open fun testJob(
    batch: BatchDsl
): Job = batch {
    job("testJob") {
        listener(
            object : JobExecutionListener {
                override fun beforeJob(jobExecution: JobExecution) {
                    println("before $jobExecution")
                }

                override fun afterJob(jobExecution: JobExecution) {
                    println("after $jobExecution")
                }
            }
        )
        step("testStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

## Set preventRestart

The Kotlin DSL helps you set preventRestart using `JobBuilder`.

```kotlin
@Bean
open fun testJob(
    batch: BatchDsl
): Job = batch {
    var isFirst = true

    job("testJob") {
        preventRestart()
        step("testStep") {
            tasklet { _, _ ->
                if (isFirst) {
                    isFirst = false
                    throw RuntimeException("First try should be failed")
                }
                RepeatStatus.FINISHED
            }
        }
    }
}
```

## Set a repository

The Kotlin DSL helps you set a `JobRepository` using `JobBuilder`.

```kotlin
@Bean
open fun testJob(
    batch: BatchDsl,
    jobRepository: JobRepository
): Job = batch {
    job("testJob") {
        repository(
            object : JobRepository by jobRepository {
                override fun update(jobExecution: JobExecution) {
                    println("update jobExecution to $jobExecution")
                    jobRepository.update(jobExecution)
                }
            }
        )
        step("testStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

## Set a JobParametersValidator

The Kotlin DSL helps you set a `JobParametersValidator` using `JobBuilder`.

```kotlin
@Bean
open fun testJob(
    batch: BatchDsl
): Job = batch {
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
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Bean
open fun testJob(
    batch: BatchDsl
): Job = batch {
    job("testJob") {
        validator {
            val value = it?.getLong("param")
            if (value == null || value < 0L) {
                throw JobParametersInvalidException("param is null or less than 0")
            }
        }
        step("testStep") {
            tasklet { _, _ -> RepeatStatus.FINISHED }
        }
    }
}
```
