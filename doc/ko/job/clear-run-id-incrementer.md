# ClearRunIdIncrementer

- [기본 run id를 사용하기](#기본-run-id를-사용하기)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [run id를 지정하기](#run-id를-지정하기)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)

`ClearRunIdIncrementer`는 Spring Batch의 [RunIdIncrementer](http://github.com/spring-projects/spring-batch/blob/master/spring-batch-core/src/main/java/org/springframework/batch/core/launch/support/RunIdIncrementer.java)대신 사용할 수 있는 `JobParametersIncrementer` 입니다. `RunIdIncrementer`는 같은 JobParameter가 들어오더라도 run.id라는 추가적인 JobParameter를 제공하여 매번 다른 job으로 인식하게 만드는 class입니다. `RunIdIncrementer`는 JobParameters를 인자로 받아서 run.id값이 있는 경우 해당 값만 증가시키고 run.id값이 없는 경우 새 값을 추가해서 리턴합니다. `RunIdIncrementer`는 `JobParametersBuilder`의 getNextJobParameters method를 호출하면 내부적으로 사용됩니다.

```java
Job job = ...
JobParameters firstJobParameter = new JobParametersBuilder(jobExplorer)
    .addString("stringValue", "1")
    .addString("longValue", "10")
    .getNextJobParameters(job) // RunIdIncrementer를 내부적으로 사용
    .toJobParameters();
```

`JobParametersBuilder`의 getNextJobParameters를 호출 하면 Metadata를 조회하여 해당 job의 마지막 JobExecution에서 JobParameters를 추출하여 RunIdIncrementer에 인자로 넘깁니다. `RunIdIncrementer`는 인자로 받은 JobParameters에서 run.id에 해당하는 값만 변경 후 리턴합니다. 하지만 `RunIdIncrementer`에서 리턴되는 값에는 run.id 뿐만 아니라 해당 job의 마지막 JobExecution에서 사용된 JobParameter를 모두 포함합니다. 이는 다음과 같은 상황에서 문제를 일으킬 수 있습니다.

예를 들어 다음과 같이 두개의 JobParameter를 사용하는 Job을 정의한 경우를 생각해볼수 있습니다.

```java
@Bean
public Job testJob(
    JobRepository jobRepository
) {
    return new JobBuilder("testJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(
            new StepBuilder("testStep", jobRepository)
                .tasklet(
                    testTasklet(null, null),
                    new ResourcelessTransactionManager()
                )
                .build()
        )
        .build();
}

@StepScope
@Bean
public Tasklet testTasklet(
    @Value("#{jobParameters['longValue']}") Long longValue,
    @Value("#{jobParameters['stringValue']}") String stringValue
) {
    return (contribution, chunkContext) -> {
        Long result;
        if (stringValue != null) {
            result = longValue + Long.parseLong(stringValue);
        } else {
            result = 999L;
        }

        ExecutionContext jobExecutionContext = contribution.getStepExecution()
            .getJobExecution()
            .getExecutionContext();
        jobExecutionContext.putLong("result", result);
        return RepeatStatus.FINISHED;
    };
}
```

이 경우 첫 번째 실행에서 stringValue를 주었지만 두 번째 실행에서 stringValue를 주지 않는 경우 두번째 실행에서 stringValue가 null이므로 result가 999가 될 것이라고 예상할 수 있습니다. 하지만 `RunIdIncrementer`는 이런 경우에 직전 JobExecution의 stringValue 값을 재사용하여 result값이 21 (longValue: 20, stringValue: 1)이 되는 것을 확인할 수 있습니다.

```java
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
    JobExplorer jobExplorer = applicationContext.getBean(JobExplorer.class);
    Job job = applicationContext.getBean(Job.class);

    JobParameters firstJobParameter = new JobParametersBuilder(jobExplorer)
        .addString("stringValue", "1")
        .addString("longValue", "10")
        .getNextJobParameters(job)
        .toJobParameters();
    JobExecution firstJobExecution = jobLauncher.run(job, firstJobParameter);

    JobParameters secondJobParameter = new JobParametersBuilder(jobExplorer)
        .addString("longValue", "20") // stringValue 미사용
        .getNextJobParameters(job)
        .toJobParameters();
    JobExecution secondJobExecution = jobLauncher.run(job, secondJobParameter);

    // first: COMPLETED, jobParameters: {run.id=1, stringValue=1, longValue=10}, result: 11
    System.out.printf("first: %s, jobParameters: %s, result: %d%n",
        firstJobExecution.getExitStatus().getExitCode(),
        firstJobExecution.getJobParameters(),
        firstJobExecution.getExecutionContext().getLong("result"));

    // second: COMPLETED, jobParameters: {run.id=2, stringValue=1, longValue=20}, result: 21
    System.out.printf("second: %s, jobParameters: %s, result: %d%n",
        secondJobExecution.getExitStatus().getExitCode(),
        secondJobExecution.getJobParameters(),
        secondJobExecution.getExecutionContext().getLong("result"));
}
```

`ClearRunIdIncrementer`는 이를 해결하여 이전 JobExecution의 인자를 신규 Job을 수행할 때 사용하지 않게 처리합니다.

```java
@Bean
public Job testJob(
    JobRepository jobRepository
) {
    return new JobBuilder("testJob", jobRepository)
        .incrementer(ClearRunIdIncrementer.create()) // use ClearRunIdIncrementer
        .start(
            new StepBuilder("testStep", jobRepository)
                .tasklet(
                    testTasklet(null, null),
                    new ResourcelessTransactionManager()
                )
                .build()
        )
        .build();
}

@StepScope
@Bean
public Tasklet testTasklet(
    @Value("#{jobParameters['longValue']}") Long longValue,
    @Value("#{jobParameters['stringValue']}") String stringValue
) {
    return (contribution, chunkContext) -> {
        Long result;
        if (stringValue != null) {
            result = longValue + Long.parseLong(stringValue);
        } else {
            result = 999L;
        }

        ExecutionContext jobExecutionContext = contribution.getStepExecution()
            .getJobExecution()
            .getExecutionContext();
        jobExecutionContext.putLong("result", result);
        return RepeatStatus.FINISHED;
    };
}
```

`ClearRunIdIncrementer`를 사용하는 경우 stringValue에 값이 null로 들어가서 result가 999가 되는 것을 확인할 수 있습니다.

```java
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
    JobExplorer jobExplorer = applicationContext.getBean(JobExplorer.class);
    Job job = applicationContext.getBean(Job.class);

    JobParameters firstJobParameter = new JobParametersBuilder(jobExplorer)
        .addString("stringValue", "1")
        .addString("longValue", "10")
        .getNextJobParameters(job)
        .toJobParameters();
    JobExecution firstJobExecution = jobLauncher.run(job, firstJobParameter);

    JobParameters secondJobParameter = new JobParametersBuilder(jobExplorer)
        .addString("longValue", "20")
        .getNextJobParameters(job)
        .toJobParameters();
    JobExecution secondJobExecution = jobLauncher.run(job, secondJobParameter);

    // first: COMPLETED, jobParameters: {run.id=1, stringValue=1, longValue=10}, result: 11
    System.out.printf("first: %s, jobParameters: %s, result: %d%n",
        firstJobExecution.getExitStatus().getExitCode(),
        firstJobExecution.getJobParameters(),
        firstJobExecution.getExecutionContext().getLong("result"));

    // second: COMPLETED, jobParameters: {run.id=2, longValue=20}, result: 999
    System.out.printf("second: %s, jobParameters: %s, result: %d%n",
        secondJobExecution.getExitStatus().getExitCode(),
        secondJobExecution.getJobParameters(),
        secondJobExecution.getExecutionContext().getLong("result"));
}
```

## 기본 run id를 사용하기

`ClearRunIdIncrementer`에서는 생성할 때 별도의 인자를 주지 않으면 `run.id`를 증가 대상 JobParameter의 id로 사용합니다.

### Java

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobRepository jobRepository
    ) {
        return new JobBuilder("testJob", jobRepository)
            .incrementer(ClearRunIdIncrementer.create())
            .start(
                new StepBuilder("testStep", jobRepository)
                    .tasklet(
                        (contribution, chunkContext) -> RepeatStatus.FINISHED,
                        new ResourcelessTransactionManager()
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
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl,
    ): Job = batch {
        job("testJob") {
            incrementer(ClearRunIdIncrementer.create())
            step("testStep") {
                tasklet(
                    { _, _ -> RepeatStatus.FINISHED },
                    ResourcelessTransactionManager()
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
        JobRepository jobRepository
    ) {
        return new JobBuilder("testJob", jobRepository)
            .incrementer(ClearRunIdIncrementer.create("testId"))
            .start(
                new StepBuilder("testStep", jobRepository)
                    .tasklet(
                        (contribution, chunkContext) -> RepeatStatus.FINISHED,
                        new ResourcelessTransactionManager()
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
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            incrementer(ClearRunIdIncrementer.create("testId"))
            step("testStep") {
                tasklet(
                    { _, _ -> RepeatStatus.FINISHED },
                    ResourcelessTransactionManager()
                )
            }
        }
    }
}
```