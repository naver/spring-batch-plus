# ClearRunIdIncrementer

- [Use the default run ID](#use-the-default-run-id)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [Specify a run ID](#specify-a-run-id)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)

`ClearRunIdIncrementer` is a `JobParametersIncrementer` that can be used instead of a [RunIdIncrementer](http://github.com/spring-projects/spring-batch/blob/master/spring-batch-core/src/main/java/org/springframework/batch/core/launch/support/RunIdIncrementer.java) in Spring Batch. `RunIdIncrementer` is a class that provides an additional job parameter called run.id to renew a job even if the same job parameter is given. This class increments the value of run.id if it exists among the given job parameters or adds a new one if not, and then returns it. `RunIdIncrementer` is internally used when you call the startNextInstance method of `JobOperator`.

```java
Job job = ... // a job that uses RunIdIncrementer
JobExecution jobExecution = jobOperator.startNextInstance(job); // Use RunIdIncrementer internally
```

The startNextInstance method of `JobOperator` retrieves metadata, extracts job parameters from the last job execution of the job, and passes them to RunIdIncrementer, which then changes only the value of run.id and returns it. The problem here is that it also returns the other job parameters used in the last job execution of the job.  

Let's say a job used to run with two job parameters given directly as shown in the following example.

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

Once `RunIdIncrementer` is added, `JobOperator` ignores job parameters passed to it and launches the job with `startNextInstance`. `RunIdIncrementer` inherits every job parameter of the previous job execution and only increments run.id, so the stringValue and longValue used before the incrementer was added keep coming along.

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

`ClearRunIdIncrementer` prevents the parameters of the previous job execution from being used for a new job. Only run.id is left, so the stringValue and longValue are not carried over.

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

## Use the default run ID

If a `ClearRunIdIncrementer` is created with no arguments specified, it regards `run.id` as the ID of a job parameter to increment.

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

## Specify a run ID

If a `ClearRunIdIncrementer` is created with an argument specified, it regards the value as the ID of a job parameter to increment.

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
