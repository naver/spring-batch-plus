package com.navercorp.spring.batch.plus.sample

import org.junit.jupiter.api.Test

class RunSamplesTest {

    @Test
    fun comparison() {
        com.navercorp.spring.batch.plus.sample.comparison.after.main()
        com.navercorp.spring.batch.plus.sample.comparison.before.main()
    }

    @Test
    fun flowCreation() {
        com.navercorp.spring.batch.plus.sample.flow.creation.main()
    }

    @Test
    fun jobConfiguration() {
        com.navercorp.spring.batch.plus.sample.job.configuration.incrementer.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.listenerannotation.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.listenerobject.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.meterregistry.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.observationconvention.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.observationregistry.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.preventrestart.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.repository.main()
        com.navercorp.spring.batch.plus.sample.job.configuration.validator.main()
    }

    @Test
    fun jobCreation() {
        com.navercorp.spring.batch.plus.sample.job.creation.main()
    }

    @Test
    fun jobFlow() {
        com.navercorp.spring.batch.plus.sample.job.flow.decider.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.decider.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.flow.plain.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.flow.plain.init.main()
        com.navercorp.spring.batch.plus.sample.job.flow.flow.plain.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.flow.transition.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.flow.transition.init.main()
        com.navercorp.spring.batch.plus.sample.job.flow.flow.transition.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.comparison.after.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.comparison.before.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.plain.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.plain.init.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.plain.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.transition.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.transition.init.main()
        com.navercorp.spring.batch.plus.sample.job.flow.step.transition.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.decider.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.decider.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.finish.end.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.finish.fail.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.finish.stop.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.flow.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.flow.init.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.flow.nested.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.flow.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.step.bean.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.step.init.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.step.nested.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.step.variable.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.stopandrestart.decider.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.stopandrestart.flow.main()
        com.navercorp.spring.batch.plus.sample.job.flow.transition.stopandrestart.step.main()
    }

    @Test
    fun jobSplit() {
        com.navercorp.spring.batch.plus.sample.job.split.bean.main()
        com.navercorp.spring.batch.plus.sample.job.split.init.main()
        com.navercorp.spring.batch.plus.sample.job.split.variable.main()
    }

    @Test
    fun chunkOrientedStep() {
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.chunkpolicy.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.chunksize.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.annotationlistener.chunklistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.annotationlistener.mixed.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.annotationlistener.processorlistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.annotationlistener.readerlistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.annotationlistener.writerlistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.chunklistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.exceptionhandler.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.executor.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.processorlistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.readerlistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.stepoperation.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.stream.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.transactionattribute.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.config.writerlistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.backoffpolicy.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.keygenerator.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.noretry.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.noskip.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.processornontx.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.retry.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.retrycontextcache.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.retrylistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.retrypolicy.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.rollback.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.skip.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.skiplistener.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.skiplistenerannotation.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.faulttolerant.skippolicy.main()
        com.navercorp.spring.batch.plus.sample.step.chunkorientedstep.repeatoperation.main()
    }

    @Test
    fun stepConfiguration() {
        com.navercorp.spring.batch.plus.sample.step.configuration.allowstartifcomplete.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.listenerannotation.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.listenerobject.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.meterregistry.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.observationconvention.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.observationregistry.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.repository.main()
        com.navercorp.spring.batch.plus.sample.step.configuration.startlimit.main()
    }

    @Test
    fun stepCreation() {
        com.navercorp.spring.batch.plus.sample.step.creation.main()
    }

    @Test
    fun flowStep() {
        com.navercorp.spring.batch.plus.sample.step.flowstep.bean.main()
        com.navercorp.spring.batch.plus.sample.step.flowstep.init.main()
        com.navercorp.spring.batch.plus.sample.step.flowstep.variable.main()
    }

    @Test
    fun jobStep() {
        com.navercorp.spring.batch.plus.sample.step.jobstep.bean.main()
        com.navercorp.spring.batch.plus.sample.step.jobstep.config.extractor.main()
        com.navercorp.spring.batch.plus.sample.step.jobstep.config.launcher.main()
        com.navercorp.spring.batch.plus.sample.step.jobstep.variable.main()
    }

    @Test
    fun partitionStep() {
        com.navercorp.spring.batch.plus.sample.step.partitionstep.aggregator.main()
        com.navercorp.spring.batch.plus.sample.step.partitionstep.partitionhandler.direct.main()
        com.navercorp.spring.batch.plus.sample.step.partitionstep.partitionhandler.inner.main()
        com.navercorp.spring.batch.plus.sample.step.partitionstep.splitter.direct.main()
        com.navercorp.spring.batch.plus.sample.step.partitionstep.splitter.inner.main()
    }

    @Test
    fun taskletStep() {
        com.navercorp.spring.batch.plus.sample.step.taskletstep.bean.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.exceptionhandler.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.executor.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.listenerannotation.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.listenerobject.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.stepoperation.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.stream.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.config.transactionattribute.main()
        com.navercorp.spring.batch.plus.sample.step.taskletstep.variable.main()
    }
}
