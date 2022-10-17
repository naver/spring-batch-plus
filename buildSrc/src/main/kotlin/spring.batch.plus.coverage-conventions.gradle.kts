import kotlinx.kover.api.DefaultIntellijEngine

plugins {
    id("org.jetbrains.kotlinx.kover") // kover also covers java code
}

kover {
    // https://github.com/Kotlin/kotlinx-kover#configuring-project
    isDisabled.set(false)
    engine.set(DefaultIntellijEngine) // use IntellijEngine to cover all kotlin

    xmlReport {
        onCheck.set(false)
    }

    htmlReport {
        onCheck.set(true)
        reportDir.set(layout.buildDirectory.dir("kover/html"))
    }

    verify {
        onCheck.set(true)
        rule {
            isEnabled = true
            name = "Test Coverage Rule" // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped

            bound {
                minValue = 90
                maxValue = 100
                counter = kotlinx.kover.api.CounterType.INSTRUCTION // (LINE, INSTRUCTION, BRANCH)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE // (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
            }
        }
    }
}
