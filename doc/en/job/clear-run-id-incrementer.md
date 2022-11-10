# ClearRunIdIncrementer

- [Use the default run ID](#use-the-default-run-id)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [Specify a run ID](#specify-a-run-id)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)

`ClearRunIdIncrementer` is a `JobParametersIncrementer` that can be used instead of a [RunIdIncrementer](http://github.com/spring-projects/spring-batch/blob/master/spring-batch-core/src/main/java/org/springframework/batch/core/launch/support/RunIdIncrementer.java) in Spring Batch. `RunIdIncrementer` is a class that provides an additional job parameter called run.id to renew a job even if the same job parameter is given. This class increments the value of run.id if it exists among the given job parameters or adds a new one if not, and then returns it. `RunIdIncrementer` is internally used when you call the getNextJobParameters method of `JobParametersBuilder`.

```java
Job job = ...
JobParameters firstJobParameter = new JobParametersBuilder(jobExplorer)
    .addString("stringValue", "1")
    .addString("longValue", "10")
    .getNextJobParameters(job) // Use RunIdIncrementer internally
    .toJobParameters();
```

The getNextJobParameters method of `JobParametersBuilder` retrieves metadata, extracts job parameters from the last job execution of the job, and passes them to RunIdIncrementer, which then changes only the value of run.id and returns it. The problem here is that it also returns the other job parameters used in the last job execution of the job.  

Let’s say you define a job using two job parameters as shown in the following example.

```java
@Bean
public Job testJob(
    JobBuilderFactory jobBuilderFactory,
    StepBuilderFactory stepBuilderFactory
) {
    return jobBuilderFactory.get("testJob")
        .incrementer(new RunIdIncrementer())
        .start(
            stepBuilderFactory.get("testStep")
                .tasklet(testTasklet(null, null))
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

If the first execution has a stringValue given and the second does not, you can expect that the result is 999 because the stringValue of the second execution is null. However, `RunIdIncrementer` reuses the stringValue of the previous job execution, so the result is 21 (longValue: 20, stringValue: 1).

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

`ClearRunIdIncrementer` prevents the parameters of the previous job execution from being used for a new job.

```java
@Bean
public Job testJob(
    JobBuilderFactory jobBuilderFactory,
    StepBuilderFactory stepBuilderFactory
) {
    return jobBuilderFactory.get("testJob")
        .incrementer(ClearRunIdIncrementer.create()) // ClearRunIdIncrementer 사용
        .start(
            stepBuilderFactory.get("testStep")
                .tasklet(testTasklet(null, null))
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

Using `ClearRunIdIncrementer` sets the stringValue as null, so the result is 999.

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

## Use the default run ID

If a `ClearRunIdIncrementer` is created with no arguments specified, it regards `run.id` as the ID of a job parameter to increment.

### Java

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory
    ) {
        return jobBuilderFactory.get("testJob")
            .incrementer(ClearRunIdIncrementer.create())
            .start(
                stepBuilderFactory.get("testStep")
                    .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
                    .build()
            )
            .build();
    }
}
```

### Kotlin

```kotlin
@Configuration
class TestJobConfig {

    @Bean
    fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            incrementer(ClearRunIdIncrementer.create())
            step("testStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```

## Specify a run ID

If a `ClearRunIdIncrementer` is created with an argument specified, it regards the value as the ID of a job parameter to increment.

### Java

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory
    ) {
        return jobBuilderFactory.get("testJob")
            .incrementer(ClearRunIdIncrementer.create("testId"))
            .start(
                stepBuilderFactory.get("testStep")
                    .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
                    .build()
            )
            .build();
    }
}
```

### Kotlin

```kotlin
@Configuration
class TestJobConfig {

    @Bean
    fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            incrementer(ClearRunIdIncrementer.create("testId"))
            step("testStep") {
                tasklet { _, _ -> RepeatStatus.FINISHED }
            }
        }
    }
}
```
