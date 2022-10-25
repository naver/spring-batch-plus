# Job Step

- [Job을 변수로 넘기기](#job을-변수로-넘기기)
- [Bean 이름으로 Job을 가져오기](#bean-이름으로-job을-가져오기)
- [Job Step 설정하기](#job-step-설정하기)
  - [JobLauncher 설정하기](#joblauncher-설정하기)
  - [JobParametersExtractor 설정하기](#jobparametersextractor-설정하기)

Job Step이란 하나의 다른 `Job`으로 이루어진 `Step`입니다.

## Job을 변수로 넘기기

`Job`을 미리 정의해 두고 변수로 넘겨서 `Step`을 정의할 수 있습니다.

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
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

## Bean 이름으로 Job을 가져오기

`Job`을 미리 Bean 으로 정의해 두고 Bean 이름을 통해 사용할 `Job`을 가져올 수 있습니다.

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
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

## Job Step 설정하기

`Job`을 `Step`으로 넘기면서 `JobLauncher`, `JobParametersExtractor`를 정의할 수 있습니다.

### JobLauncher 설정하기

Job Step이 수행할 때 어떤 `JobLauncher`로 `Job`을 수행할지 설정할 수 있습니다.

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
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

Kotlin의 Trailing Lambda를 활용하여 간략하게 작성할 수 있습니다.

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
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

### JobParametersExtractor 설정하기

Job Step이 수행할 `Job`의 Parameter를 선택하여 지정할 수 있습니다.

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
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

Kotlin의 Trailing Lambda를 활용하여 간략하게 작성할 수 있습니다.

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
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```