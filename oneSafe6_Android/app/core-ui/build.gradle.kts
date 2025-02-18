plugins {
    `android-library`
    `onesafe-publish`
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "studio.lunabee.onesafe.coreui"

    resourcePrefix("os_")

    buildFeatures {
        compose = true
    }
    packaging {
        resources.pickFirsts.add("META-INF/LICENSE.md")
        resources.pickFirsts.add("META-INF/LICENSE-notice.md")
        resources.pickFirsts.add("META-INF/DEPENDENCIES")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    // Compose
    // FIXME https://github.com/android/android-test/issues/1755#issuecomment-1511876990
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.lbcandroidtest)
    api(libs.lbcaccessibility)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.tracing)
    implementation(libs.activity.ktx)
    implementation(libs.android.material)
    implementation(libs.coil.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.kotlinx.coroutines.android) // enforce version of coroutines
    implementation(libs.lbccore)
    implementation(libs.lbchaptic)
    implementation(libs.lbctheme)
    implementation(libs.lblogger)
    implementation(libs.palette.ktx)

    lintChecks(project(":app:core-ui:checks"))
    lintPublish(project(":app:core-ui:checks"))
}
