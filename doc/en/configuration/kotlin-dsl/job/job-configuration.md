# Job Configuration

- [Set a JobParameterIncrementer](#set-a-jobparameterincrementer)
- [Set a job listener](#set-a-job-listener)
  - [Set a listener using annotations](#set-a-listener-using-annotations)
  - [Set a listener using a JobExecutionListener object](#set-a-listener-using-a-jobexecutionlistener-object)
  - [Set a listener using the bean name of an annotated object](#set-a-listener-using-the-bean-name-of-an-annotated-object)
  - [Set a listener using the bean name of a JobExecutionListener](#set-a-listener-using-the-bean-name-of-a-jobexecutionlistener)
- [Set a ObservationRegistry](#set-a-observationregistry)
- [Set preventRestart](#set-preventrestart)
- [Set a JobParametersValidator](#set-a-jobparametersvalidator)

The functions that can be set with `JobBuilder` are also available with the Kotlin DSL. In this page, you will learn how to set a `Job` using the Kotlin DSL.

## Set a JobParameterIncrementer

The Kotlin DSL helps you set a `JobParameterIncrementer` using `JobBuilder`. You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                incrementer {
                    val nextValue = it?.getLong("param")?.plus(1L) ?: 0L
                    JobParametersBuilder(it ?: JobParameters())
                        .addLong("param", nextValue)
                        .toJobParameters()
                }
                // same as
                // incrementer(
                //     object : JobParametersIncrementer {
                //         override fun getNext(parameters: JobParameters?): JobParameters {
                //             val nextValue = parameters?.getLong("param")?.plus(1L) ?: 0L
                //             return JobParametersBuilder(parameters ?: JobParameters())
                //                 .addLong("param", nextValue)
                //                 .toJobParameters()
                //         }
                //     }
                // )
                step("testStep") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }
        }
}
```

## Set a job listener

The Kotlin DSL also helps you set a job listener using `JobBuilder`. To set a listener, you can either use annotations or pass a `JobExecutionListener` object. You can also name a bean instead of passing the object itself.

### Set a listener using annotations

You can add `@BeforeJob` and `@AfterJob` annotations to an object to set a listener.

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
    open fun testJob(): Job =
        batch {
            job("testJob") {
                listener(TestListener())
                step("testStep") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }
        }
}
```

### Set a listener using a JobExecutionListener object

You can directly pass a `JobExecutionListener` object as an argument to set a listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
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

### Set a listener using the bean name of an annotated object

You can add `@BeforeJob` and `@AfterJob` annotations to a `@Component` object and set it as a listener using its bean name.

```kotlin
@Component
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
```

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                listenerBean("testListener")
                step("testStep") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }
        }
}
```

### Set a listener using the bean name of a JobExecutionListener

You can set an object implementing `JobExecutionListener` as a listener using its bean name.

```kotlin
@Component
class TestListener : JobExecutionListener {
    override fun beforeJob(jobExecution: JobExecution) {
        println("before $jobExecution")
    }

    override fun afterJob(jobExecution: JobExecution) {
        println("after $jobExecution")
    }
}
```

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                listenerBean("testListener")
                step("testStep") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }
        }
}
```

## Set a ObservationRegistry

The Kotlin DSL helps you set a `ObservationRegistry` using `JobBuilder`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                observationRegistry(ObservationRegistry.create())
                step("testStep") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }
        }
}
```

## Set preventRestart

The Kotlin DSL helps you set preventRestart using `JobBuilder`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
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

## Set a JobParametersValidator

The Kotlin DSL helps you set a `JobParametersValidator` using `JobBuilder`. You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                validator {
                    val value = it?.getLong("param")
                    if (value == null || value < 0L) {
                        throw InvalidJobParametersException("param is null or less than 0")
                    }
                }
                // same as
                // validator(
                //     object : JobParametersValidator {
                //         override fun validate(parameters: JobParameters?) {
                //             val value = parameters?.getLong("param")
                //             if (value == null || value < 0L) {
                //                 throw InvalidJobParametersException("param is < 0")
                //             }
                //         }
                //     }
                // )
                step("testStep") {
                    tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                }
            }
        }
}
```
