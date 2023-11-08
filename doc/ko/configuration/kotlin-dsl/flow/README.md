# Flow

- [Flow를 생성하기](#flow를-생성하기)

`Flow`는 `Job`의 흐름을 정의합니다. `Flow`는 [Job Flow 설정](../job/README.md#job-flow-설정)처럼 `Job`을 정의할 때 생성할 수 있지만 직접 다른 객체로 생성할 수 있습니다. `Flow`를 다른 객체로 생성하는것 이외에는 `Job`와 동작이 동일하므로 순차 수행, 분기 수행 등 상세한 사용법은 [Job Flow 설정](../job/README.md#job-flow-설정)을 참고바랍니다.

## Flow를 생성하기

`BatchDsl`을 사용하여 `Flow`를 생성할 수 있습니다.

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(): Job = batch {
        job("testJob") {
            flow(testFlow())
        }
    }

    @Bean
    open fun testFlow(): Flow = batch {
        flow("testFlow") {
            step(testStep()) {
                on("COMPLETED") {
                    stop()
                }
                on("*") {
                    fail()
                }
            }
        }
    }

    @Bean
    open fun testStep(): Step = batch {
        step("testStep") {
            tasklet({ _, _ -> RepeatStatus.FINISHED }, transactionManager)
        }
    }
}
```