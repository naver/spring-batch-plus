# Chunk-Oriented Step

- [Specify the chunk size](#specify-the-chunk-size)
- [Set a chunk-oriented step](#set-a-chunk-oriented-step)
  - [Set a listener using annotations](#set-a-listener-using-annotations)
  - [Set a listener using a StepExecutionListener object](#set-a-listener-using-a-stepexecutionlistener-object)
  - [Set a listener using a ChunkListener object](#set-a-listener-using-a-chunklistener-object)
  - [Set a listener using an ItemReadListener object](#set-a-listener-using-an-itemreadlistener-object)
  - [Set a listener using an ItemProcessListener object](#set-a-listener-using-an-itemprocesslistener-object)
  - [Set a listener using an ItemWriteListener object](#set-a-listener-using-an-itemwritelistener-object)
  - [Set a listener using the bean name of an annotated object](#set-a-listener-using-the-bean-name-of-an-annotated-object)
  - [Set a listener using the bean name of a ChunkListener](#set-a-listener-using-the-bean-name-of-a-chunklistener)
  - [Set a stream](#set-a-stream)
  - [Set a TaskExecutor](#set-a-taskexecutor)
  - [Set a TransactionAttribute](#set-a-transactionattribute)
  - [Set a TransactionManager](#set-a-transactionmanager)
  - [Set a StepInterruptionPolicy](#set-a-stepinterruptionpolicy)
  - [Set an ObservationRegistry](#set-an-observationregistry)
- [Set faultTolerant](#set-faulttolerant)
  - [Set a SkipListener using annotations](#set-a-skiplistener-using-annotations)
  - [Set a SkipListener](#set-a-skiplistener)
  - [Set a RetryListener](#set-a-retrylistener)
  - [Set a retry class with retryLimit](#set-a-retry-class-with-retrylimit)
  - [Set retryPolicy](#set-retrypolicy)
  - [Set a skip class with skipLimit](#set-a-skip-class-with-skiplimit)
  - [Set SkipPolicy](#set-skippolicy)

A chunk-oriented step consists of `ItemReader`, `ItemProcessor`, and `ItemWriter`.

## Specify the chunk size

You can specify the chunk size to create a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

## Set a chunk-oriented step

The functions that can be set with `ChunkOrientedStepBuilder` are also available with the Kotlin DSL.

### Set a listener using annotations

You can add `@BeforeStep` and `@AfterStep` annotations to an object to set a step execution listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @BeforeStep
        fun beforeStep(stepExecution: StepExecution) {
            println("beforeStep: $stepExecution")
        }

        @AfterStep
        fun afterStep(stepExecution: StepExecution): ExitStatus? {
            println("afterStep: $stepExecution")
            return null
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

You can add `@BeforeChunk`, `@AfterChunk`, and `@AfterChunkError` annotations to an object to set a chunk listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @BeforeChunk
        fun beforeChunk(chunk: Chunk<Int>) {
            println("beforeChunk: $chunk")
        }

        @AfterChunk
        fun afterChunk(chunk: Chunk<String>) {
            println("afterChunk: $chunk")
        }

        @AfterChunkError
        fun afterChunkError() {
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

You can add `@BeforeRead`, `@AfterRead`, and `@OnReadError` annotations to an object to to set an item reader listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @BeforeRead
        fun beforeRead() {
            println("beforeRead")
        }

        @AfterRead
        fun afterRead(item: Any) {
            println("afterRead (item: $item)")
        }

        @OnReadError
        fun onReadError(ex: Exception) {
            println("onReadError (exception: $ex)")
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

You can add `@BeforeProcess`, `@AfterProcess`, and `@OnProcessError` annotations to an object to set an item process listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @BeforeProcess
        fun beforeProcess(item: Any) {
            println("beforeProcess: $item")
        }

        @AfterProcess
        fun afterProcess(
            item: Any,
            result: Any?,
        ) {
            println("afterProcess: $item, result: $result")
        }

        @OnProcessError
        fun onProcessError(
            item: Any,
            e: Exception,
        ) {
            println("onProcessError: $item, exception: $e")
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
}
```

You can add `@BeforeWrite`, `@AfterWrite`, and `@OnWriteError` annotations to an object to set an item writer listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @BeforeWrite
        fun beforeWrite(chunk: Chunk<String>) {
            println("beforeWrite: ${chunk.items}")
        }

        @AfterWrite
        fun afterWrite(chunk: Chunk<String>) {
            println("afterWrite: ${chunk.items}")
        }

        @OnWriteError
        fun onWriteError(
            exception: Exception,
            chunk: Chunk<String>,
        ) {
            println("afterWrite: ${chunk.items}, exception: $exception")
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

You can selectively add annotation-based listeners to an object.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @AfterRead
        fun afterRead(item: Any) {
            println("afterRead (item: $item)")
        }

        @AfterProcess
        fun afterProcess(
            item: Any,
            result: Any?,
        ) {
            println("afterProcess: $item, result: $result")
        }

        @BeforeWrite
        fun beforeWrite(chunk: Chunk<String>) {
            println("beforeWrite: ${chunk.items}")
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a listener using a StepExecutionListener object

You can pass a `StepExecutionListener` object directly to set a step execution listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(
                            object : StepExecutionListener {
                                override fun beforeStep(stepExecution: StepExecution) {
                                    println("beforeStep: $stepExecution")
                                }

                                override fun afterStep(stepExecution: StepExecution): ExitStatus? {
                                    println("afterStep: $stepExecution")
                                    return null
                                }
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a listener using a ChunkListener object

You can pass a `ChunkListener` object as an argument to set a chunk listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(
                            object : ChunkListener<Int, String> {
                                override fun beforeChunk(chunk: Chunk<Int>) {
                                    println("beforeChunk: $chunk")
                                }

                                override fun afterChunk(chunk: Chunk<String>) {
                                    println("afterChunk: $chunk")
                                }

                                override fun onChunkError(
                                    exception: Exception,
                                    chunk: Chunk<String>,
                                ) {
                                }
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
}
```

### Set a listener using an ItemReadListener object

You can pass an `ItemReadListener` object as an argument to set an item read listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(
                            object : ItemReadListener<Int> {
                                override fun beforeRead() {
                                    println("beforeRead")
                                }

                                override fun onReadError(ex: Exception) {
                                }

                                override fun afterRead(item: Int) {
                                    println("afterRead (item: $item)")
                                }
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a listener using an ItemProcessListener object

You can pass an `ItemProcessListener` object as an argument to set an item process listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(
                            object : ItemProcessListener<Int, String> {
                                override fun beforeProcess(item: Int) {
                                    println("beforeProcess: $item")
                                }

                                override fun afterProcess(
                                    item: Int,
                                    result: String?,
                                ) {
                                    println("afterProcess: $item, result: $result")
                                }

                                override fun onProcessError(
                                    item: Int,
                                    e: Exception,
                                ) {
                                }
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a listener using an ItemWriteListener object

You can pass an `ItemWriteListener` object as an argument to set an item write listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(
                            object : ItemWriteListener<String> {
                                override fun beforeWrite(chunk: Chunk<out String>) {
                                    println("beforeWrite: ${chunk.items}")
                                }

                                override fun afterWrite(chunk: Chunk<out String>) {
                                    println("afterWrite: ${chunk.items}")
                                }

                                override fun onWriteError(
                                    exception: Exception,
                                    Chunk: Chunk<out String>,
                                ) {
                                }
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a listener using the bean name of an annotated object

You can add `@BeforeStep`, `@AfterStep`, `@BeforeChunk` and `@AfterChunk` annotations to a `@Component` object and set it as a listener using its bean name.

```kotlin
@Component
class TestListener {
    @BeforeStep
    fun beforeStep() {
        println("beforeStep")
    }

    @AfterStep
    fun afterStep() {
        println("afterStep")
    }

    @BeforeChunk
    fun beforeChunk() {
        println("beforeChunk")
    }

    @AfterChunk
    fun afterChunk() {
        println("afterChunk")
    }
}
```

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listenerBean("testListener")
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a listener using the bean name of a ChunkListener

You can set an object implementing `ChunkListener` as a listener using its bean name.

```kotlin
@Component
class TestListener : ChunkListener<Int, String> {
    override fun beforeChunk(chunk: Chunk<Int>) {
        println("beforeChunk: $chunk")
    }

    override fun afterChunk(chunk: Chunk<String>) {
        println("afterChunk: $chunk")
    }

    override fun onChunkError(
        exception: Exception,
        chunk: Chunk<String>,
    ) {
    }
}
```

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listenerBean("testListener")
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a stream

You can pass an `ItemStream` object as an argument to set a stream.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
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
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 11) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
}
```

### Set a TaskExecutor

You can pass a `TaskExecutor` object as an argument to run chunks at the same time.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun customExecutor(): AsyncTaskExecutor = SimpleAsyncTaskExecutor()

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(testItemReader())
                        writer(testItemWriter())
                        taskExecutor(customExecutor())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 20) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> =
        ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
}
```

### Set a TransactionAttribute

You can use a `TransactionAttribute` object to set a transaction. The following example uses PROPAGATION_NOT_SUPPORTED to set the transaction not to work.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(testItemReader())
                        writer(testItemWriter())
                        transactionAttribute(
                            DefaultTransactionAttribute().apply {
                                setName("test-tx")
                            },
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 20) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> =
        ItemWriter { items ->
            val transactionName = TransactionSynchronizationManager.getCurrentTransactionName()
            println("write $items (transactionName: $transactionName)")
        }
}
```

### Set a TransactionManager

You can set the `PlatformTransactionManager` that the chunk-oriented step uses.

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
                step("testStep") {
                    chunk<Int, Int>(3) {
                        transactionManager(transactionManager)
                        reader(testItemReader())
                        writer(testItemWriter())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 20) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a StepInterruptionPolicy

You can set the `StepInterruptionPolicy` that decides whether a running step should be interrupted.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(testItemReader())
                        writer(testItemWriter())
                        interruptionPolicy(ThreadStepInterruptionPolicy())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 20) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set an ObservationRegistry

You can set an `ObservationRegistry` to observe the step execution.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, Int>(3) {
                        reader(testItemReader())
                        writer(testItemWriter())
                        observationRegistry(ObservationRegistry.create())
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> =
        object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? =
                if (count < 20) {
                    count++
                } else {
                    null
                }
        }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

## Set faultTolerant

A chunk-oriented step lets you define an action to run when it fails. The Kotlin DSL helps you do the same.

### Set a SkipListener using annotations

If you set faultTolerant, you can set a skip listener based on `@OnSkipInRead`, `@OnSkipInProcess`, and `@OnSkipInWrite`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    class TestListener {
        @OnSkipInRead
        fun onSkipInRead(t: Throwable) {
            println("Ignore exception of read (exception: ${t.message})")
        }

        @OnSkipInProcess
        fun onSkipInProcess(
            item: Any,
            t: Throwable,
        ) {
            println("Ignore exception of process (item: $item, exception: ${t.message})")
        }

        @OnSkipInWrite
        fun onSkipInWrite(
            item: Any,
            t: Throwable,
        ) {
            println("Ignore exception of write (item: $item, exception: ${t.message})")
        }
    }

    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        listener(TestListener())
                        faultTolerant()
                        skip<IllegalStateException>()
                        skipLimit(1L)
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                val next = count++

                if (next == 3) {
                    throw IllegalStateException("I am ignored")
                }

                if (next < 11) {
                    return next
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a SkipListener

You can use a `SkipListener` object to set a skip listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        skipListener(
                            object : SkipListener<Int, String> {
                                override fun onSkipInRead(t: Throwable) {
                                    println("Ignore exception of read (exception: ${t.message})")
                                }

                                override fun onSkipInProcess(
                                    item: Int,
                                    t: Throwable,
                                ) {
                                    println("Ignore exception of process (item: $item, exception: ${t.message})")
                                }

                                override fun onSkipInWrite(
                                    item: String,
                                    t: Throwable,
                                ) {
                                    println("Ignore exception of write (item: $item, exception: ${t.message})")
                                }
                            },
                        )
                        skip<IllegalStateException>()
                        skipLimit(3L)
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                val next = count++

                if (next == 3) {
                    throw IllegalStateException("I am ignored in read")
                }

                if (next < 11) {
                    return next
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            if (item == 5) {
                throw IllegalStateException("I am ignored in process")
            }

            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { chunk ->
            if (chunk.items.contains("7")) {
                throw IllegalStateException("I am ignored in write")
            }

            println("write ${chunk.items}")
        }
}
```

### Set a RetryListener

You can use a `RetryListener` object to set a retry listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        retryListener(
                            object : RetryListener {
                                override fun beforeRetry(
                                    retryPolicy: RetryPolicy,
                                    retryable: Retryable<*>,
                                    retryState: RetryState,
                                ) {
                                    println("RetryListener::beforeRetry (state: $retryState)")
                                }

                                override fun onRetryFailure(
                                    retryPolicy: RetryPolicy,
                                    retryable: Retryable<*>,
                                    throwable: Throwable,
                                ) {
                                    println("RetryListener::onRetryFailure (error: ${throwable.message})")
                                }

                                override fun onRetryPolicyExhaustion(
                                    retryPolicy: RetryPolicy,
                                    retryable: Retryable<*>,
                                    exception: RetryException,
                                ) {
                                    println("RetryListener::onRetryPolicyExhaustion (error: ${exception.message})")
                                }
                            },
                        )
                        retry<IllegalStateException>()
                        retryLimit(4L)
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                if (count < 11) {
                    return count++
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        var tryCount = 0

        return ItemProcessor<Int, String> { item ->
            if (item == 5 && tryCount < 3) {
                ++tryCount
                throw IllegalStateException("Error (tryCount: $tryCount)")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a retry class with retryLimit

You can set a retry class with retryLimit to retry a specific exception and its subclasses.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        retry<RuntimeException>()
                        retryLimit(4L)
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                if (count < 11) {
                    return count++
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        var tryCount = 0

        return ItemProcessor<Int, String> { item ->
            if (item == 5 && tryCount < 3) {
                ++tryCount
                throw IllegalStateException("Error (tryCount: $tryCount)")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set retryPolicy

You can set `retryPolicy` to set retry policies.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        retry<RuntimeException>()
                        retryPolicy(RetryPolicy.withMaxRetries(4))
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                if (count < 11) {
                    return count++
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        var tryCount = 0

        return ItemProcessor<Int, String> { item ->
            if (item == 5 && tryCount < 3) {
                ++tryCount
                throw IllegalStateException("Error (tryCount: $tryCount)")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set a skip class with skipLimit

You can set a skip class with skipLimit to skip a specific exception and its subclasses.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        skip<RuntimeException>()
                        skipLimit(4L)
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                if (count < 11) {
                    return count++
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```

### Set SkipPolicy

You can set `SkipPolicy` to skip exceptions.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
) {
    @Bean
    open fun testJob(): Job =
        batch {
            job("testJob") {
                step("testStep") {
                    chunk<Int, String>(3) {
                        reader(testItemReader())
                        processor(testItemProcessor())
                        writer(testItemWriter())
                        faultTolerant()
                        skipPolicy(
                            LimitCheckingItemSkipPolicy(
                                4,
                                mapOf(RuntimeException::class.java to true),
                            ),
                        )
                    }
                }
            }
        }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                if (count < 11) {
                    return count++
                } else {
                    return null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> =
        ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            item.toString()
        }

    @Bean
    open fun testItemWriter(): ItemWriter<String> =
        ItemWriter { items ->
            println("write $items")
        }
}
```
