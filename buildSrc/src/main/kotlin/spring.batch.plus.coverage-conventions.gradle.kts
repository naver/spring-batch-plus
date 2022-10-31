import kotlinx.kover.api.DefaultIntellijEngine

plugins {
    id("org.jetbrains.kotlinx.kover") // kover also covers java code
}

kover {
    // https://github.com/Kotlin/kotlinx-kover#configuring-project
    isDisabled.set(false)
    engine.set(DefaultIntellijEngine) // use IntellijEngine to cover all kotlin

    xmlReport {
        onCheck.set(true)
        reportFile.set(layout.buildDirectory.file("kover/result.xml"))
    }

    htmlReport {
        onCheck.set(true)
        reportDir.set(layout.buildDirectory.dir("kover/html"))
    }
}
