# DeleteMetadataJob

- [Create a deleteMetadataJob](#create-a-deletemetadatajob)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [Specify a job name](#specify-a-job-name)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)
- [Specify a metatable prefix](#specify-a-metatable-prefix)
  - [Java](#java-2)
  - [Kotlin](#kotlin-2)
- [Specify a date job parameter name](#specify-a-date-job-parameter-name)
  - [Java](#java-3)
  - [Kotlin](#kotlin-3)
- [Specify a date format](#specify-a-date-format)
  - [Java](#java-4)
  - [Kotlin](#kotlin-4)
- [Execute as a dryRun mode](#execute-as-a-dryrun-mode)
  - [Java](#java-5)
  - [Kotlin](#kotlin-5)
- [Specify dryRun parameter name](#specify-dryrun-parameter-name)
  - [Java](#java-6)
  - [Kotlin](#kotlin-6)

Execution of all `Jobs` in Spring Batch is stored in metadata. The longer you keep your job DB, the larger the data becomes, which may require you to delete old data. A `deleteMetadataJob` deletes the metadata of all the `Jobs` and `Steps` that have run before the specified date.

## Create a deleteMetadataJob 

You can pass `jobRepository` and `dataSource` of metadata to `DeleteMetadataJobBuilder` to create a `deleteMetadataJob`. Unless otherwise specified, the name of a `Job` is deleteMetadataJob, the name of a `JobParameter` is baseDate, the date format is yyyy/MM/dd, and the table prefix is `BATCH_`.

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
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch deleteMetadataJob
    Job removeJob = applicationContext.getBean("deleteMetadataJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
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
fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch deleteMetadataJob
    val removeJob = applicationContext.getBean<Job>("deleteMetadataJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameter = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```

## Specify a job name

You can call the `name` of `DeleteMetadataJobBuilder` to specify a `Job` name.

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
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch removeJob
    Job removeJob = applicationContext.getBean("removeJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("base", now.format(formatter)) // custom naming
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
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
fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameter = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```

## Specify a metatable prefix

You can call the `tablePrefix` of `DeleteMetadataJobBuilder` to specify a table prefix. The following example sets a metatable prefix to `CUSTOM_` (CUSTOM_JOB_INSTANCE, CUSTOM_JOB_EXECUTION, ...).

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
public static void main(String[] args) throws Exception {
    // launch with custom prefix
    SpringApplication application = new SpringApplication(BatchApplication.class);
    Properties properties = new Properties();
    properties.put("spring.batch.jdbc.table-prefix", "CUSTOM_");
    application.setDefaultProperties(properties);
    ApplicationContext applicationContext = application.run(args);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch removeJob
    Job removeJob = applicationContext.getBean("removeJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
```

You can also get the value of `spring.batch.jdbc.table-prefix` in Spring Boot as shown in the following code example.

```java
import org.springframework.boot.autoconfigure.batch.BatchProperties;

...

@Configuration
public class TestJobConfig {

    @Bean
    public Job removeJob(
        @BatchDataSource DataSource dataSource,
        JobRepository jobRepository,
        BatchProperties properties
    ) {
        String tablePrefix = properties.getJdbc().getTablePrefix();
        return new DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .tablePrefix(tablePrefix)
            .build();
    }
}
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
fun main() {
    // launch with custom prefix
    val application = SpringApplication(BatchApplication::class.java).apply {
        val properties = Properties().apply {
            this["spring.batch.jdbc.table-prefix"] = "CUSTOM_"
        }
        setDefaultProperties(properties)
    }
    val applicationContext = application.run()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameter = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```

You can also get the value of `spring.batch.jdbc.table-prefix` in Spring Boot as shown in the following code example.

```kotlin
import org.springframework.boot.autoconfigure.batch.BatchProperties

...

@Configuration
open class TestJobConfig {

    @Bean
    open fun removeJob(
        @BatchDataSource dataSource: DataSource,
        jobRepository: JobRepository,
        properties: BatchProperties
    ): Job {
        val tablePrefix = properties.jdbc.tablePrefix
        return DeleteMetadataJobBuilder(jobRepository, dataSource)
            .name("removeJob")
            .tablePrefix(tablePrefix)
            .build()
    }
}
```

## Specify a date job parameter name

You can call the `baseDateParameterName` of `DeleteMetadataJobBuilder` to specify a date `JobParameter` name.

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
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch removeJob
    Job removeJob = applicationContext.getBean("removeJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("base", now.format(formatter)) // custom naming
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
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
fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameter = JobParametersBuilder()
        .addString("base", now.format(formatter)) // custom naming
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```

## Specify a date format

You can call the `baseDateFormatter` of `DeleteMetadataJobBuilder` to specify the format of a date parameter.

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
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch removeJob
    Job removeJob = applicationContext.getBean("removeJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
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
fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val jobParameter = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```

## Execute as a dryRun mode

You can launch job as dryRun mode by setting `dryRun` parameter to `true`.

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
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch removeJob
    Job removeJob = applicationContext.getBean("removeJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .addString("dryRun", "true") // set dryRun to 'true'
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
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
fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameter = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .addString("dryRun", "true") // set dryRun to 'true'
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```

## Specify dryRun parameter name

You can call the `dryRunParameterName` of `DeleteMetadataJobBuilder` to specify a `dryRun` parameter name.

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
            .dryRunParameterName("customDryRunParam")
            .build();
    }
}

// run
public static void main(String[] args) throws Exception {
    ApplicationContext applicationContext = SpringApplication.run(BatchApplication.class);
    JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

    // launch removeJob
    Job removeJob = applicationContext.getBean("removeJob", Job.class);
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    JobParameters jobParameter = new JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .addString("customDryRunParam", "true") // set dryRun to 'true'
        .toJobParameters();
    jobLauncher.run(removeJob, jobParameter);
}
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
            .dryRunParameterName("customDryRunParam")
            .build()
    }
}

// run
fun main() {
    val applicationContext = runApplication<BatchApplication>()
    val jobLauncher = applicationContext.getBean<JobLauncher>()

    // launch removeJob
    val removeJob = applicationContext.getBean<Job>("removeJob")
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val jobParameter = JobParametersBuilder()
        .addString("baseDate", now.format(formatter))
        .addString("customDryRunParam", "true") // set dryRun to 'true'
        .toJobParameters()
    jobLauncher.run(removeJob, jobParameter)
}
```