# Chunk-Oriented Step

- [Chunk Size를 지정하여 생성하기](#chunk-size를-지정하여-생성하기)
- [CompletionPolicy Policy를 지정하여 생성하기](#completionpolicy-policy를-지정하여-생성하기)
- [RepeatOperation을 통해 생성하기](#repeatoperation을-통해-생성하기)
- [Chunk-oriented Step 설정하기](#chunk-oriented-step-설정하기)
  - [Annotation을 사용하여 Listener 설정하기](#annotation을-사용하여-listener-설정하기)
  - [ChunkListener 객체를 사용하여 Listener 설정하기](#chunklistener-객체를-사용하여-listener-설정하기)
  - [ItemReadListener 객체를 사용하여 Listener 설정하기](#itemreadlistener-객체를-사용하여-listener-설정하기)
  - [ItemProcessListener 객체를 사용하여 Listener 설정하기](#itemprocesslistener-객체를-사용하여-listener-설정하기)
  - [ItemWriteListener 객체를 사용하여 Listener 설정하기](#itemwritelistener-객체를-사용하여-listener-설정하기)
  - [Stream 설정하기](#stream-설정하기)
  - [TaskExecutor 설정하기](#taskexecutor-설정하기)
  - [ThrottleLimit 설정하기](#throttlelimit-설정하기)
  - [ExceptionHandler 설정하기](#exceptionhandler-설정하기)
  - [RepeatOperations 설정하기](#repeatoperations-설정하기)
  - [TransactionAttribute 설정하기](#transactionattribute-설정하기)
- [FaultTolerant 설정하기](#faulttolerant-설정하기)
  - [Annotation을 사용해서 SkipListener 설정하기](#annotation을-사용해서-skiplistener-설정하기)
  - [SkipListener 설정하기](#skiplistener-설정하기)
  - [RetryListener 설정하기](#retrylistener-설정하기)
  - [KeyGenerator 설정하기](#keygenerator-설정하기)
  - [Retry Class, RetryLimit 설정하기](#retry-class-retrylimit-설정하기)
  - [noRetry Class 설정하기](#noretry-class-설정하기)
  - [RetryPolicy 설정하기](#retrypolicy-설정하기)
  - [BackOffPolicy 설정하기](#backoffpolicy-설정하기)
  - [RetryContextCache 설정하기](#retrycontextcache-설정하기)
  - [Skip Class, SkipLimit 설정하기](#skip-class-skiplimit-설정하기)
  - [noSkip Class 설정하기](#noskip-class-설정하기)
  - [SkipPolicy 설정하기](#skippolicy-설정하기)
  - [noRollback Class 설정하기](#norollback-class-설정하기)
  - [processorNonTransactional 설정하기](#processornontransactional-설정하기)

Chunk-Oriented Step은 `ItemReader`, `ItemProcessor`, `ItemWriter`로 구성된 `Step`입니다.

## Chunk Size를 지정하여 생성하기

Chunk size를 지정하여 `Step`을 생성할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
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

## CompletionPolicy Policy를 지정하여 생성하기

CompletionPolicy를 직접 지정하여 `Step`을 생성할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(SimpleCompletionPolicy(3)) {
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

## RepeatOperation을 통해 생성하기

`RepeatOperations`를 직접 지정하여 `Step`을 생성할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                val repeatOperations = RepeatTemplate().apply {
                    setCompletionPolicy(SimpleCompletionPolicy(3))
                }
                chunk<Int, String>(repeatOperations) {
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

## Chunk-oriented Step 설정하기

Kotlin DSL에서는 `SimpleStepBuilder`에서 설정할 수 있는 기능을 모두 제공합니다.

### Annotation을 사용하여 Listener 설정하기

임의의 객체에 `@BeforeChunk`, `@AfterChunk`, `@AfterChunkError` Annotation을 붙여서 Chunk Listener 를 설정할 수 있습니다.

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

임의의 객체에 `@BeforeRead`, `@AfterRead`, `@OnReadError` Annotation을 붙여서 Item Reader Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

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
        }
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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

임의의 객체에 `@BeforeProcess`, `@AfterProcess`, `@OnProcessError` Annotation을 붙여서 Item Writer Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

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
        }
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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

임의의 객체에 `@BeforeWrite`, `@AfterWrite`, `@OnWriteError` Annotation을 붙여서 Item Writer Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    class TestListener {
        @BeforeWrite
        fun beforeWrite(items: List<String>) {
            println("beforeWrite: $items")
        }

        @AfterWrite
        fun afterWrite(items: List<String>) {
            println("afterWrite: $items")
        }

        @OnWriteError
        fun onWriteError(exception: Exception, items: List<String>) {
        }
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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

Annotation 기반의 Listener들은 한 객체에서 선택적으로 붙여서도 사용 가능합니다.

```kotlin
@Configuration
open class TestJobConfig {

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
        fun beforeWrite(items: List<String>) {
            println("beforeWrite: $items")
        }
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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

### ChunkListener 객체를 사용하여 Listener 설정하기

`ChunkListener` 객체를 직접 인자로 넘겨서 Chunk Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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
                        }
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

### ItemReadListener 객체를 사용하여 Listener 설정하기

`ItemReadListener` 객체를 직접 인자로 넘겨서 Item Read Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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
                        }
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

### ItemProcessListener 객체를 사용하여 Listener 설정하기

`ItemProcessListener` 객체를 직접 인자로 넘겨서 Item Process Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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

                            override fun afterProcess(item: Int, result: String?) {
                                println("afterProcess: $item, result: $result")
                            }

                            override fun onProcessError(item: Int, e: Exception) {
                            }
                        }
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

### ItemWriteListener 객체를 사용하여 Listener 설정하기

`ItemWriteListener` 객체를 직접 인자로 넘겨서 Item Write Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    listener(
                        object : ItemWriteListener<String> {
                            override fun beforeWrite(items: List<String>) {
                                println("beforeWrite: $items")
                            }

                            override fun afterWrite(items: List<String>) {
                                println("afterWrite: $items")
                            }

                            override fun onWriteError(exception: Exception, items: List<String>) {
                            }
                        }
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

### Stream 설정하기

`ItemStream` 객체를 인자로 넘겨서 stream 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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
                        }
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

### TaskExecutor 설정하기

`TaskExecutor` 객체를 인자로 넘겨서 chunk를 동시에 수행할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun customExecutor(): TaskExecutor {
        return SimpleAsyncTaskExecutor()
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
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

### ThrottleLimit 설정하기

`TaskExecutor`를 지정하고 throttlelimit 설정을 해서 동시에 수행될 chunk의 최대 수를 지정할 수 있습니다. 다음은 `TaskExecutor`를 설정했으나 throttlelimit를 한개만 지정해서 사실상 단일 `TaskExecutor`에서 동작하게 되는 예시입니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun customExecutor(): TaskExecutor {
        return SimpleAsyncTaskExecutor()
    }

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3) {
                    reader(testItemReader())
                    writer(testItemWriter())
                    taskExecutor(customExecutor())
                    throttleLimit(1)
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

### ExceptionHandler 설정하기

`ExceptionHandler` 객체를 통해 Exception handler 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3) {
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

Kotlin의 trailing lambda 기능을 사용하면 보다 간단하게 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3) {
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

### RepeatOperations 설정하기

`RepeatOperations` 객체를 통해 RepeatOperation 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3) {
                    reader(testItemReader())
                    writer(testItemWriter())
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

### TransactionAttribute 설정하기

`TransactionAttribute` 객체를 통해 Transaction 을 설정할 수 있습니다. 아래는 PROPAGATION_NOT_SUPPORTED 설정을 부여 함으로써 Transaction이 동작하지 않게 설정한 예시입니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3) {
                    reader(testItemReader())
                    writer(testItemWriter())
                    transactionAttribute(DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_NOT_SUPPORTED))
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
            val actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive()
            println("write $items (actualTransactionActive: $actualTransactionActive)")
        }
    }
}
```

## FaultTolerant 설정하기

Chunk-Oriented Step은 실패했을때의 동작을 정의할 수 있습니다. Kotlin DSL에서도 동일한 기능을 제공합니다.

### Annotation을 사용해서 SkipListener 설정하기

faultTolerant를 설정하는 경우 `@OnSkipInRead`, `@OnSkipInProcess`, `@OnSkipInWrite`에 기반하여 Skip에 대한 Listener를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    class TestListener {

        @OnSkipInRead
        fun onSkipInRead(t: Throwable) {
            println("Ignore exception of read (exception: ${t.message})")
        }

        @OnSkipInProcess
        fun onSkipInProcess(item: Any, t: Throwable) {
        }

        @OnSkipInWrite
        fun onSkipInWrite(item: Any, t: Throwable) {
        }
    }

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### SkipListener 설정하기

`SkipListener` 객체를 사용하여 Skip에 대한 Listener를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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
                            }
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

### RetryListener 설정하기

`RetryListener` 객체를 사용하여 Retry에 대한 Listener를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        listener(
                            object : RetryListener {
                                override fun <T : Any?, E : Throwable?> open(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?
                                ): Boolean {
                                    println("RetryListener::open (context: $context")
                                    return true
                                }

                                override fun <T : Any?, E : Throwable?> close(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                    throwable: Throwable?
                                ) {
                                    println("RetryListener::close (error: ${throwable?.message})")
                                }

                                override fun <T : Any?, E : Throwable?> onError(
                                    context: RetryContext?,
                                    callback: RetryCallback<T, E>?,
                                    throwable: Throwable?
                                ) {
                                    println("RetryListener::onError (error: ${throwable?.message})")
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

### KeyGenerator 설정하기

`KeyGenerator` 객체를 사용하여 Retry할 때 item을 식별할 수 있는 key를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

Kotlin의 trailing lambda를 활용하여 보다 간략하게 표현 가능합니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### Retry Class, RetryLimit 설정하기

Retry 대상 Class, Retry Limit을 설정해서 특정 예외와 그 하위 클래스들에 대한 Retry를 할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### noRetry Class 설정하기

Retry 대상 Class를 설정 후 특정 예외는 Retry 대상에서 제외하고 싶은 경우 noRetry Class를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### RetryPolicy 설정하기

`RetryPolicy`를 직접 지정해서 Retry 정책을 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### BackOffPolicy 설정하기

`BackOffPolicy`를 지정해서 Retry가 발생한 경우 backoff를 지정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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
                            }
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

### RetryContextCache 설정하기

`RetryContextCache`를 지정해서 Retry하는 경우 내부 Context의 Cache를 지정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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
                            }
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

### Skip Class, SkipLimit 설정하기

Skip 대상 Class, Skip Limit을 설정해서 특정 예외와 그 하위 클래스들을 Skip할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### noSkip Class 설정하기

Skip 대상 Class를 설정 후 특정 예외는 Skip 대상에서 제외하고 싶은 경우 noSkip 설정을 통해 할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### SkipPolicy 설정하기

`SkipPolicy`를 직접 설정해서 예외를 Skip할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
                    reader(testItemReader())
                    processor(testItemProcessor())
                    writer(testItemWriter())
                    faultTolerant {
                        skipPolicy(
                            LimitCheckingItemSkipPolicy(
                                4,
                                mapOf(RuntimeException::class.java to true)
                            )
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

### noRollback Class 설정하기

noRollback 을 설정하여 에러가 발생해도 롤백하지 않고 진행할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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

### processorNonTransactional 설정하기

processorNonTransactional 을 설정하여 retry를 할 때 process에서 이전에 처리한 데이터를 cache할지 지정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(batch: BatchDsl): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
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
