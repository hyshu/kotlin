/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.properties.Properties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppleConfigurablesTest {
    @Test
    fun bootstrapCanDiscoverVisionOsTargetsWithoutSdkDependencies() {
        // Native build tools enumerate current targets against the older bootstrap distribution.
        val bootstrapProperties = Properties().apply {
            setProperty("predefinedLlvmDistributions", "bootstrap-llvm")
            setProperty("predefinedLibffiVersions", "bootstrap-libffi")
            setProperty("llvmHome", "bootstrap-llvm")
            setProperty("libffiDir", "bootstrap-libffi")
            setProperty("targetTriple.ios_arm64", "arm64-apple-ios")
            setProperty("targetSysRoot.ios_arm64", "bootstrap-ios-sdk")
            setProperty("targetToolchain.macos_arm64-ios_arm64", "bootstrap-toolchain")
            setProperty("targetToolchain.macos_x64-ios_arm64", "bootstrap-toolchain")
        }
        val targets = listOf(KonanTarget.VISIONOS_ARM64, KonanTarget.VISIONOS_SIMULATOR_ARM64)

        for (target in targets) {
            val configurables = loadConfigurables(target, bootstrapProperties, dependenciesRoot = null) as AppleConfigurablesImpl

            assertEquals(target, configurables.target)
            assertEquals(listOf("bootstrap-llvm", "bootstrap-libffi"), configurables.dependencies)
        }
    }
}
