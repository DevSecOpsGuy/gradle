/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configurations

import common.buildToolGradleParameters
import jetbrains.buildServer.configs.kotlin.v2018_2.AbsoluteId
import model.CIBuildModel
import model.Stage

class InstantGradleception(model: CIBuildModel, stage: Stage) : BaseGradleBuildType(model, stage = stage, init = {
    uuid = "${model.projectPrefix}InstantGradleception"
    id = AbsoluteId(uuid)
    name = "InstantGradleception - Java8 Linux"
    description = "Builds Gradle with the version of Gradle which is currently under development (twice) with instant execution enabled"

    params {
        param("env.JAVA_HOME", buildJavaHome)
    }

    features {
        publishBuildStatusToGithub(model)
    }

    val buildScanTagForType = buildScanTag("InstantGradleception")
    val defaultParameters = (buildToolGradleParameters() + listOf(buildScanTagForType)).joinToString(separator = " ")

    applyDefaults(model, this, ":install", notQuick = true, extraParameters = "-Pgradle_installPath=dogfood-first $buildScanTagForType", extraSteps = {
        localGradle {
            name = "BUILD_WITH_BUILT_GRADLE"
            tasks = "clean :install"
            gradleHome = "%teamcity.build.checkoutDir%/dogfood-first"
            gradleParams = "-Pgradle_installPath=dogfood-second -PignoreIncomingBuildReceipt=true $defaultParameters"
        }
        localGradle {
            name = "QUICKCHECK_WITH_GRADLE_BUILT_BY_GRADLE_STORING_WORK_GRAPH"
            tasks = "clean"
            gradleHome = "%teamcity.build.checkoutDir%/dogfood-second"
            gradleParams = "-Dorg.gradle.unsafe.instant-execution=true $defaultParameters"
        }
        localGradle {
            name = "QUICKCHECK_WITH_GRADLE_BUILT_BY_GRADLE_LOADING_WORK_GRAPH"
            tasks = "clean"
            gradleHome = "%teamcity.build.checkoutDir%/dogfood-second"
            gradleParams = "-Dorg.gradle.unsafe.instant-execution=true $defaultParameters"
        }
    })
})
