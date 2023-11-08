# ItemStreamReaderProcessorWriter

- [processor를 포함하여 Tasklet 작성하기](#processor를-포함하여-tasklet-작성하기)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [Processor 없이 Tasklet 작성하기](#processor-없이-tasklet-작성하기)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)
- [Callback 사용하기](#callback-사용하기)
  - [Java](#java-2)
  - [Kotlin](#kotlin-2)

Spring 진영에서는 Reactive library로 [Reactor](https://projectreactor.io/)를 사용하고 있습니다. Reactor에서는 여러 데이터에 대한 stream을 `Flux`로 제공합니다. `Flux`로 읽은 데이터를 Spring Batch의 `ItemReader`에서 사용하기 위해서는 `Flux`에서 단일 Item씩 뽑아내서 리턴하는 작업이 필요합니다.

Spring Batch의 Chunk-oriented Step은 `ItemReader`, `ItemProcessor`, `ItmeWriter`로 구성됩니다. Spring Batch에서는 일반적으로 `ItemReader`, `ItemProcessor`, `ItmeWriter`를 각각 정의하고 이를 Step을 정의할 때 조립해서 사용합니다. 그런데 이 경우 `ItemReader`, `ItemProcessor`, `ItmeWriter`간에 데이터 공유가 힘들고 배치의 흐름을 알기 위해서는 `ItemReader`, `ItemProcessor`, `ItmeWriter`파일 각각을 살펴봐야 한다는 문제점이 있습니다. 또한 해당 클래스들이 재활용 되지 않는 케이스라면 Job의 응집도를 해치는 요소가 될 수 있습니다.

이 두가지 이슈를 해결하기 위해 `ItemStreamReaderProcessorWriter`는  `Flux`를 사용할 수 있는 Adaptor와 단일 파일에서 `ItemReader`, `ItemProcessor`, `ItmeWriter` 정의할 수 있는 기능을 제공핣니다.

Spring Batch Plus에서는 reactor를 compileOnly로 의존하기 때문에 `ItemStreamReaderProcessorWriter`를 사용하기 위해서는 직접 Reactor 의존성을 추가해야 합니다.

```kotlin
dependencies {
    implementation("io.projectreactor:reactor-core:${reactorVersion}")
}
```

## processor를 포함하여 Tasklet 작성하기

`ItemStreamReaderProcessorWriter`를 사용해서 단일 class에서 `ItemStreamReader`, `ItemProcessor`, `ItemStreamWriter`를 정의할 수 있습니다.

### Java

Java의 경우 `AdaptorFactory`를 이용해서 정의한 Tasklet을 `ItemStreamReader`, `ItemProcessor`, `ItemStreamWriter`로 변환하여 사용할 수 있습니다.

```java
@Component
@StepScope
class SampleTasklet implements ItemStreamReaderProcessorWriter<Integer, String> {

    @Value("#{jobParameters['totalCount']}")
    private long totalCount;

    private int count = 0;

    @NonNull
    @Override
    public Flux<Integer> readFlux(@NonNull ExecutionContext executionContext) {
        System.out.println("totalCount: " + totalCount);
        return Flux.generate(sink -> {
            if (count < totalCount) {
                sink.next(count);
                ++count;
            } else {
                sink.complete();
            }
        });
    }

    @Override
    public String process(@NonNull Integer item) {
        return "'" + item.toString() + "'";
    }

    @Override
    public void write(@NonNull Chunk<? extends String> chunk) {
        System.out.println(chunk.getItems());
    }
}
```

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        SampleTasklet sampleTasklet,
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .start(
                new StepBuilder("testStep", jobRepository)
                    .<Integer, String>chunk(3, transactionManager)
                    .reader(AdapterFactory.itemStreamReader(sampleTasklet))
                    .processor(AdapterFactory.itemProcessor(sampleTasklet))
                    .writer(AdapterFactory.itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

이 경우 `AdaptorFactory`의 method를 static import를 해서 사용하는게 미관상 보기 더 좋습니다.

```java
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemProcessor;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamWriter;

...

@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        SampleTasklet sampleTasklet,
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .start(
                new StepBuilder("testStep", jobRepository)
                    .<Integer, String>chunk(3, transactionManager)
                    .reader(itemStreamReader(sampleTasklet))
                    .processor(itemProcessor(sampleTasklet))
                    .writer(itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

### Kotlin

Kotlin에서는 extension function을 이용하여 정의한 Tasklet을 `ItemStreamReader`, `ItemProcessor`, `ItemStreamWriter`로 변환하여 사용할 수 있습니다.

```kotlin
@Component
@StepScope
open class SampleTasklet(
    @Value("#{jobParameters['totalCount']}") private var totalCount: Long
) : ItemStreamReaderProcessorWriter<Int, String> {
    private var count = 0

    override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
        println("totalCount: $totalCount")
        return Flux.generate { sink ->
            if (count < totalCount) {
                sink.next(count)
                ++count
            } else {
                sink.complete()
            }
        }
    }

    override fun process(item: Int): String? {
        return "'$item'"
    }

    override fun write(chunk: Chunk<out String>) {
        println(chunk.items)
    }
}
```

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    open fun testJob(
        sampleTasklet: SampleTasklet,
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(sampleTasklet.asItemStreamReader())
                    processor(sampleTasklet.asItemProcessor())
                    writer(sampleTasklet.asItemStreamWriter())
                }
            }
        }
    }
}
```

## Processor 없이 Tasklet 작성하기

Process 과정이 불필요하고 `ItemStreamReader` 와 `ItemStreamWriter` 만 필요하다면 `ItemStreamReaderWriter`를 상속하여 단일 class에서 `ItemStreamReader`, `ItemStreamWriter`를 정의할 수 있습니다.

### Java

Java의 경우 `AdaptorFactory`를 이용해서 정의한 Tasklet을 `ItemStreamReader`, `ItemStreamWriter`로 변환하여 사용할 수 있습니다.

```java
@Component
@StepScope
class SampleTasklet implements ItemStreamReaderWriter<Integer> {

    @Value("#{jobParameters['totalCount']}")
    private long totalCount;

    private int count = 0;

    @NonNull
    @Override
    public Flux<Integer> readFlux(@NonNull ExecutionContext executionContext) {
        System.out.println("totalCount: " + totalCount);
        return Flux.generate(sink -> {
            if (count < totalCount) {
                sink.next(count);
                ++count;
            } else {
                sink.complete();
            }
        });
    }

    @Override
    public void write(@NonNull Chunk<? extends Integer> chunk) {
        System.out.println(chunk.getItems());
    }
}
```

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        SampleTasklet sampleTasklet,
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .start(
                new StepBuilder("testStep", jobRepository)
                    .<Integer, Integer>chunk(3, transactionManager)
                    .reader(AdaptorFactory.itemStreamReader(sampleTasklet))
                    .writer(AdaptorFactory.itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

이 경우 `AdaptorFactory`의 method를 static import를 해서 사용하는게 미관상 보기 더 좋습니다.

```java
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamWriter;

...

@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        SampleTasklet sampleTasklet,
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .start(
                new StepBuilder("testStep", jobRepository)
                    .<Integer, Integer>chunk(3, transactionManager)
                    .reader(itemStreamReader(sampleTasklet))
                    .writer(itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

### Kotlin

Kotlin 사용시에는 Spring Batch Plus가 제공하는 extension function 을 사용하여 정의한 Tasklet을 `ItemStreamReader`, `ItemStreamWriter`로 편리하게 변환할 수 있습니다.

```Kotlin
@Component
@StepScope
open class SampleTasklet(
    @Value("#{jobParameters['totalCount']}") private var totalCount: Long
) : ItemStreamReaderWriter<Int> {
    private var count = 0

    override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
        println("totalCount: $totalCount")
        return Flux.generate { sink ->
            if (count < totalCount) {
                sink.next(count)
                ++count
            } else {
                sink.complete()
            }
        }
    }

    override fun write(chunk: Chunk<out Int>) {
        println(chunk.items)
    }
}
```

```Kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(
        sampleTasklet: SampleTasklet,
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3, transactionManager) {
                    reader(sampleTasklet.asItemStreamReader())
                    writer(sampleTasklet.asItemStreamWriter())
                }
            }
        }
    }
}
```

## Callback 사용하기

`ItemStreamReaderProcessorWriter`, `ItemStreamReaderWriter` 에는 `ItemStreamReader`, `ItemStreamWriter`의 `ItemStream`에 대한 callback method도 같이 정의할 수 있습니다. Callback method는 선택적으로 정의할 수 있습니다.

### Java

```java
@Component
@StepScope
public class SampleTasklet implements ItemStreamReaderProcessorWriter<Integer, String> {

    @Value("#{jobParameters['totalCount']}")
    private long totalCount;

    private int count = 0;

    @Override
    public void onOpenRead(@NonNull ExecutionContext executionContext) {
        System.out.println("onOpenRead");
    }

    @NonNull
    @Override
    public Flux<Integer> readFlux(@NonNull ExecutionContext executionContext) {
        System.out.println("totalCount: " + totalCount);
        return Flux.generate(sink -> {
            if (count < totalCount) {
                sink.next(count);
                ++count;
            } else {
                sink.complete();
            }
        });
    }

    @Override
    public void onUpdateRead(@NonNull ExecutionContext executionContext) {
        System.out.println("onUpdateRead");
    }

    @Override
    public void onCloseRead() {
        System.out.println("onCloseRead");
    }

    @Override
    public String process(@NonNull Integer item) {
        return "'" + item.toString() + "'";
    }

    @Override
    public void onOpenWrite(@NonNull ExecutionContext executionContext) {
        System.out.println("onOpenWrite");
    }

    @Override
    public void write(@NonNull Chunk<? extends String> chunk) {
        System.out.println(chunk.getItems());
    }

    @Override
    public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
        System.out.println("onUpdateWrite");
        executionContext.putString("samplekey", "samplevlaue");
    }

    @Override
    public void onCloseWrite() {
        System.out.println("onCloseWrite");
    }
}
```

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        SampleTasklet sampleTasklet,
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder("testJob", jobRepository)
            .start(
                new StepBuilder("testStep", jobRepository)
                    .<Integer, String>chunk(3, transactionManager)
                    .reader(itemStreamReader(sampleTasklet))
                    .processor(itemProcessor(sampleTasklet))
                    .writer(itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

### Kotlin

```kotlin
@Component
@StepScope
open class SampleTasklet(
    @Value("#{jobParameters['totalCount']}") private var totalCount: Long
) : ItemStreamReaderProcessorWriter<Int, String> {
    private var count = 0

    override fun onOpenRead(executionContext: ExecutionContext) {
        println("onOpenRead")
    }

    override fun readFlux(executionContext: ExecutionContext): Flux<Int> {
        println("totalCount: $totalCount")
        return Flux.generate { sink ->
            if (count < totalCount) {
                sink.next(count)
                ++count
            } else {
                sink.complete()
            }
        }
    }

    override fun onUpdateRead(executionContext: ExecutionContext) {
        println("onUpdateRead")
    }

    override fun onCloseRead() {
        println("onCloseRead")
    }

    override fun process(item: Int): String? {
        return "'$item'"
    }

    override fun onOpenWrite(executionContext: ExecutionContext) {
        println("onOpenWrite")
    }

    override fun write(chunk: Chunk<out String>) {
        println(chunk.items)
    }

    override fun onUpdateWrite(executionContext: ExecutionContext) {
        println("onUpdateWrite")
        executionContext.putString("samplekey", "samplevalue")
    }

    override fun onCloseWrite() {
        println("onCloseWrite")
    }
}
```

```kotlin
@Configuration
open class TestJobConfig(
    private val batch: BatchDsl,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    open fun testJob(
        sampleTasklet: SampleTasklet,
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3, transactionManager) {
                    reader(sampleTasklet.asItemStreamReader())
                    processor(sampleTasklet.asItemProcessor())
                    writer(sampleTasklet.asItemStreamWriter())
                }
            }
        }
    }
}
```