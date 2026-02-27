plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

group = "com.seuapp"
version = "1.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
}

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "desktop.MainKt"

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "DiarioTurnoDesktop"
            packageVersion = "1.0.0"
        }
    }
}
