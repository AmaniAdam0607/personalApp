import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(compose.materialIconsExtended)

            // Add SQLite dependencies
            implementation("org.jetbrains.exposed:exposed-core:0.41.1")
            implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
            implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
            implementation("org.xerial:sqlite-jdbc:3.42.0.0")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.dromio.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "org.dromio"
            packageVersion = "1.0.0"

            modules("java.sql")

            windows {
                menuGroup = "Avelyn Shop"
                shortcut = true
                menu = true
                includeAllModules = true
            }
        }
    }
}
