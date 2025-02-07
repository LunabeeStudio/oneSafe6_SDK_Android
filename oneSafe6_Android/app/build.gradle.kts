import com.android.build.api.dsl.ApplicationDefaultConfig
import java.util.regex.Pattern

plugins {
    `application-flavors`
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    `onesafe-firebase`
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "studio.lunabee.onesafe"

    compileSdk = AndroidConfig.COMPILE_SDK
    buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK

        versionCode = AndroidConfig.envVersionCode
        versionName = AndroidConfig.envVersionName

        testInstrumentationRunner = "studio.lunabee.onesafe.test.HiltTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        missingDimensionStrategy("crypto", AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)

        val isTest = properties["studio.lunabee.onesafe.instrumentedTestProcess"]?.toString()?.toBoolean() ?: false
        setupProcesses(isTest = isTest)
        setupActivityAliases()
    }

    signingConfigs {
        maybeCreate("debug").apply {
            val keystore = System.getenv(EnvConfig.ENV_KEYSTORE_PATH)?.let { File(it) }
            if (keystore?.exists() == true) {
                println("Debug signing with ${keystore.path}")
                storeFile = keystore
                storePassword = System.getenv(EnvConfig.ENV_KEYSTORE_PWD)
                keyAlias = System.getenv(EnvConfig.ENV_KEY_ALIAS)
                keyPassword = System.getenv(EnvConfig.ENV_KEY_PWD)
            } else {
                storeFile = project.file("debug.keystore")
                storePassword = "androiddebug"
                keyAlias = "debug"
                keyPassword = "androiddebug"
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            val enableFirebase = false
            if (!enableFirebase) {
                extra.set("enableCrashlytics", false)
                (this as ExtensionAware).configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                    mappingFileUploadEnabled = false
                }
            }
            buildConfigField("Boolean", "ENABLE_FIREBASE", "$enableFirebase")
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["enableFirebase"] = enableFirebase
        }
        val release = getByName("release") {
            val enableFirebase = true
            manifestPlaceholders["enableFirebase"] = enableFirebase
            buildConfigField("Boolean", "ENABLE_FIREBASE", "$enableFirebase")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            packaging {
                resources {
                    excludes += "DebugProbesKt.bin" // https://github.com/Kotlin/kotlinx.coroutines/issues/2274
                    // https://github.com/grpc/grpc-java/issues/5199#issuecomment-451542605
                    excludes += "/*.proto"
                    excludes += "google/protobuf/*.proto"
                }
            }
        }
        create("benchmark") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
            proguardFiles("benchmark-rules.pro")
        }
    }

    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            pickFirsts += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "/META-INF/DEPENDENCIES"
            pickFirsts += "/META-INF/INDEX.LIST"
            pickFirsts += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

    lint {
        abortOnError = false
        checkDependencies = true
        checkGeneratedSources = false
        xmlOutput = file("${project.rootDir}/build/reports/lint/lint-report.xml")
        htmlOutput = file("${project.rootDir}/build/reports/lint/lint-report.html")
        lintConfig = file("${project.rootDir}/lint.xml")
        baseline = file("lint-baseline.xml")
        textReport = true
        explainIssues = false
    }

    testOptions {
        animationsDisabled = true
        packaging {
            jniLibs {
                // https://github.com/mockk/mockk/issues/297#issuecomment-901924678
                useLegacyPackaging = true
            }
            resources {
                resources.pickFirsts += "META-INF/LICENSE.md"
                resources.pickFirsts += "META-INF/LICENSE-notice.md"
                resources.pickFirsts += "META-INF/DEPENDENCIES"
                resources.pickFirsts += "META-INF/INDEX.LIST"
                resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            }
        }
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.maxParallelForks = 10
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    sourceSets["main"].resources {
        srcDir("${rootDir.path}/oneSafe6_common/assets")
    }
}

