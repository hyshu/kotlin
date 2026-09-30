/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

#pragma once

#include <os/log.h>
#include <os/signpost.h>

#if !__has_attribute(stack_protector_ignore)
// Xcode 27 logging macros use an attribute unavailable in upstream LLVM 21.
// Limit the diagnostic override to the SDK's existing push/pop around each expansion.
#undef OS_LOG_PRAGMA_PUSH
#define OS_LOG_PRAGMA_PUSH \
    _Pragma("clang diagnostic push") _Pragma("clang diagnostic ignored \"-Wvla\"") \
            _Pragma("clang diagnostic ignored \"-Wunknown-attributes\"") OS_LOG_FORMAT_ERRORS
#endif
