/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.tasks.fatFrameworkPlistPlatform
import org.jetbrains.kotlin.konan.target.KonanTarget
import kotlin.test.Test
import kotlin.test.assertEquals

class FatFrameworkTaskTest {

    @Test
    fun `visionOS targets use XROS plist platform`() {
        assertEquals("XROS", KonanTarget.VISIONOS_ARM64.fatFrameworkPlistPlatform)
        assertEquals("XROS", KonanTarget.VISIONOS_SIMULATOR_ARM64.fatFrameworkPlistPlatform)
    }
}
