# Partition Step

- [Set a splitter](#set-a-splitter)
  - [Use a StepExecutionSplitter object](#use-a-stepexecutionsplitter-object)
  - [Use a SimpleStepExecutionSplitter](#use-a-simplestepexecutionsplitter)
- [Set a PartitionHandler](#set-a-partitionhandler)
  - [Use a PartitionHandler object](#use-a-partitionhandler-object)
  - [Use a TaskExecutorPartitionHandler](#use-a-taskexecutorpartitionhandler)
- [Set an aggregator](#set-an-aggregator)

A partition step partitions a `Step` into multiple tasks and runs them. It is subdivided into the following two processes: one is splitting a `Step` into partitions and the other is setting a `PartitionHandler` that executes the step with the partitions.

## Set a splitter

A `StepExecutionSplitter` object splits the step execution into partitions. To set a splitter, you can either directly inject an object or use an object that is created inside the Spring Batch DSL.

### Use a StepExecutionSplitter object

You can pass a `StepExecutionSplitter` object as an argument.

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

### Use a SimpleStepExecutionSplitter

Instead of using a `StepExecutionSplitter` object, you can use a `SimpleStepExecutionSplitter` that is used in the Spring Batch DSL. The following example sets the stepName and split method of `SimpleStepExecutionSplitter`.

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

## Set a PartitionHandler

A `PartitionHandler` object executes split partitions. To set a `PartitionHandler`, you can either directly inject an object or use an object that is created inside the Spring Batch DSL.

### Use a PartitionHandler object

You can pass a `PartitionHandler` object as an argument.

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

### Use a TaskExecutorPartitionHandler

Instead of using a `PartitionHandler` object, you can use a `TaskExecutorPartitionHandler` that is used in the Spring Batch DSL. The following example sets the taskExecutor, step, and gridSize of `TaskExecutorPartitionHandler`.

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

## Set an aggregator

You can set a `StepExecutionAggregator` to aggregate partitioned steps.

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
