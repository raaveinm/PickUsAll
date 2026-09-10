plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinx.serialization)
}

/*
 * webrtc-java ships native binaries as a separate, OS+arch-classified artifact
 * (dev.onvoid.webrtc:webrtc-java:<version>:<classifier>) alongside the plain
 * jar of Java classes - mirrors the classifier logic webrtc-java's own
 * NativeLoader uses at runtime to pick the bundled .so/.dll/.dylib, so this
 * must stay in sync with dev.onvoid.webrtc.internal.NativeLoader.
 */
fun webrtcJavaNativesClassifier(): String {
    val osName = System.getProperty("os.name").lowercase()
    val osFamily = when {
        osName.startsWith("mac os") -> "macos"
        osName.startsWith("linux") -> "linux"
        osName.startsWith("windows") -> "windows"
        else -> throw GradleException("webrtc-java: unsupported OS '$osName'")
    }
    val osArch = when (System.getProperty("os.arch").lowercase()) {
        "x86_64", "x86-64", "amd64" -> "x86_64"
        "aarch32", "arm" -> "aarch32"
        "aarch64", "arm64" -> "aarch64"
        else -> throw GradleException("webrtc-java: unsupported arch '${System.getProperty("os.arch")}'")
    }
    return "$osFamily-$osArch"
}

kotlin {

    android {
        namespace = "com.raaveinm.features.impl_webrtc"
        compileSdk {
            version = release(libs.versions.android.compileSdk.get().toInt()) {
                minorApiLevel = libs.versions.android.minorSdk.get().toInt()
            }
        }
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "features:impl-webrtcKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinxJson)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.webrtc.kmp)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.runner)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.webrtc.kmp)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.webrtc.java)
                implementation(
                    "dev.onvoid.webrtc:webrtc-java:${libs.versions.webrtc.java.get()}:${webrtcJavaNativesClassifier()}"
                )
            }
        }
    }

}
