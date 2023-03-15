# Tasklet Step

- [Pass a tasklet as a variable](#pass-a-tasklet-as-a-variable)
- [Get a tasklet using the bean name](#get-a-tasklet-using-the-bean-name)
- [Set a tasklet step](#set-a-tasklet-step)
  - [Set a listener using annotations](#set-a-listener-using-annotations)
  - [Set a listener using a ChunkListener object](#set-a-listener-using-a-chunklistener-object)
  - [Set a stream](#set-a-stream)
  - [Set a TaskExecutor](#set-a-taskexecutor)
  - [Set an ExceptionHandler](#set-an-exceptionhandler)
  - [Set RepeatOperations](#set-repeatoperations)
  - [Set a TransactionAttribute](#set-a-transactionattribute)

A tasklet step consists of a single `Tasklet`.

## Pass a tasklet as a variable

You can pass a predefined `tasklet` as a variable to define a `Step`.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}
```

## Get a tasklet using the bean name

You can define a `Tasklet` as a bean and use the bean name to get the tasklet.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                taskletBean("testTasklet", ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    @StepScope
    open fun testTasklet(
        @Value("#{jobParameters['param']}") paramValue: String
    ): Tasklet = Tasklet { _, _ ->
        println("param is '$paramValue'")
        RepeatStatus.FINISHED
    }
}
```

Here, you can take advantage of nullability in Kotlin. If you do not get a tasklet using the bean name, you need to pass testTasklet by calling a method with null as an argument, which is OK because the tasklet is created as a proxy object with `@StepScope`, binding the argument when the `Step` is run. However, this requires the argument type to be declared as nullable in Kotlin. The Kotlin DSL for Spring Batch Plus helps you easily get a tasklet using the bean name, allowing you to declare such an argument as non-nullable.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                taskletBean("testTasklet", ResourcelessTransactionManager())
            }
        }
    }

    @Bean
    @StepScope
    open fun testTasklet(
        @Value("#{jobParameters['param']}") paramValue: String?
    ): Tasklet = Tasklet { _, _ ->
        println("param is '$paramValue'")
        RepeatStatus.FINISHED
    }
}
```

## Set a tasklet step

The functions that can be set with `TaskletStepBuilder` are also available with the Kotlin DSL.

### Set a listener using annotations

You can add `@BeforeChunk`, `@AfterChunk`, and `@AfterChunkError` annotations to an object to set a chunk listener.

```kotlin
@Configuration
open class TestJobConfig {

    class TestListener {
        @BeforeChunk
        fun beforeChunk(context: ChunkContext) {
            println("beforeChunk: $context")
        }

        @AfterChunk
        fun afterChunk(context: ChunkContext) {
            println("afterChunk: $context")
        }

        @AfterChunkError
        fun afterChunkError() {
        }
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    listener(TestListener())
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}
```

### Set a listener using a ChunkListener object

You can pass a `ChunkListener` object as an argument to set a chunk listener.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    listener(
                        object : ChunkListener {
                            override fun beforeChunk(context: ChunkContext) {
                                println("beforeChunk: $context")
                            }

                            override fun afterChunk(context: ChunkContext) {
                                println("afterChunk: $context")
                            }

                            override fun afterChunkError(context: ChunkContext) {
                            }
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}
```

### Set a stream

You can pass an `ItemStream` object as an argument to set a stream.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    stream(
                        object : ItemStream {
                            override fun open(executionContext: ExecutionContext) {
                                println("open stream")
                            }

                            override fun update(executionContext: ExecutionContext) {
                                println("update stream")
                            }

                            override fun close() {
                                println("close stream")
                            }
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}
```

### Set a TaskExecutor

You can pass a `TaskExecutor` object to set an executor.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun customExecutor(): TaskExecutor {
        return object : SimpleAsyncTaskExecutor() {
            override fun execute(task: Runnable) {
                println("run in custom executor")
                super.execute(task)
            }
        }
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    taskExecutor(customExecutor())
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}
```

### Set an ExceptionHandler

You can use an `ExceptionHandler` object to set an exception handler.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    exceptionHandler(
                        object : ExceptionHandler {
                            override fun handleException(context: RepeatContext, throwable: Throwable) {
                                println("handle exception ${throwable.message}")
                                throw throwable
                            }
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        throw IllegalStateException("testTasklet error")
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin

@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    exceptionHandler { _, throwable ->
                        println("handle exception ${throwable.message}")
                        throw throwable
                    }
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        throw IllegalStateException("testTasklet error")
    }
}
```

### Set RepeatOperations

You can use a `RepeatOperations` object to set a repeat operation.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    stepOperations(
                        object : RepeatOperations {
                            override fun iterate(callback: RepeatCallback): RepeatStatus {
                                val delegate = RepeatTemplate()
                                println("custom iterate")
                                return delegate.iterate(callback)
                            }
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        println("run testTasklet")
        RepeatStatus.FINISHED
    }
}
```

### Set a TransactionAttribute

You can use a `TransactionAttribute` object to set a transaction. The following example uses PROPAGATION_NOT_SUPPORTED to set the transaction not to work.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), ResourcelessTransactionManager()) {
                    transactionAttribute(
                        DefaultTransactionAttribute().apply {
                            setName("test-tx")
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        // print false
        val transactionName = TransactionSynchronizationManager.getCurrentTransactionName()
        println("run testTasklet (transactionName: $transactionName}")
        RepeatStatus.FINISHED
    }
}
``` 