aboutLibraries {
    // Define the path configuration files are located in. E.g. additional libraries, licenses to add to the target .json
    configPath = "config"
    // A list of patterns (matching on the library `uniqueId` ($groupId:$artifactId)) to exclude libraries.
    exclusionPatterns = listOf(
        Pattern.compile("studio\\.lunabee:.*"),
        Pattern.compile("javax\\.annotation:.*"),
        Pattern.compile("com\\.onetrust\\.cmp:.*"),
        Pattern.compile("com\\.salesforce\\.marketingcloud:.*"),
        Pattern.compile("com\\.google\\.android\\.libraries\\.places:.*"),
        Pattern.compile("com\\.google\\.android\\.gms:.*"),
        Pattern.compile("com\\.google\\.android\\.play:.*"),
        Pattern.compile("com\\.google\\.firebase:.*"),
        Pattern.compile("com\\.ariadnext\\.android\\.idcheckio:.*"),
        Pattern.compile("com\\.github\\.parse-community:.*"),
        Pattern.compile("com\\.github\\.parse-community\\.Parse-SDK-Android:.*"),
        Pattern.compile("com\\.amazonaws:.*"),
    )
    // Enable pretty printing for the generated JSON file
    prettyPrint = true
}

ksp {
    arg("room.generateKotlin", "true")
}

kotlin {
    compilerOptions {
        //  To get reports, run: ./gradlew assembleDevRelease -PenableComposeCompilerReports=true
        if (project.findProperty("enableComposeCompilerReports") == "true") {
            val metricsPath = project.layout.buildDirectory.dir("compose_metrics")
            freeCompilerArgs.addAll(
                "-Pplugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$metricsPath",
                "-Pplugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$metricsPath",
            )
        }
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    betaImplementation(platform(libs.firebase.bom))
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.lunabee.bom))

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.hilt.compiler)

    // Camera X
    // Compose
    // FIXME https://github.com/android/android-test/issues/1755#issuecomment-1511876990
    // Force lifecycle-livedata-core-ktx version to fix Lint failure
    // connected test stuff
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.datastore.preferences)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.reflect)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlinx.datetime)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.room.ktx)
    androidTestImplementation(libs.sqlcipher.android)
    betaImplementation(libs.firebase.crashlytics.ktx)
    betaImplementation(libs.lblogger.crashlytics)
    betaImplementation(libs.play.services.base)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.tracing)
    devImplementation(libs.datastore)
    devImplementation(libs.datastore.preferences.core)
    devImplementation(libs.google.api.client.android)
    devImplementation(libs.kotlin.reflect)
    devImplementation(libs.room.ktx)
    implementation(libs.aboutlibraries.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.activity.compose)
    implementation(libs.android.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.media3)
    implementation(libs.androidx.media3.ui)
    implementation(libs.appcompat)
    implementation(libs.autofill)
    implementation(libs.biometric)
    implementation(libs.bouquet) { exclude(group = "org.bouncycastle") }
    implementation(libs.camerax.camera)
    implementation(libs.camerax.extensions)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.core.splashscreen)
    implementation(libs.doubleratchet)
    implementation(libs.emoji2)
    implementation(libs.emoji2.bundled)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lbccore)
    implementation(libs.lbcore)
    implementation(libs.lbcore.compose)
    implementation(libs.lbctheme)
    implementation(libs.lbextensions)
    implementation(libs.lbextensions.android)
    implementation(libs.lbloading.compose)
    implementation(libs.lblogger)
    implementation(libs.lifecycle.livedata.core.ktx)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lottie)
    implementation(libs.navigation.compose)
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    implementation(libs.palette.ktx)
    implementation(libs.play.services.auth.base) // required to get GoogleAuthUtil and GoogleApiAvailability
    implementation(libs.process.phoenix)
    implementation(libs.review)
    implementation(libs.review.ktx)
    implementation(libs.security.crypto)
    implementation(libs.seismic)
    implementation(libs.skydoves.colorpicker)
    implementation(libs.slice.builders.ktx)
    implementation(libs.work.runtime)
    implementation(libs.zoomable)
    kspAndroidTest(libs.androidx.hilt.compiler)
    kspDev(libs.room.compiler)
    kspTest(libs.androidx.hilt.compiler)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.uiautomator)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.datastore.preferences)
    testImplementation(libs.espresso.core)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.datetime)
    testImplementation(libs.mockk.android)
    testImplementation(libs.room.ktx)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.sqlcipher.android)

    androidTestImplementation(project(":bubbles"))
    androidTestImplementation(project(":crypto-android"))
    androidTestImplementation(project(":remote"))
    androidTestImplementation(project(":repository"))
    androidTestImplementation(projects.commonTestAndroid)
    androidTestImplementation(projects.dependencyInjection.testComponent)
    androidTestImplementation(projects.localAndroid)
    androidTestImplementation(projects.oneSafe6KMP.bubblesRepository)
    androidTestImplementation(projects.oneSafe6KMP.messagingRepository)
    androidTestImplementation(projects.oneSafe6KMP.shared)
    devImplementation(project(":app:message-companion"))
    devImplementation(project(":crypto-android"))
    devImplementation(projects.localAndroid)
    implementation(project(":app:help"))
    implementation(project(":app:login"))
    implementation(project(":app:migration"))
    implementation(project(":bubbles"))
    implementation(project(":dependency-injection"))
    implementation(project(":ime-android"))
    implementation(project(":import-export-core"))
    implementation(project(":messaging"))
    implementation(project(":widget-android"))
    implementation(projects.app.commonUi)
    implementation(projects.app.coreUi)
    implementation(projects.app.settings)
    implementation(projects.commonJvm)
    implementation(projects.domainJvm)
    implementation(projects.importExportAndroid)
    implementation(projects.importExportDrive)
    implementation(projects.oneSafe6KMP.error)
    implementation(projects.oneSafe6KMP.messagingDomain)
    implementation(projects.oneSafe6KMP.shared)
    testImplementation(project(":bubbles"))
    testImplementation(project(":common-protobuf"))
    testImplementation(project(":common-test"))
    testImplementation(project(":common-test-robolectric"))
    testImplementation(project(":crypto-android"))
    testImplementation(project(":import-export-proto"))
    testImplementation(project(":remote"))
    testImplementation(project(":repository"))
    testImplementation(projects.dependencyInjection.testComponent)
    testImplementation(projects.localAndroid)
    testImplementation(projects.oneSafe6KMP.bubblesDomain)
    testImplementation(projects.oneSafe6KMP.error)
    testImplementation(projects.oneSafe6KMP.messagingRepository)
}

