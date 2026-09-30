/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.native

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.OS
import kotlin.io.path.appendText
import kotlin.io.path.readText
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(EnvironmentalVariablesOverride::class)
@OsCondition(supportedOn = [OS.MAC], enabledOnCI = [OS.MAC])
@DisplayName("K/N tests with synthetic cocoapods")
@NativeGradlePluginTests
class CocoaPodsSyntheticIT : KGPBaseTest() {

    override val defaultBuildOptions = super.defaultBuildOptions.copy(
        nativeOptions = super.defaultBuildOptions.nativeOptions.copy(
            cocoapodsGenerateWrapper = true
        )
    )

    private val cocoapodsSingleKtPod = "native-cocoapods-single"
    private val templateProjectName = "native-cocoapods-template"

    private val defaultPodInstallSyntheticTaskName = ":podInstallSyntheticIos"

    private val environmentVariables = EnvironmentalVariables(cocoaPodsEnvironmentVariables())

    @BeforeAll
    fun setUp() {
        ensureCocoapodsInstalled()
    }

    @DisplayName("Synthetic project podfile generation")
    @GradleTest
    fun testSyntheticProjectPodfileGeneration(gradleVersion: GradleVersion) {
        nativeProject(
            cocoapodsSingleKtPod,
            gradleVersion,
            environmentVariables = environmentVariables
        ) {
            buildGradleKts.addCocoapodsBlock(
                """
                ios.deploymentTarget = "15"
                pod("SSZipArchive")
                pod("Base64", "~> 1.1.0")
                pod("Alamofire") {
                    source = git("https://github.com/Alamofire/Alamofire.git") {
                        tag = "5.6.1"
                    }
                }
                """.trimIndent()
            )
            build(defaultPodInstallSyntheticTaskName) {
                assertTasksExecuted(":podGenIos")

                val podfile = "build/cocoapods/synthetic/ios/Podfile"
                assertFileInProjectContains(
                    podfile,
                    "platform :ios, '15'",
                    "pod 'SSZipArchive'",
                    "pod 'Base64', '~> 1.1.0'",
                    "pod 'Alamofire', :git => 'https://github.com/Alamofire/Alamofire.git', :tag => '5.6.1'",
                    "config.build_settings['EXPANDED_CODE_SIGN_IDENTITY'] = \"\"",
                    "config.build_settings['CODE_SIGNING_REQUIRED'] = \"NO\"",
                    "config.build_settings['CODE_SIGNING_ALLOWED'] = \"NO\""
                )
            }
        }
    }

    @DisplayName("Synthetic visionOS project pod installation")
    @GradleTest
    fun testSyntheticVisionosProjectPodInstallation(gradleVersion: GradleVersion) {
        nativeProject(
            cocoapodsSingleKtPod,
            gradleVersion,
            environmentVariables = environmentVariables
        ) {
            buildGradleKts.appendText(
                """

                kotlin {
                    visionosArm64()
                    visionosSimulatorArm64()
                }
                """.trimIndent()
            )
            buildGradleKts.addCocoapodsBlock(
                """
                visionos.deploymentTarget = "1.0"
                pod("SSZipArchive")
                """.trimIndent()
            )

            build(":podInstallSyntheticVisionos", ":podspec") {
                assertTasksExecuted(":podGenVisionos", ":podInstallSyntheticVisionos", ":podspec")
                val podfile = projectPath.resolve("build/cocoapods/synthetic/visionos/Podfile").readText()
                val podspec = projectPath.resolve("cocoapods.podspec").readText()

                assertEquals(
                    "visionos",
                    Regex("""(?m)^target '([^']+)' do$""").find(podfile)?.groupValues?.get(1),
                )
                assertEquals(
                    listOf("visionos", "1.0"),
                    Regex("""(?m)^\s*platform :([^,]+), '([^']+)'$""").find(podfile)?.groupValues?.drop(1),
                )
                assertEquals(
                    "SSZipArchive",
                    Regex("""(?m)^\s*pod '([^']+)'$""").find(podfile)?.groupValues?.get(1),
                )
                assertEquals(
                    listOf("XROS_DEPLOYMENT_TARGET"),
                    Regex("""config\.build_settings\['([^']+_DEPLOYMENT_TARGET)'\]""")
                        .findAll(podfile)
                        .map { it.groupValues[1] }
                        .distinct()
                        .toList(),
                )
                assertEquals(
                    "1.0",
                    Regex("""(?m)^\s*spec\.visionos\.deployment_target\s*=\s*'([^']+)'$""")
                        .find(podspec)
                        ?.groupValues
                        ?.get(1),
                )
            }
        }
    }

    @DisplayName("Synthetic project podfile postprocessing")
    @GradleTest
    fun testSyntheticProjectPodfilePostprocessing(gradleVersion: GradleVersion) {
        nativeProject(templateProjectName, gradleVersion, environmentVariables = environmentVariables) {
            preparePodfile("ios-app", ImportMode.FRAMEWORKS)
            buildGradleKts.addCocoapodsBlock("""pod("ChatSDK", version = "5.2.1")""")
            buildGradleKts.appendText(
                """
                
                tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask>().configureEach {
                    doLast {
                        podfile.get().appendText("ENV['SWIFT_VERSION'] = '5'")
                    }
                }
                """.trimIndent()
            )

            build(defaultPodInstallSyntheticTaskName) {
                assertFileInProjectContains("build/cocoapods/synthetic/ios/Podfile", "ENV['SWIFT_VERSION'] = '5'")
            }
        }
    }

    @DisplayName("Reinstalling pod invalidates UTD")
    @GradleTest
    fun testPodInstallInvalidatesUTD(gradleVersion: GradleVersion) {
        nativeProject(templateProjectName, gradleVersion, environmentVariables = environmentVariables) {
            preparePodfile("ios-app", ImportMode.FRAMEWORKS)
            buildGradleKts.addPod("Base64")

            build(defaultPodInstallSyntheticTaskName) {
                assertTasksExecuted(defaultPodInstallSyntheticTaskName)
                assertTrue { projectPath.resolve("build/cocoapods/synthetic/ios/Pods/Base64").toFile().deleteRecursively() }
            }

            build(defaultPodInstallSyntheticTaskName) {
                assertTasksExecuted(defaultPodInstallSyntheticTaskName)
            }
        }
    }
}
