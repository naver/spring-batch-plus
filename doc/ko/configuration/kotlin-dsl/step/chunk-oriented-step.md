# Chunk-Oriented Step

- [Chunk Size를 지정하여 생성하기](#chunk-size를-지정하여-생성하기)
- [Chunk-oriented Step 설정하기](#chunk-oriented-step-설정하기)
  - [Annotation을 사용하여 Listener 설정하기](#annotation을-사용하여-listener-설정하기)
  - [StepExecutionListener 객체를 사용하여 Listener 설정하기](#stepexecutionlistener-객체를-사용하여-listener-설정하기)
  - [ChunkListener 객체를 사용하여 Listener 설정하기](#chunklistener-객체를-사용하여-listener-설정하기)
  - [ItemReadListener 객체를 사용하여 Listener 설정하기](#itemreadlistener-객체를-사용하여-listener-설정하기)
  - [ItemProcessListener 객체를 사용하여 Listener 설정하기](#itemprocesslistener-객체를-사용하여-listener-설정하기)
  - [ItemWriteListener 객체를 사용하여 Listener 설정하기](#itemwritelistener-객체를-사용하여-listener-설정하기)
  - [Stream 설정하기](#stream-설정하기)
  - [TaskExecutor 설정하기](#taskexecutor-설정하기)
  - [TransactionAttribute 설정하기](#transactionattribute-설정하기)
  - [TransactionManager 설정하기](#transactionmanager-설정하기)
  - [StepInterruptionPolicy 설정하기](#stepinterruptionpolicy-설정하기)
  - [ObservationRegistry 설정하기](#observationregistry-설정하기)
- [FaultTolerant 설정하기](#faulttolerant-설정하기)
  - [Annotation을 사용해서 SkipListener 설정하기](#annotation을-사용해서-skiplistener-설정하기)
  - [SkipListener 설정하기](#skiplistener-설정하기)
  - [RetryListener 설정하기](#retrylistener-설정하기)
  - [Retry Class, RetryLimit 설정하기](#retry-class-retrylimit-설정하기)
  - [RetryPolicy 설정하기](#retrypolicy-설정하기)
  - [Skip Class, SkipLimit 설정하기](#skip-class-skiplimit-설정하기)
  - [SkipPolicy 설정하기](#skippolicy-설정하기)

Chunk-Oriented Step은 `ItemReader`, `ItemProcessor`, `ItemWriter`로 구성된 `Step`입니다.

## Chunk Size를 지정하여 생성하기

Chunk size를 지정하여 `Step`을 생성할 수 있습니다.

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

## Chunk-oriented Step 설정하기

Kotlin DSL에서는 `ChunkOrientedStepBuilder`에서 설정할 수 있는 기능을 모두 제공합니다.

### Annotation을 사용하여 Listener 설정하기

임의의 객체에 `@BeforeStep`, `@AfterStep` Annotation을 붙여서 Step Execution Listener 를 설정할 수 있습니다.

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

임의의 객체에 `@BeforeChunk`, `@AfterChunk`, `@AfterChunkError` Annotation을 붙여서 Chunk Listener 를 설정할 수 있습니다.

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

임의의 객체에 `@BeforeRead`, `@AfterRead`, `@OnReadError` Annotation을 붙여서 Item Reader Listener 를 설정할 수 있습니다.

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

임의의 객체에 `@BeforeProcess`, `@AfterProcess`, `@OnProcessError` Annotation을 붙여서 Item Process Listener 를 설정할 수 있습니다.

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

임의의 객체에 `@BeforeWrite`, `@AfterWrite`, `@OnWriteError` Annotation을 붙여서 Item Writer Listener 를 설정할 수 있습니다.

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

Annotation 기반의 Listener들은 한 객체에서 일부만 선택하여 사용 가능합니다.

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

### StepExecutionListener 객체를 사용하여 Listener 설정하기

`StepExecutionListener` 객체를 직접 인자로 넘겨서 Step Execution Listener 를 설정할 수 있습니다.

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

### ChunkListener 객체를 사용하여 Listener 설정하기

`ChunkListener` 객체를 직접 인자로 넘겨서 Chunk Listener 를 설정할 수 있습니다.

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

### ItemReadListener 객체를 사용하여 Listener 설정하기

`ItemReadListener` 객체를 직접 인자로 넘겨서 Item Read Listener 를 설정할 수 있습니다.

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

### ItemProcessListener 객체를 사용하여 Listener 설정하기

`ItemProcessListener` 객체를 직접 인자로 넘겨서 Item Process Listener 를 설정할 수 있습니다.

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

### ItemWriteListener 객체를 사용하여 Listener 설정하기

`ItemWriteListener` 객체를 직접 인자로 넘겨서 Item Write Listener 를 설정할 수 있습니다.

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

### Stream 설정하기

`ItemStream` 객체를 인자로 넘겨서 stream 를 설정할 수 있습니다.

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

### TaskExecutor 설정하기

`TaskExecutor` 객체를 인자로 넘겨서 chunk를 동시에 수행할 수 있습니다.

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

### TransactionAttribute 설정하기

`TransactionAttribute` 객체를 통해 Transaction 을 설정할 수 있습니다. 아래는 PROPAGATION_NOT_SUPPORTED 설정을 부여 함으로써 Transaction이 동작하지 않게 설정한 예시입니다.

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

### TransactionManager 설정하기

chunk-oriented step이 사용할 `PlatformTransactionManager`를 설정할 수 있습니다.

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

### StepInterruptionPolicy 설정하기

Step 수행 중 중단 여부를 판단하는 `StepInterruptionPolicy`를 설정할 수 있습니다.

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

### ObservationRegistry 설정하기

Step 수행에 대한 관측을 위해 `ObservationRegistry`를 설정할 수 있습니다.

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

## FaultTolerant 설정하기

Chunk-Oriented Step은 실패했을때의 동작을 정의할 수 있습니다. Kotlin DSL에서도 동일한 기능을 제공합니다.

### Annotation을 사용해서 SkipListener 설정하기

faultTolerant를 설정하는 경우 `@OnSkipInRead`, `@OnSkipInProcess`, `@OnSkipInWrite`에 기반하여 Skip에 대한 Listener를 설정할 수 있습니다.

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

### SkipListener 설정하기

`SkipListener` 객체를 사용하여 Skip에 대한 Listener를 설정할 수 있습니다.

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

### RetryListener 설정하기

`RetryListener` 객체를 사용하여 Retry에 대한 Listener를 설정할 수 있습니다.

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

### Retry Class, RetryLimit 설정하기

Retry 대상 Class, Retry Limit을 설정해서 특정 예외와 그 하위 클래스들에 대한 Retry를 할 수 있습니다.

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

### RetryPolicy 설정하기

`RetryPolicy`를 직접 지정해서 Retry 정책을 설정할 수 있습니다.

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

### Skip Class, SkipLimit 설정하기

Skip 대상 Class, Skip Limit을 설정해서 특정 예외와 그 하위 클래스들을 Skip할 수 있습니다.

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

### SkipPolicy 설정하기

`SkipPolicy`를 직접 설정해서 예외를 Skip할 수 있습니다.

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