apply(from = "../../Commons_Android/gradle/lunabee-app.gradle.kts")

tasks.named("downloadStrings").configure {
    this.setProperty("projectDir", "${project.projectDir.path}/common-ui")
}

fun ApplicationDefaultConfig.setupProcesses(isTest: Boolean) {
    val mainProcessName: String
    val autofillProcessName: String
    val databaseSetupProcessName: String

    if (isTest) { // Instrumented test does not support multi-process
        mainProcessName = ":main"
        autofillProcessName = ":main"
        databaseSetupProcessName = ":main"
    } else {
        mainProcessName = ":main"
        autofillProcessName = ":autofill"
        databaseSetupProcessName = ":databasesetup"
    }

    val oneSafe5Package = "com.lunabee.onesafe"
    buildConfigField("String", "ONESAFE_5_PACKAGE", "\"$oneSafe5Package\"")
    manifestPlaceholders["oneSafe5Package"] = oneSafe5Package

    buildConfigField("String", "MAIN_PROCESS_NAME", "\"$mainProcessName\"")
    manifestPlaceholders["mainProcessName"] = mainProcessName

    buildConfigField("String", "AUTOFILL_PROCESS_NAME", "\"$autofillProcessName\"")
    manifestPlaceholders["autofillProcessName"] = autofillProcessName

    buildConfigField("String", "DATABASE_SETUP_PROCESS_NAME", "\"$databaseSetupProcessName\"")
    manifestPlaceholders["databaseSetupProcessName"] = databaseSetupProcessName
}

fun ApplicationDefaultConfig.setupActivityAliases() {
    mapOf(
        "appIconDefault" to "MainActivityDefault",
        "appIconDefaultDark" to "MainActivityDefaultDark",
        "appIconChessText" to "MainActivityChessText",
        "appIconChess" to "MainActivityChess",
        "appIconHeadphones" to "MainActivityHeadphones",
    ).forEach { (key, value) ->
        manifestPlaceholders[key] = value
        buildConfigField("String", "ALIAS_$key", "\"$value\"")
    }
}
