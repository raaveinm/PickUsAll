import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(libs.ktor.client.okhttp)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.composenativetray)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.components.resources)
}

compose.desktop {
    application {
        mainClass = "com.raaveinm.picasso.MainKt"
        if (System.getProperty("os.name").startsWith("Mac", ignoreCase = true)) {
            jvmArgs += listOf(
                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
            )
        }

        nativeDistributions {
            // DataStore protobuf and Chromium integration need APIs omitted by jlink inference.
            modules("jdk.unsupported", "java.net.http")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.raaveinm.picasso"
            packageVersion = "1.0.0"
        }
    }
}
