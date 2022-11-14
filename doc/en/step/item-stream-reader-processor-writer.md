# ItemStreamReaderProcessorWriter

- [Create a tasklet with a processor](#create-a-tasklet-with-a-processor)
  - [Java](#java)
  - [Kotlin](#kotlin)
- [Create a tasklet without a processor](#create-a-tasklet-without-a-processor)
  - [Java](#java-1)
  - [Kotlin](#kotlin-1)
- [Use a callback](#use-a-callback)
  - [Java](#java-2)
  - [Kotlin](#kotlin-2)

Spring uses a reactive library called [Reactor](https://projectreactor.io/), which provides streams for various types of data using `Flux`. For `ItemReader` in Spring Batch to use the data read using `Flux`, you need to extract each item from `Flux` and return it.

A chunk-oriented step in Spring Batch consists of `ItemReader`, `ItemProcessor`, and `ItemWriter`, which are usually defined separately and then assembled to define a step. However, there are some issues with this approach: it is difficult to share data between `ItemReader`, `ItemProcessor`, and `ItemWriter`, and you need to see each respective file to understand the batch flow. Also, if the classes are not reused, they can make the elements of a job less coherent.

To resolve such issues, `ItemStreamReaderProcessorWriter` provides an adaptor to use `Flux` and helps you define `ItemReader`, `ItemProcessor`, and `ItemWriter` in a single file.

Because Spring Batch Plus has compileOnly dependencies on Reactor, you need to manually add them to use `ItemStreamReaderProcessorWriter`.

```kotlin
dependencies {
    implementation("io.projectreactor:reactor-core:${reactorVersion}")
}
```

## Create a tasklet with a processor

You can use `ItemStreamReaderProcessorWriter` to define `ItemStreamReader`, `ItemProcessor`, and `ItemStreamWriter` in a single class.

### Java

In Java, you can convert a tasklet defined using `AdaptorFactory` to `ItemStreamReader`, `ItemProcessor`, and `ItemStreamWriter`.

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
    public void write(@NonNull List<? extends String> items) {
        System.out.println(items);
    }
}

```

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        SampleTasklet sampleTasklet
    ) {
        return jobBuilderFactory.get("testJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .<Integer, String>chunk(3)
                    .reader(AdaptorFactory.itemStreamReader(sampleTasklet))
                    .processor(AdaptorFactory.itemProcessor(sampleTasklet))
                    .writer(AdaptorFactory.itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

You can statically import the method of `AdaptorFactory` for better readability.

```java
...
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemProcessor;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamWriter;
...

@Configuration
public class TestJobConfig {

    @Bean
    Job testJob(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        SampleTasklet sampleTasklet
    ) {
        return jobBuilderFactory.get("testJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .<Integer, String>chunk(3)
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

In Kotlin, you can convert a tasklet defined using an extension function to `ItemStreamReader`, `ItemProcessor`, and `ItemStreamWriter`.

```kotlin
@Component
@StepScope
class SampleTasklet(
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

    override fun write(items: List<String>) {
        println(items)
    }
}
```

```kotlin
@Configuration
class TestJobConfig {

    @Bean
    fun testJob(
        sampleTasklet: SampleTasklet,
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, String>(3) {
                    reader(sampleTasklet.asItemStreamReader())
                    processor(sampleTasklet.asItemProcessor())
                    writer(sampleTasklet.asItemStreamWriter())
                }
            }
        }
    }
}
```

## Create a tasklet without a processor

If you need only `ItemStreamReader` and `ItemStreamWriter` without a processor, you can inherit `ItemStreamReaderWriter` to define `ItemStreamReader` and `ItemStreamWriter` in a single class.

### Java

In Java, you can convert a tasklet defined using `AdaptorFactory` to `ItemStreamReader` and `ItemStreamWriter`.

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
    public void write(@NonNull List<? extends Integer> items) {
        System.out.println(items);
    }
}
```

```java
@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        SampleTasklet sampleTasklet
    ) {
        return jobBuilderFactory.get("testJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .<Integer, Integer>chunk(3)
                    .reader(AdaptorFactory.itemStreamReader(sampleTasklet))
                    .writer(AdaptorFactory.itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

You can statically import the method of `AdaptorFactory` for better readability.

```java
...
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamReader;
import static com.navercorp.spring.batch.plus.item.AdaptorFactory.itemStreamWriter;
...

@Configuration
public class TestJobConfig {

    @Bean
    public Job testJob(
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        SampleTasklet sampleTasklet
    ) {
        return jobBuilderFactory.get("testJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .<Integer, Integer>chunk(3)
                    .reader(itemStreamReader(sampleTasklet))
                    .writer(itemStreamWriter(sampleTasklet))
                    .build()
            )
            .build();
    }
}
```

### Kotlin

In Kotlin, you can easily convert a tasklet defined using an extension function in Spring Batch Plus to `ItemStreamReader` and `ItemStreamWriter`.

```Kotlin
@Component
@StepScope
class SampleTasklet(
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

    override fun write(items: List<Int>) {
        println(items)
    }
}
```

```Kotlin
@Configuration
class TestJobConfig {

    @Bean
    fun testJob(
        sampleTasklet: SampleTasklet,
        batch: BatchDsl
    ): Job = batch {
        job("testJob") {
            step("testStep") {
                chunk<Int, Int>(3) {
                    reader(sampleTasklet.asItemStreamReader())
                    writer(sampleTasklet.asItemStreamWriter())
                }
            }
        }
    }
}
```

## Use a callback

You can define a callback method for `ItemStream` of `ItemStreamWriter` in `ItemStreamReaderProcessorWriter` and `ItemStreamReaderWriter`. You can selectively define a callback method.

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
        Object beforeLastReadCount = executionContext.get("lastReadCount");
        long afterLastReadCount = this.count;
        System.out.printf("onUpdateWrite (lastReadCount: %d -> %d)%n", beforeLastReadCount, afterLastReadCount);
        executionContext.put("lastReadCount", afterLastReadCount);
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
    public void write(@NonNull List<? extends String> items) {
        System.out.println(items);
    }

    @Override
    public void onUpdateWrite(@NonNull ExecutionContext executionContext) {
        Object beforeLastWriteCount = executionContext.get("lastWriteCount");
        long afterLastWriteCount = this.count;
        System.out.printf("onUpdateWrite (lastWriteCount: %d -> %d)%n", beforeLastWriteCount, afterLastWriteCount);
        executionContext.put("lastWriteCount", afterLastWriteCount);
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
        JobBuilderFactory jobBuilderFactory,
        StepBuilderFactory stepBuilderFactory,
        SampleTasklet sampleTasklet
    ) {
        return jobBuilderFactory.get("testJob")
            .start(
                stepBuilderFactory.get("testStep")
                    .<Integer, String>chunk(3)
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
        val beforeLastReadCount = executionContext["lastReadCount"]
        val afterLastReadCount = count.toLong()
        System.out.printf("onUpdateWrite (lastReadCount: %d -> %d)%n", beforeLastReadCount, afterLastReadCount)
        executionContext.put("lastReadCount", afterLastReadCount)
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

    override fun write(items: List<String>) {
        println(items)
    }

    override fun onUpdateWrite(executionContext: ExecutionContext) {
        val beforeLastWriteCount = executionContext["lastWriteCount"]
        val afterLastWriteCount = count.toLong()
        System.out.printf("onUpdateWrite (lastWriteCount: %d -> %d)%n", beforeLastWriteCount, afterLastWriteCount)
        executionContext.put("lastWriteCount", afterLastWriteCount)
    }

    override fun onCloseWrite() {
        println("onCloseWrite")
    }
}
```
