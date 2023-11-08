# Tasklet Step

- [Tasklet을 변수로 넘기기](#tasklet을-변수로-넘기기)
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager)
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

## Bean 이름으로 Tasklet을 가져오기

`Tasklet`을 bean으로 미리 정의해 두고 이름으로 가져올 수도 있습니다.

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
                taskletBean("testTasklet", transactionManager)
            }
        }
    }

    @Bean
    @StepScope
    open fun testTasklet(
        @Value("#{jobParameters['param']}") paramValue: String,
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
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

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
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                tasklet(testTasklet(), transactionManager) {
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
    open fun testTasklet(): Tasklet = Tasklet { _, _ ->
        // print false
        val transactionName = TransactionSynchronizationManager.getCurrentTransactionName()
        println("run testTasklet (transactionName: $transactionName}")
        RepeatStatus.FINISHED
    }
}
```
