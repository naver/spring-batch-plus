# ClearRunIdIncrementer

- [기본 run id를 사용하기](#기본-run-id를-사용하기)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [run id를 지정하기](#run-id를-지정하기)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)

`ClearRunIdIncrementer`는 Spring Batch의 [RunIdIncrementer](http://github.com/spring-projects/spring-batch/blob/master/spring-batch-core/src/main/java/org/springframework/batch/core/launch/support/RunIdIncrementer.java)대신 사용할 수 있는 `JobParametersIncrementer` 입니다. `RunIdIncrementer`는 같은 JobParameter가 들어오더라도 run.id라는 추가적인 JobParameter를 제공하여 매번 다른 job으로 인식하게 만드는 class입니다. `RunIdIncrementer`는 JobParameters를 인자로 받아서 run.id값이 있는 경우 해당 값만 증가시키고 run.id값이 없는 경우 새 값을 추가해서 리턴합니다. `RunIdIncrementer`는 `JobOperator`의 startNextInstance method를 호출하면 내부적으로 사용됩니다.

```java
Job job = ... // RunIdIncrementer를 사용하는 job
JobExecution jobExecution = jobOperator.startNextInstance(job); // RunIdIncrementer를 내부적으로 사용
```

`JobOperator`의 startNextInstance를 호출 하면 Metadata를 조회하여 해당 job의 마지막 JobExecution에서 JobParameters를 추출하여 RunIdIncrementer에 인자로 넘깁니다. `RunIdIncrementer`는 인자로 받은 JobParameters에서 run.id에 해당하는 값만 변경 후 리턴합니다. 하지만 `RunIdIncrementer`에서 리턴되는 값에는 run.id 뿐만 아니라 해당 job의 마지막 JobExecution에서 사용된 JobParameter를 모두 포함합니다. 이는 다음과 같은 상황에서 문제를 일으킬 수 있습니다.

예를 들어 다음과 같이 두개의 JobParameter를 직접 넘겨 실행하던 Job이 있다고 하겠습니다.

```java
Step testStep = new StepBuilder("testStep", jobRepository)
    .tasklet(
        (contribution, chunkContext) -> RepeatStatus.FINISHED,
        transactionManager
    )
    .build();

Job jobBeforeIncrementer = new JobBuilder("testJob", jobRepository)
    .start(testStep)
    .build();

JobParameters jobParameters = new JobParametersBuilder()
    .addString("stringValue", "1")
    .addString("longValue", "10")
    .toJobParameters();
jobOperator.start(jobBeforeIncrementer, jobParameters);
```

이후 job에 `RunIdIncrementer`를 추가하면 `JobOperator`는 JobParameter를 직접 넘겨도 이를 무시하고 `startNextInstance`로 기동합니다. `RunIdIncrementer`는 직전 JobExecution의 JobParameter를 모두 물려받고 run.id만 증가시키므로, incrementer를 추가하기 전에 쓰던 stringValue와 longValue가 계속 따라옵니다.

```java
Job jobWithIncrementer = new JobBuilder("testJob", jobRepository)
    .incrementer(new RunIdIncrementer())
    .start(testStep)
    .build();

JobExecution jobExecution = jobOperator.startNextInstance(jobWithIncrementer);

// COMPLETED, jobParameters: {run.id=1, stringValue=1, longValue=10}
System.out.printf("%s, jobParameters: %s%n",
    jobExecution.getExitStatus().getExitCode(),
    jobExecution.getJobParameters());
```

`ClearRunIdIncrementer`는 이를 해결하여 이전 JobExecution의 인자를 신규 Job을 수행할 때 사용하지 않게 처리합니다. run.id만 남고 stringValue와 longValue는 넘어오지 않는 것을 확인할 수 있습니다.

```java
Job jobWithIncrementer = new JobBuilder("testJob", jobRepository)
    .incrementer(ClearRunIdIncrementer.create())
    .start(testStep)
    .build();

JobExecution jobExecution = jobOperator.startNextInstance(jobWithIncrementer);

// COMPLETED, jobParameters: {run.id=1}
System.out.printf("%s, jobParameters: %s%n",
    jobExecution.getExitStatus().getExitCode(),
    jobExecution.getJobParameters());
```

## 기본 run id를 사용하기

`ClearRunIdIncrementer`에서는 생성할 때 별도의 인자를 주지 않으면 `run.id`를 증가 대상 JobParameter의 id로 사용합니다.

### Java

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .incrementer(ClearRunIdIncrementer.create())
            .start(
                new StepBuilder("testStep", jobRepository)
                    .tasklet(
                        (contribution, chunkContext) -> RepeatStatus.FINISHED,
                        transactionManager
                    )
                    .build()
            )
            .build();
    }
}
```

### Kotlin

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            incrementer(ClearRunIdIncrementer.create())
            step("testStep") {
                tasklet(
                    { _, _ -> RepeatStatus.FINISHED },
                    transactionManager,
                )
            }
        }
    }
}
```

## run id를 지정하기

`ClearRunIdIncrementer`를 생성할 때 별도의 인자를 주면 해당 값을 증가 대상 JobParameter의 id로 사용합니다.

### Java

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .incrementer(ClearRunIdIncrementer.create("testId"))
            .start(
                new StepBuilder("testStep", jobRepository)
                    .tasklet(
                        (contribution, chunkContext) -> RepeatStatus.FINISHED,
                        transactionManager
                    )
                    .build()
            )
            .build();
    }
}
```

### Kotlin

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            incrementer(ClearRunIdIncrementer.create("testId"))
            step("testStep") {
                tasklet(
                    { _, _ -> RepeatStatus.FINISHED },
                    transactionManager,
                )
            }
        }
    }
}
```