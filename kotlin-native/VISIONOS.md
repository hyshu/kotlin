# visionOS

This fork adds visionOS device and Apple Silicon simulator targets to Kotlin 2.4.10.
The branch starts at `v2.4.10` and reorganizes the changes prepared for the draft proposal.
It keeps Native support, ABI tooling, and Gradle integration in separate commits.
It includes compatibility fixes for Xcode 27 and the audio platform libraries used by Ambient.

Build on an Apple Silicon Mac with Java 21 and Xcode with the visionOS SDK installed.
Run these commands from the repository root.

Build the target utilities first so the Native build scripts can recognize visionOS.

```sh
./gradlew :native:kotlin-native-utils:jar \
  :kotlin-util-io:jar :kotlin-util-klib:jar :core:metadata:jar \
  -Pkotlin.native.enabled=false -Pbuild.number=2.4.10-visionos --max-workers=2
```

Then build the compiler, runtimes, and platform libraries for both targets.

```sh
./gradlew :kotlin-native:dist \
  :kotlin-native:visionos_arm64CrossDistRuntime \
  :kotlin-native:visionos_simulator_arm64CrossDistRuntime \
  :kotlin-native:platformLibs:visionos_arm64Install \
  :kotlin-native:platformLibs:visionos_simulator_arm64Install \
  -Pkotlin.native.enabled=true -Pbuild.number=2.4.10-visionos \
  -Pvisionos.native.utils.jar="$PWD/native/utils/build/libs/kotlin-native-utils-2.4.10-visionos.jar" \
  -PdisableBreakpad=true --max-workers=2
```

The distribution is written to `kotlin-native/dist`.
Use `visionos_arm64` for devices and `visionos_simulator_arm64` for the simulator.
Ambient runs this compiler directly to produce its static `SharedLogic.framework`.
