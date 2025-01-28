package com.navercorp.spring.batch.plus.sample

import org.junit.jupiter.api.Test

class RunSamplesTest {

    @Test
    fun all() {
        com.navercorp.spring.batch.plus.sample.clear.main()
        com.navercorp.spring.batch.plus.sample.clearwithid.main()
    }
}
