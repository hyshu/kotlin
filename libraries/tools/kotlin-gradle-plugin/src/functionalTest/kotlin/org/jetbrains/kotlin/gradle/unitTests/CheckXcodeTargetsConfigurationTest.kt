/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.plugin.diagnostics.KotlinToolingDiagnostics
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckXcodeTargetsConfigurationTest {

    @Test
    fun `missing visionOS target diagnostic suggests a visionOS application`() {
        val diagnostic = KotlinToolingDiagnostics.MissingXcodeTargetDiagnostic(
            missingTargets = listOf(KonanTarget.VISIONOS_ARM64),
            xcodeProject = File("App.xcodeproj"),
        )

        assertEquals(
            """
            The following Kotlin targets are not linked to any Xcode application target in 'App.xcodeproj':
             - 'visionos_arm64'
            """.trimIndent(),
            diagnostic.message,
        )
        assertEquals(
            listOf(
                "Add the following new application targets to your Xcode project: 'visionOS Application' and setup embedAndSign task invocation for it.",
            ),
            diagnostic.solutions,
        )
    }
}
