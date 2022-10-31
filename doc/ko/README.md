# Spring Batch Plus

Spring Batch Plus는 [Spring Batch](https://github.com/spring-projects/spring-batch)에서 사용할 수 있는 여러 유용한 class들을 제공합니다. 처음에 Kotlin DSL을 위한 repository로만 개발하려고 했으나 Spring Batch를 쓰면서 '아 이런게 있으면 좋겠다' 싶은 것들도 같이 모았습니다.

## 사용자 가이드

Kotlin DSL은 `JobBuilderFactory`, `StepBuilderFactory`, `FlowBuilder`를 이용하지 않고 Kotlin의 [Type-safe builder](https://kotlinlang.org/docs/type-safe-builders.html)를 이용하여 선언적으로 `Job`, `Step`, `Flow`를 선언할 수 있는 기능을 제공합니다. `ClearRunIdIncrementer`는 Spring Batch에서 제공하는 `RunIdIncrementer`를 대신하는 class로 이전의 JobExecution에 있는 JobParameter를 재사용하는 문제를 해결한 class 입니다. `DeleteMetadataJob`은 오래된 metadata를 삭제하는 기능을 제공해주는 `Job` 입니다. `ItemStreamReaderProcessorWriter`는 `ItemStreamReader`, `ItemProcessor`, `ItemStreamWriter`를 단일 class에서 정의할 수 있습니다.

- [Kotlin DSL](./configuration/kotlin-dsl/README.md)
- [ClearRunIdIncrementer](./job/clear-run-id-incrementer.md)
- [DeleteMetadataJob](./job/delete-metadata-job.md)
- [ItemStreamReaderProcessorWriter](./step/item-stream-reader-processor-writer.md)

## 예제 코드

[sample](../../spring-batch-plus-sample/)에 여러 케이스별로 샘플 코드를 모아놓았습니다.
