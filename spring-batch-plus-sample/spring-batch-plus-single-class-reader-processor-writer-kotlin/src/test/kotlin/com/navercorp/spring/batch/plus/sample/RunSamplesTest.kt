package com.navercorp.spring.batch.plus.sample

import org.junit.jupiter.api.Test

class RunSamplesTest {

    @Test
    fun all() {
        com.navercorp.spring.batch.plus.sample.flux.callback.main()
        com.navercorp.spring.batch.plus.sample.flux.readerprocessorwriter.main()
        com.navercorp.spring.batch.plus.sample.flux.readerwriter.main()
        com.navercorp.spring.batch.plus.sample.iterable.callback.main()
        com.navercorp.spring.batch.plus.sample.iterable.readerprocessorwriter.main()
        com.navercorp.spring.batch.plus.sample.iterable.readerwriter.main()
        com.navercorp.spring.batch.plus.sample.iterator.callback.main()
        com.navercorp.spring.batch.plus.sample.iterator.readerprocessorwriter.main()
        com.navercorp.spring.batch.plus.sample.iterator.readerwriter.main()
        com.navercorp.spring.batch.plus.sample.simple.callback.main()
        com.navercorp.spring.batch.plus.sample.simple.readerprocessorwriter.main()
        com.navercorp.spring.batch.plus.sample.simple.readerwriter.main()
    }
}
