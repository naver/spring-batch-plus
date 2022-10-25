# Tasklet Step

- [Tasklet을 변수로 넘기기](#tasklet을-변수로-넘기기)
- [Trailing Lambda 를 통해 Tasklet을 정의하기](#trailing-lambda-를-통해-tasklet을-정의하기)
- [Bean 이름으로 Tasklet을 가져오기](#bean-이름으로-tasklet을-가져오기)
- [Tasklet Step 설정하기](#tasklet-step-설정하기)
  - [Annotation을 사용하여 Listener 설정하기](#annotation을-사용하여-listener-설정하기)
  - [ChunkListener 객체를 사용하여 Listener 설정하기](#chunklistener-객체를-사용하여-listener-설정하기)
  - [Stream 설정하기](#stream-설정하기)
  - [TaskExecutor 설정하기](#taskexecutor-설정하기)
  - [ExceptionHandler 설정하기](#exceptionhandler-설정하기)
  - [RepeatOperations 설정하기](#repeatoperations-설정하기)
  - [TransactionAttribute 설정하기](#transactionattribute-설정하기)

Tasklet Step은 단일 `Tasklet`으로 구성된 `Step`입니다.

## Tasklet을 변수로 넘기기

`Tasklet`을 미리 정의해 두 변수로 넘겨서 `Step`을 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet())
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

## Trailing Lambda 를 통해 Tasklet을 정의하기

`Step`을 정의할 때 trailing lambda를 통해 `Tasklet`을 정의할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet { _, _ ->
                    println("run testTasklet")
                    RepeatStatus.FINISHED
                }
            }
        }
    }
}
```

## Bean 이름으로 Tasklet을 가져오기

`Tasklet`을 bean으로 미리 정의해 두고 이름으로 가져올 수도 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                taskletBean("testTasklet")
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

이 방식에서는 kotlin의 nullability를 잘 활용할 수 있습니다. 이 방식으로 사용하지 않으면 testTasklet을 method 호출로 넘겨줘야 하는데 이 경우 인자로 null을 넣어야 합니다. `@StepScope`를 통해 Proxy로 생성되어서 실제 `Step`이 수행될 때 인자가 바인딩 되기 때문에 null을 넣어도 문제는 없으나 kotlin에서 강제로 nullable로 선언해줘야 하는 문제가 있습니다. Spring Batch Plus에서 제공하는 Kotlin DSL 기능을 활용하면 빈 이름으로 쉽게 적용이 가능해서 이런 non-nullable로 선언이 가능합니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(null))
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

## Tasklet Step 설정하기

Kotlin DSL에서는 `TaskletStepBuilder`에서 설정할 수 있는 기능을 모두 제공합니다.

### Annotation을 사용하여 Listener 설정하기

임의의 객체에 `@BeforeChunk`, `@AfterChunk`, `@AfterChunkError` Annotation을 붙여서 Chunk Listener 를 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    class TestListener {
        @BeforeChunk
        fun beforeChunk() {
            println("beforeChunk")
        }

        @AfterChunk
        fun afterChunk() {
            println("afterChunk")
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
                tasklet(testTasklet()) {
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
                tasklet(testTasklet()) {
                    listener(
                        object : ChunkListener {
                            override fun beforeChunk(context: ChunkContext) {
                                println("beforeChunk")
                            }

                            override fun afterChunk(context: ChunkContext) {
                                println("afterChunk")
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

### Stream 설정하기

`ItemStream` 객체를 인자로 넘겨서 stream 을 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet()) {
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

### TaskExecutor 설정하기

`TaskExecutor` 객체를 인자로 넘겨서 Executor 를 설정할 수 있습니다.

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
                tasklet(testTasklet()) {
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
                tasklet(testTasklet()) {
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
                tasklet(testTasklet()) {
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
                tasklet(testTasklet()) {
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

### TransactionAttribute 설정하기

`TransactionAttribute` 객체를 통해 transaction 을 설정할 수 있습니다. 아래의 예시는 PROPAGATION_NOT_SUPPORTED 설정을 부여 함으로써 transaction이 동작하지 않게 한 예시입니다.

```kotlin
@Configuration
open class TestJobConfig {

    @Bean
    open fun testJob(
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet()) {
                    transactionAttribute(DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_NOT_SUPPORTED))
                }
            }
        }
    }

    @Bean
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        // print false
        val actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive()
        println("run testTasklet (transaction active: $actualTransactionActive}")
        RepeatStatus.FINISHED
    }
}
```
