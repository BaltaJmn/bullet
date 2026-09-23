import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

// purchases-kmp 3.2.1 ships an absolute path in its cinterop manifest:
//   linkerOpts.ios_simulator_arm64=-L/Applications/Xcode-16.4.app/.../usr/lib/swift/iphonesimulator/
// That is the Xcode on RevenueCat's build machine. Nobody else has it, so the Swift
// back-deployment libraries the RevenueCat binary force-loads (swiftCompatibility56 and
// friends) are never found and the link dies on undefined symbols. Point the linker at
// whatever Xcode is actually selected here.
//
// Only the test binaries need it. The framework is static, so the app's real link happens
// inside Xcode, which already searches its own toolchain and never notices the dead path.
//
// Asked only on a Mac. The Linux job configures the iOS targets too, it just never links them,
// and asking a machine with no Xcode where Xcode is brings down the configuration of the whole
// project, not only the iOS half of it.
val isMacHost = System.getProperty("os.name").startsWith("Mac")
val swiftToolchainLibs =
    providers.exec { commandLine("xcode-select", "-p") }
        .standardOutput.asText.map { "${it.trim()}/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift" }

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        val sdk = if (iosTarget.konanTarget.name.contains("simulator")) "iphonesimulator" else "iphoneos"
        if (isMacHost) {
            iosTarget.binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable>()
                .configureEach { linkerOpts("-L${swiftToolchainLibs.get()}/$sdk") }
        }
    }

    android {
       namespace = "com.baltajmn.bullet.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }

    sourceSets {
        // RevenueCat's iOS half is a cinterop binding.
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings { optIn("kotlinx.cinterop.ExperimentalForeignApi") }
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.glance.appwidget)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.fragment)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.uiBackhandler)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.purchases.kmp.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
