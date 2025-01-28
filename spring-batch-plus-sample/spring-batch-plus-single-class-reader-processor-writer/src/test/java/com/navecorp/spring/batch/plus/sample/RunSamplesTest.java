package com.navecorp.spring.batch.plus.sample;

import org.junit.jupiter.api.Test;

public class RunSamplesTest {

	@Test
	void all() throws Exception {
		com.navercorp.spring.batch.plus.sample.flux.callback.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.flux.readerprocessorwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.flux.readerwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.iterable.callback.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.iterable.readerprocessorwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.iterable.readerwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.iterator.callback.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.iterator.readerprocessorwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.iterator.readerwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.simple.callback.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.simple.readerprocessorwriter.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.simple.readerwriter.BatchApplication.main(new String[0]);
	}
}
