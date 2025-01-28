package com.navecorp.spring.batch.plus.sample;

import org.junit.jupiter.api.Test;

public class RunSamplesTest {

	@Test
	void all() throws Exception {
		com.navercorp.spring.batch.plus.sample.clear.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.clearwithid.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.comparison.bad.BatchApplication.main(new String[0]);
		com.navercorp.spring.batch.plus.sample.comparison.good.BatchApplication.main(new String[0]);
	}
}
