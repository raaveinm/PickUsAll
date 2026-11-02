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

// IDE hotRun uses JBR, whose bundled jcef module otherwise shadows jcefmaven's API jar.
// Keep the standard desktop modules and the attach/compiler modules used by hot reload.
// Apply this only to development JVMs: the packaged jlink runtime has its own module set.
tasks.withType<JavaExec>().configureEach {
    jvmArgs("--limit-modules=java.se,jdk.unsupported,jdk.crypto.ec,jdk.attach,jdk.compiler,jdk.jdi")
    if (System.getProperty("os.name").startsWith("Mac", ignoreCase = true)) {
        jvmArgs(
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
        )
    }
}

val webViewRuntimeCheck: SourceSet = sourceSets.create("webViewRuntimeCheck") {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

// This checks the real application classpath without starting Chromium or touching its cache.
tasks.register<JavaExec>("verifyWebViewRuntime") {
    group = "verification"
    description = "Verifies that desktop WebView loads JCEF and its metadata from the same dependency."
    dependsOn(tasks.named(webViewRuntimeCheck.classesTaskName))
    classpath = webViewRuntimeCheck.runtimeClasspath
    mainClass.set("com.raaveinm.picasso.WebViewRuntimeCheckKt")
}

tasks.named("check") {
    dependsOn("verifyWebViewRuntime")
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
