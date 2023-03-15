# Partition Step

- [Splitter 설정하기](#splitter-설정하기)
  - [StepExecutionSplitter 객체를 이용하기](#stepexecutionsplitter-객체를-이용하기)
  - [Spring Batch DSL 내부에서 설정하는 기능 활용하기](#spring-batch-dsl-내부에서-설정하는-기능-활용하기)
- [PartitionHandler 설정하기](#partitionhandler-설정하기)
  - [PartitionHandler 객체를 이용하기](#partitionhandler-객체를-이용하기)
  - [Spring Batch DSL 내부에서 설정하는 기능 활용하기](#spring-batch-dsl-내부에서-설정하는-기능-활용하기-1)
- [Aggregator 설정](#aggregator-설정)

Partition Step은 한 `Step`을 여러 Task로 나누어서 수행하는 `Step`입니다. Partition Step은 `Step`을 partition 단위로 분할하는 split 작업과 실제 partition을 가지고 작업을 수행하는 `PartitionHandler`를 설정하는 과정으로 이루어집니다.

## Splitter 설정하기

`StepExecutionSplitter`는 한 작업을 partition 단위로 분할하는 객체입니다. 이를 설정하는 방법에는 객체를 직접 주입하는 방법과 Spring Batch에서 DSL에서 내부에서 생성되는 객체를 이용하는 방법이 있습니다.

### StepExecutionSplitter 객체를 이용하기

`StepExecutionSplitter` 객체를 직접 인자로 넘겨서 설정할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val jobRepository: JobRepository
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                partitioner {
                    splitter(
                        object : StepExecutionSplitter {
                            override fun getStepName(): String = "workerStep"

                            override fun split(stepExecution: StepExecution, gridSize: Int): Set<StepExecution> {
                                val jobExecution = stepExecution.jobExecution
                                val stepExecutions = (0 until gridSize)
                                    .map {
                                        jobExecution.createStepExecution("$stepName:partition-$it")
                                    }
                                jobRepository.addAll(stepExecutions)
                                return stepExecutions.toSet()
                            }
                        }
                    )
                    partitionHandler {
                        taskExecutor(SimpleAsyncTaskExecutor())
                        step(actualStep())
                        gridSize(4)
                    }
                }
            }
        }
    }

    @Bean
    open fun actualStep(): Step = batch {
        step("actualStep") {
            tasklet(
                { contribution, _ ->
                    println("[${Thread.currentThread().name}][${contribution.stepExecution.stepName}] run actual tasklet")
                    RepeatStatus.FINISHED
                },
                ResourcelessTransactionManager()
            )
        }
    }
}
```

### Spring Batch DSL 내부에서 설정하는 기능 활용하기

`StepExecutionSplitter` 객체를 직접 이용하지 않고 Spring Batch DSL 내부에서 사용하는 `SimpleStepExecutionSplitter`를 이용하여 설정할 수도 있습니다. 다음의 코드는 `SimpleStepExecutionSplitter`의 stepName, split method를 설정하여 사용합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                partitioner {
                    // use SimpleStepExecutionSplitter internally
                    splitter("workerStep") { gridSize ->
                        (0 until gridSize).associate {
                            "partition-$it" to ExecutionContext()
                        }
                    }
                    partitionHandler {
                        taskExecutor(SimpleAsyncTaskExecutor())
                        step(testStep())
                        gridSize(4)
                    }
                }
            }
        }
    }

    @Bean
    open fun testStep(): Step = batch {
        step("actualStep") {
            tasklet(
                { contribution, _ ->
                    println("[${Thread.currentThread().name}][${contribution.stepExecution.stepName}] run actual tasklet")
                    RepeatStatus.FINISHED
                },
                ResourcelessTransactionManager()
            )
        }
    }
}
```

## PartitionHandler 설정하기

`PartitionHandler`는 split된 partition들을 실제로 수행하는 객체입니다. 객체를 직접 주입하거나 Spring Batch에서 DSL 내부에서 생성되는 객체를 이용하여 `PartitionHandler` 를 설정할 수 있습니다.

### PartitionHandler 객체를 이용하기

`PartitionHandler` 객체를 직접 인자로 넘겨서 설정이 가능합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                partitioner {
                    splitter("workerStep") { gridSize ->
                        (0 until gridSize).associate {
                            "partition-$it" to ExecutionContext()
                        }
                    }
                    partitionHandler(
                        TaskExecutorPartitionHandler().apply {
                            setTaskExecutor(SimpleAsyncTaskExecutor())
                            step = actualStep()
                            gridSize = 4
                        }
                    )
                }
            }
        }
    }

    @Bean
    open fun actualStep(): Step = batch {
        step("actualStep") {
            tasklet(
                { contribution, _ ->
                    println("[${Thread.currentThread().name}][${contribution.stepExecution.stepName}] run actual tasklet")
                    RepeatStatus.FINISHED
                },
                ResourcelessTransactionManager()
            )
        }
    }
}
```

### Spring Batch DSL 내부에서 설정하는 기능 활용하기

`PartitionHandler` 객체를 직접 이용하지 않고 Spring Batch DSL 내부에서 사용하는 `TaskExecutorPartitionHandler`를 이용하여 설정할 수도 있습니다. 다음의 코드는 `TaskExecutorPartitionHandler`의 taskExecutor, step, gridSize를 설정하여 사용합니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                partitioner {
                    splitter("workerStep") { gridSize ->
                        (0 until gridSize).associate {
                            "partition-$it" to ExecutionContext()
                        }
                    }
                    // use TaskExecutorPartitionHandler internally
                    partitionHandler {
                        taskExecutor(SimpleAsyncTaskExecutor())
                        step(actualStep())
                        gridSize(4)
                    }
                }
            }
        }
    }

    @Bean
    open fun actualStep(): Step = batch {
        step("actualStep") {
            tasklet(
                { contribution, _ ->
                    println("[${Thread.currentThread().name}][${contribution.stepExecution.stepName}] run actual tasklet")
                    RepeatStatus.FINISHED
                },
                ResourcelessTransactionManager()
            )
        }
    }
}
```

## Aggregator 설정

`StepExecutionAggregator`를 설정하여 실제 partition된 step들을 aggregation할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            step("testStep") {
                partitioner {
                    splitter("workerStep") { gridSize ->
                        (0 until gridSize).associate {
                            "partition-$it" to ExecutionContext()
                        }
                    }
                    partitionHandler {
                        taskExecutor(SimpleAsyncTaskExecutor())
                        step(testStep())
                        gridSize(4)
                    }
                    aggregator(DefaultStepExecutionAggregator())
                }
            }
        }
    }

    @Bean
    open fun testStep(): Step = batch {
        step("actualStep") {
            tasklet(
                { contribution, _ ->
                    println("[${Thread.currentThread().name}][${contribution.stepExecution.stepName}] run actual tasklet")
                    RepeatStatus.FINISHED
                },
                ResourcelessTransactionManager()
            )
        }
    }
}
```