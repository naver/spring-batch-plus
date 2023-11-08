# Flow Step

- [Flow를 변수로 넘기기](#flow를-변수로-넘기기)
- [Step을 정의할 때 Flow를 초기화하기](#step을-정의할-때-flow를-초기화하기)
- [Bean 이름으로 Flow를 가져오기](#bean-이름으로-flow를-가져오기)

Flow Step 이란 하나의 다른 `Flow`으로 이루어진 `Step`입니다.

## Flow를 변수로 넘기기

`Flow`을 미리 정의해 두고 변수로 넘겨서 `Step`을 정의할 수 있습니다.

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
                flow(anotherFlow())
            }
        }
    }

    @Bean
    open fun anotherFlow(): Flow = batch {
        flow("anotherFlow") {
            step("anotherFlowStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```

## Step을 정의할 때 Flow를 초기화하기

`Step`을 정의 할 때 `Flow`를 초기화 하여 사용할 수 있습니다.

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
                flow("anotherFlow") {
                    step("anotherFlowStep") {
                        tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
                    }
                }
            }
        }
    }
}
```

## Bean 이름으로 Flow를 가져오기

`Flow`를 미리 Bean 으로 정의해 두고 Bean 이름을 통해 사용할 `Flow`를 가져올 수 있습니다.

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
                flowBean("anotherFlow")
            }
        }
    }

    @Bean
    open fun anotherFlow(): Flow = batch {
        flow("anotherFlow") {
            step("anotherFlowStep") {
                tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
            }
        }
    }
}
```