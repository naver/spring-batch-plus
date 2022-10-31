# DeleteMetadataJob

- [DeleteMetadataJob 생성하기](#deletemetadatajob-생성하기)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [Job 이름 지정하기](#job-이름-지정하기)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)
- [Metatable Prefix 지정하기](#metatable-prefix-지정하기)
  - [Java](#java-2)
  - [Kotlin](#kotlin-2)
- [날짜 JobParameter 이름 지정하기](#날짜-jobparameter-이름-지정하기)
  - [Java](#java-3)
  - [Kotlin](#kotlin-3)
- [날짜 형식 지정하기](#날짜-형식-지정하기)
  - [Java](#java-4)
  - [Kotlin](#kotlin-4)

Spring Batch의 모든 `Job` 수행은 metadata에 저장됩니다. Job DB를 오랜 기간 운영하면 데이터가 계속 쌓이게 되므로 오래된 데이터를 삭제할 필요가 있습니다. `DeleteMetadataJob`은 날짜를 기준으로 해당 날짜 이전에 수행된 모든 `Job`, `Step`의 metadata를 삭제하는 `Job`입니다.

## DeleteMetadataJob 생성하기

`DeleteMetadataJobBuilder`를 사용하여 DeleteMetadataJob을 생성할 수 있습니다. `JobRepository`와 metadata의 `DataSource`를 인자로 넘겨서 생성 가능합니다. 기본으로 설정할 경우 `Job` 이름은 deleteMetadataJob, `JobParameter`이름은 baseDate, 날짜 패턴은 yyyy/MM/dd 입니다.

### Java

```java
// config
@Configuration
public class TestJobConfig {

    @Bean
    public Job deleteMetadataJob(
        @BatchDataSource DataSource dataSource,
        JobRepository jobRepository
    ) {
        return new DeleteMetadataJobBuilder(jobRepository, dataSource)
            .build();
    }
}

// run
JobLauncher jobLauncher = ...
Job deleteMetadataJob = applicationContext.getBean("deleteMetadataJob", Job.class);
LocalDate now = LocalDate.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
JobParameters jobParameter = new JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters();
jobLauncher.run(deleteMetadataJob, jobParameter);
```

### Kotlin

```kotlin
// config
@Configuration
open class TestJobConfig {

    @Bean
    open fun deleteMetadataJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository
    ): Job {
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .build()
    }
}

// run
val jobLauncher = ...
val deleteMetadataJob = applicationContext.getBean<Job>("deleteMetadataJob")
val now = LocalDate.now()
val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
val jobParameter = JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters()
jobLauncher.run(deleteMetadataJob, jobParameter)
```

## Job 이름 지정하기

`DeleteMetadataJobBuilder`의 `name` 호출로 `Job` 이름을 지정할 수 있습니다.

### Java

```java
// config
@Configuration
public class TestJobConfig {

    @Bean
    public Job removeJob(
        @BatchDataSource DataSource dataSource,
        JobRepository jobRepository
    ) {
        return new DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .build();
    }
}

// run
JobLauncher jobLauncher = ...
Job removeJob = applicationContext.getBean("removeJob", Job.class);
LocalDate now = LocalDate.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
JobParameters jobParameter = new JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters();
jobLauncher.run(removeJob, jobParameter);
```

### Kotlin

```kotlin
// config
@Configuration
open class TestJobConfig {

    @Bean
    open fun removeJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository
    ): Job {
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .build()
    }
}

// run
val jobLauncher = ...
val removeJob = applicationContext.getBean<Job>("removeJob")
val now = LocalDate.now()
val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
val jobParameter = JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters()
jobLauncher.run(removeJob, jobParameter)
```

## Metatable Prefix 지정하기

`DeleteMetadataJobBuilder`의 `tablePrefix` 호출로 table prefix를 지정할 수 있습니다. 다음의 예시는 meta table의 prefix가 `CUSTOM_` (CUSTOM_JOB_INSTANCE, CUSTOM_JOB_EXECUTION, ...)인 경우입니다.

### Java

```java
// config
@Configuration
public class TestJobConfig {

    @Bean
    public Job removeJob(
        @BatchDataSource DataSource dataSource,
        JobRepository jobRepository
    ) {
        return new DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .tablePrefix("CUSTOM_")
            .build();
    }
}

// run
JobLauncher jobLauncher = ...
Job removeJob = applicationContext.getBean("removeJob", Job.class);
LocalDate now = LocalDate.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
JobParameters jobParameter = new JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters();
jobLauncher.run(removeJob, jobParameter);
```

### Kotlin

```kotlin
// config
@Configuration
open class TestJobConfig {

    @Bean
    open fun removeJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository
    ): Job {
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .tablePrefix("CUSTOM_")
            .build()
    }
}

// run
val jobLauncher = ...
val removeJob = applicationContext.getBean<Job>("removeJob")
val now = LocalDate.now()
val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
val jobParameter = JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters()
jobLauncher.run(removeJob, jobParameter)
```

## 날짜 JobParameter 이름 지정하기

`DeleteMetadataJobBuilder`의 `baseDateParameterName` 호출로 날짜 `JobParameter`의 이름을 지정할 수 있습니다.

### Java

```java
// config
@Configuration
public class TestJobConfig {

    @Bean
    public Job removeJob(
        @BatchDataSource DataSource dataSource,
        JobRepository jobRepository
    ) {
        return new DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .baseDateParameterName("base") // custom naming
            .build();
    }
}

// run
JobLauncher jobLauncher = ...

// launch removeJob
Job removeJob = applicationContext.getBean("removeJob", Job.class);
LocalDate now = LocalDate.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
JobParameters jobParameter = new JobParametersBuilder()
    .addString("base", now.format(formatter)) // custom naming
    .toJobParameters();
jobLauncher.run(removeJob, jobParameter);
```

### Kotlin

```kotlin
// config
@Configuration
open class TestJobConfig {

    @Bean
    open fun removeJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository
    ): Job {
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .baseDateParameterName("base")
            .build()
    }
}

// run
val jobLauncher = ...
val removeJob = applicationContext.getBean<Job>("removeJob")
val now = LocalDate.now()
val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
val jobParameter = JobParametersBuilder()
    .addString("base", now.format(formatter)) // custom naming
    .toJobParameters()
jobLauncher.run(removeJob, jobParameter)
```

## 날짜 형식 지정하기

`DeleteMetadataJobBuilder`의 `baseDateFormatter` 호출로 날짜 인자의 형식을 지정할 수 있습니다.

### Java

```java
// config
@Configuration
public class TestJobConfig {

    @Bean
    public Job removeJob(
        @BatchDataSource DataSource dataSource,
        JobRepository jobRepository
    ) {
        return new DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .baseDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .build();
    }
}

// run
JobLauncher jobLauncher = ...
Job removeJob = applicationContext.getBean("removeJob", Job.class);
LocalDate now = LocalDate.now();
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
JobParameters jobParameter = new JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters();
jobLauncher.run(removeJob, jobParameter);
```

### Kotlin

```kotlin
// config
@Configuration
open class TestJobConfig {

    @Bean
    open fun removeJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository
    ): Job {
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .baseDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .build()
    }
}

// run
val jobLauncher = ...
val removeJob = applicationContext.getBean<Job>("removeJob")
val now = LocalDate.now()
val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val jobParameter = JobParametersBuilder()
    .addString("baseDate", now.format(formatter))
    .toJobParameters()
jobLauncher.run(removeJob, jobParameter)
```
