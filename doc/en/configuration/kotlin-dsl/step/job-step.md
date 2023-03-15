# Job Step

- [Pass a job as a variable](#pass-a-job-as-a-variable)
- [Get a job using the bean name](#get-a-job-using-the-bean-name)
- [Set a job step](#set-a-job-step)
  - [Set a JobLauncher](#set-a-joblauncher)
  - [Set a JobParametersExtractor](#set-a-jobparametersextractor)

A job step consists of another single `Job`.

## Pass a job as a variable

You can pass a predefined `Job` as a variable to define a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                job(anotherJob())
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

## Get a job using the bean name

You can define a `Job` as a bean and use the bean name to get the `Job`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                jobBean("anotherJob")
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

## Set a job step

You can define a `JobLauncher` and `JobParametersExtractor` when you pass a `Job` as a `Step`.

### Set a JobLauncher

You can set which `JobLauncher` will run the `Job` when a job step is run.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val jobLauncher: JobLauncher,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                job(anotherJob()) {
                    launcher(
                        object : JobLauncher {
                            override fun run(job: Job, jobParameters: JobParameters): JobExecution {
                                println("launch anotherJob!!!")
                                return jobLauncher.run(job, jobParameters)
                            }
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val jobLauncher: JobLauncher,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                job(anotherJob()) {
                    launcher { job, jobParameters ->
                        println("launch anotherJob!!!")
                        jobLauncher.run(job, jobParameters)
                    }
                }
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

### Set a JobParametersExtractor

You can specify parameters of the `Job` that a job step will run.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                job(anotherJob()) {
                    object : JobParametersExtractor {
                        override fun getJobParameters(job: Job, stepExecution: StepExecution): JobParameters {
                            return JobParametersBuilder()
                                .addString("extra", "value")
                                .toJobParameters()
                        }
                    }
                }
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                job(anotherJob()) {
                    parametersExtractor { _, _ ->
                        JobParametersBuilder()
                            .addString("extra", "value")
                            .toJobParameters()
                    }
                }
            }
        }
    }

    @Bean
    open fun anotherJob() = batch {
        job("anotherJob") {
            step("anotherJobStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, ResourcelessTransactionManager())
            }
        }
    }
}
```
