# Chunk-Oriented Step

- [Specify the chunk size](#specify-the-chunk-size)
- [Specify CompletionPolicy](#specify-completionpolicy)
- [Specify RepeatOperations](#specify-repeatoperations)
- [Set a chunk-oriented step](#set-a-chunk-oriented-step)
  - [Set a listener using annotations](#set-a-listener-using-annotations)
  - [Set a listener using a ChunkListener object](#set-a-listener-using-a-chunklistener-object)
  - [Set a listener using an ItemReadListener object](#set-a-listener-using-an-itemreadlistener-object)
  - [Set a listener using an ItemProcessListener object](#set-a-listener-using-an-itemprocesslistener-object)
  - [Set a listener using an ItemWriteListener object](#set-a-listener-using-an-itemwritelistener-object)
  - [Set a stream](#set-a-stream)
  - [Set a TaskExecutor](#set-a-taskexecutor)
  - [Set an ExceptionHandler](#set-an-exceptionhandler)
  - [Set RepeatOperations](#set-repeatoperations)
  - [Set a TransactionAttribute](#set-a-transactionattribute)
- [Set faultTolerant](#set-faulttolerant)
  - [Set a SkipListener using annotations](#set-a-skiplistener-using-annotations)
  - [Set a SkipListener](#set-a-skiplistener)
  - [Set a RetryListener](#set-a-retrylistener)
  - [Set a KeyGenerator](#set-a-keygenerator)
  - [Set a retry class with retryLimit](#set-a-retry-class-with-retrylimit)
  - [Set a noRetry class](#set-a-noretry-class)
  - [Set retryPolicy](#set-retrypolicy)
  - [Set BackOffPolicy](#set-backoffpolicy)
  - [Set RetryContextCache](#set-retrycontextcache)
  - [Set a skip class with skipLimit](#set-a-skip-class-with-skiplimit)
  - [Set a noSkip class](#set-a-noskip-class)
  - [Set SkipPolicy](#set-skippolicy)
  - [Set a noRollback class](#set-a-norollback-class)
  - [Set processorNonTransactional](#set-processornontransactional)

A chunk-oriented step consists of `ItemReader`, `ItemProcessor`, and `ItemWriter`.

## Specify the chunk size

You can specify the chunk size to create a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

## Specify CompletionPolicy

You can specify CompletionPolicy to create a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(SimpleCompletionPolicy(3), transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

## Specify RepeatOperations

You can specify `RepeatOperations` to create a `Step`.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                val repeatOperations = RepeatTemplate().apply {
                    setCompletionPolicy(SimpleCompletionPolicy(3))
                }
                chunk<Int, String>(repeatOperations, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

## Set a chunk-oriented step

The functions that can be set with `SimpleStepBuilder` are also available with the Kotlin DSL.

### Set a listener using annotations

You can add `@BeforeChunk`, `@AfterChunk`, and `@AfterChunkError` annotations to an object to set a chunk listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

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
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(TestListener())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

You can add `@BeforeRead`, `@AfterRead`, and `@OnReadError` annotations to an object to to set an item reader listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
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
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(TestListener())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

You can add `@BeforeProcess`, `@AfterProcess`, and `@OnProcessError` annotations to an object to set an item process listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    class TestListener {
        @BeforeProcess
        fun beforeProcess(item: Any) {
            println("beforeProcess: $item")
        }

        @AfterProcess
        fun afterProcess(item: Any, result: Any?) {
            println("afterProcess: $item, result: $result")
        }

        @OnProcessError
        fun onProcessError(item: Any, e: Exception) {
            println("onProcessError: $item, exception: $e")
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(TestListener())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
    }
}
```

You can add `@BeforeWrite`, `@AfterWrite`, and `@OnWriteError` annotations to an object to set an item writer listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
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
        fun onWriteError(exception: Exception, chunk: Chunk<String>) {
            println("afterWrite: ${chunk.items}, exception: $exception")
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(TestListener())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

You can selectively add annotation-based listeners to an object.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    class TestListener {
        @AfterRead
        fun afterRead(item: Any) {
            println("afterRead (item: $item)")
        }

        @AfterProcess
        fun afterProcess(item: Any, result: Any?) {
            println("afterProcess: $item, result: $result")
        }

        @BeforeWrite
        fun beforeWrite(chunk: Chunk<String>) {
            println("beforeWrite: ${chunk.items}")
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(TestListener())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
    }
}
```

### Set a listener using a ChunkListener object

You can pass a `ChunkListener` object as an argument to set a chunk listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
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
                        },
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
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
    }
}
```

### Set a listener using an ItemReadListener object

You can pass an `ItemReadListener` object as an argument to set an item read listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
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
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a listener using an ItemProcessListener object

You can pass an `ItemProcessListener` object as an argument to set an item process listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(
                        object : ItemProcessListener<Int, String> {
                            override fun beforeProcess(item: Int) {
                                println("beforeProcess: $item")
                            }

                            override fun afterProcess(item: Int, result: String?) {
                                println("afterProcess: $item, result: $result")
                            }

                            override fun onProcessError(item: Int, e: Exception) {
                            }
                        },
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
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a listener using an ItemWriteListener object

You can pass an `ItemWriteListener` object as an argument to set an item write listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
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

                            override fun onWriteError(exception: Exception, Chunk: Chunk<out String>) {
                            }
                        },
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
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a stream

You can pass an `ItemStream` object as an argument to set a stream.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
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
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 11) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
    }
}
```

### Set a TaskExecutor

You can pass a `TaskExecutor` object as an argument to run chunks at the same time.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun customExecutor(): TaskExecutor {
        return SimpleAsyncTaskExecutor()
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3, transactionManager) {
                    reader(testItemReader())
                    writer(testItemWriter())
                    taskExecutor(customExecutor())
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 20) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> {
        return ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
    }
}
```

### Set an ExceptionHandler

You can use an `ExceptionHandler` object to set an exception handler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3, transactionManager) {
                    reader(testItemReader())
                    writer(testItemWriter())
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
    open fun testItemReader(): ItemReader<Int> {
        return ItemReader<Int> {
            throw IllegalStateException("Error in read")
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3, transactionManager) {
                    reader(testItemReader())
                    writer(testItemWriter())
                    exceptionHandler { _, throwable ->
                        println("handle exception ${throwable.message}")
                        throw throwable
                    }
                }
            }
        }
    }

    @Bean
    open fun testItemReader(): ItemReader<Int> {
        return ItemReader<Int> {
            throw IllegalStateException("Error in read")
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set RepeatOperations

You can use a `RepeatOperations` object to set a repeat operation.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3, transactionManager) {
                    reader(testItemReader())
                    writer(testItemWriter())
                    stepOperations(
                        object : RepeatOperations {
                            override fun iterate(callback: RepeatCallback): RepeatStatus {
                                val delegate = RepeatTemplate()
                                println("custom iterate")
                                return delegate.iterate(callback)
                            }
                        },
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
                return if (count < 20) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> {
        return ItemWriter { items ->
            println("[${Thread.currentThread().name}] write $items")
        }
    }
}
```

### Set a TransactionAttribute

You can use a `TransactionAttribute` object to set a transaction. The following example uses PROPAGATION_NOT_SUPPORTED to set the transaction not to work.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3, transactionManager) {
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
    open fun testItemReader(): ItemReader<Int> {
        return object : ItemReader<Int> {
            private var count = 0

            override fun read(): Int? {
                return if (count < 20) {
                    count++
                } else {
                    null
                }
            }
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<Int> {
        return ItemWriter { items ->
            val transactionName = TransactionSynchronizationManager.getCurrentTransactionName()
            println("write $items (transactionName: $transactionName)")
        }
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
    private val transactionManager: PlatformTransactionManager,
) {

    class TestListener {

        @OnSkipInRead
        fun onSkipInRead(t: Throwable) {
            println("Ignore exception of read (exception: ${t.message})")
        }

        @OnSkipInProcess
        fun onSkipInProcess(item: Any, t: Throwable) {
            println("Ignore exception of process (item: $item, exception: ${t.message})")
        }

        @OnSkipInWrite
        fun onSkipInWrite(item: Any, t: Throwable) {
            println("Ignore exception of write (item: $item, exception: ${t.message})")
        }
    }

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(TestListener())
                    faultTolerant {
                        skip<IllegalStateException>()
                        skipLimit(1)
                    }
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
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a SkipListener

You can use a `SkipListener` object to set a skip listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        listener(
                            object : SkipListener<Int, String> {
                                override fun onSkipInRead(t: Throwable) {
                                    println("Ignore exception of read (exception: ${t.message})")
                                }

                                override fun onSkipInProcess(item: Int, t: Throwable) {
                                }

                                override fun onSkipInWrite(item: String, t: Throwable) {
                                }
                            },
                        )
                        skip<IllegalStateException>()
                        skipLimit(1)
                    }
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
    open fun testItemProcessor(): ItemProcessor<Int, String> {
        return ItemProcessor<Int, String> { item ->
            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a RetryListener

You can use a `RetryListener` object to set a retry listener.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        listener(
                            object : RetryListener {
                                override fun <T : Any?, E : Throwable?> open(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                ): Boolean {
                                    println("RetryListener::open (context: $context")
                                    return true
                                }

                                override fun <T : Any?, E : Throwable?> close(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                    throwable: Throwable?,
                                ) {
                                    println("RetryListener::close (error: ${throwable?.message})")
                                }

                                override fun <T : Any?, E : Throwable?> onError(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                    throwable: Throwable?,
                                ) {
                                    println("RetryListener::onError (error: ${throwable?.message})")
                                }
                            },
                        )
                        retry<IllegalStateException>()
                        retryLimit(4)
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a KeyGenerator

You can use a `KeyGenerator` object to set a key that identifies items on a retry.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        keyGenerator(
                            object : KeyGenerator {
                                override fun getKey(item: Any): Any {
                                    println("get key of $item")
                                    return item.toString()
                                }
                            }
                        )
                        retry<IllegalStateException>()
                        retryLimit(4)
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

You can use Kotlin’s trailing lambda to make the code simpler.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        keyGenerator { item ->
                            println("get key of $item")
                            item.toString()
                        }
                        retry<IllegalStateException>()
                        retryLimit(4)
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a retry class with retryLimit

You can set a retry class with retryLimit to retry a specific exception and its subclasses.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        retry<RuntimeException>()
                        retryLimit(4)
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a noRetry class

You can set a specific exception not to be retried after setting a retry class.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        retry<RuntimeException>()
                        retryLimit(Int.MAX_VALUE)
                        noRetry<IllegalArgumentException>()
                    }
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
                throw IllegalArgumentException("I cannot be retryed")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set retryPolicy

You can set `retryPolicy` to set retry policies.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        retry<RuntimeException>()
                        retryPolicy(SimpleRetryPolicy(4))
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set BackOffPolicy

You can set `BackOffPolicy` to set backoff on a retry.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        retry<IllegalStateException>()
                        retryLimit(4)
                        backOffPolicy(
                            object : BackOffPolicy {
                                override fun start(context: RetryContext?): BackOffContext? {
                                    return null
                                }

                                override fun backOff(backOffContext: BackOffContext?) {
                                    println("backOff (context: $backOffContext)")
                                }
                            },
                        )
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set RetryContextCache

You can set `RetryContextCache` to set the cache of an internal context on a retry.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        retry<IllegalStateException>()
                        retryLimit(4)
                        retryContextCache(
                            object : MapRetryContextCache() {
                                override fun containsKey(key: Any?): Boolean {
                                    println("contains key: $key")
                                    return super.containsKey(key)
                                }
                            },
                        )
                    }
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
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a skip class with skipLimit

You can set a skip class with skipLimit to skip a specific exception and its subclasses.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        skip<RuntimeException>()
                        skipLimit(4)
                    }
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
        return ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a noSkip class

You can set a specific exception not to be skipped after setting a skip class.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        skip<RuntimeException>()
                        skipLimit(4)
                        noSkip<IllegalArgumentException>()
                    }
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
        return ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            if (item == 5) {
                throw IllegalArgumentException("I cannot be skipped")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set SkipPolicy

You can set `SkipPolicy` to skip exceptions.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
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
        return ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set a noRollback class

You can set noRollback so that no rollback is performed on error.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        noRollback<RuntimeException>()
                    }
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
        return ItemProcessor<Int, String> { item ->
            if (item % 3 == 0) {
                throw IllegalStateException("Error")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```

### Set processorNonTransactional

You can set processorNonTransactional to specify whether a process caches the previous data on a retry.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        retry<IllegalStateException>()
                        retryLimit(4)
                        processorNonTransactional()
                    }
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
                    val next = count
                    println("read: $next")
                    count++
                    return next
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
            println("process: $item")
            if (item == 5 && tryCount < 3) {
                ++tryCount
                throw IllegalStateException("Error (tryCount: $tryCount)")
            }

            item.toString()
        }
    }

    @Bean
    open fun testItemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            println("write $items")
        }
    }
}
```
