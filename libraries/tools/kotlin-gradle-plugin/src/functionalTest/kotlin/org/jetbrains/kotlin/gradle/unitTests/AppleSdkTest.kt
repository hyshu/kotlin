/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.AppleSdk
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.AppleTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.applePlatform
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.appleTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.sdk
import org.jetbrains.kotlin.konan.target.KonanTarget
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleSdkTest {

    @Test
    fun `Xcode visionOS platforms map to Kotlin Native targets`() {
        assertEquals(
            listOf(KonanTarget.VISIONOS_ARM64),
            AppleSdk.defineNativeTargets("xros", listOf("arm64", "arm64e")),
        )
        assertEquals(
            listOf(KonanTarget.VISIONOS_SIMULATOR_ARM64),
            AppleSdk.defineNativeTargets("xrsimulator", listOf("arm64", "arm64e")),
        )
    }

    @Test
    fun `visionOS targets expose Xcode SDK and platform names`() {
        assertEquals(AppleTarget.VISIONOS_DEVICE, KonanTarget.VISIONOS_ARM64.appleTarget)
        assertEquals("xros", AppleTarget.VISIONOS_DEVICE.sdk)
        assertEquals("visionOS", KonanTarget.VISIONOS_ARM64.applePlatform)

        assertEquals(AppleTarget.VISIONOS_SIMULATOR, KonanTarget.VISIONOS_SIMULATOR_ARM64.appleTarget)
        assertEquals("xrsimulator", AppleTarget.VISIONOS_SIMULATOR.sdk)
        assertEquals("visionOS Simulator", KonanTarget.VISIONOS_SIMULATOR_ARM64.applePlatform)
    }
}
