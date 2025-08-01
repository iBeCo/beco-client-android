plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.beco.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.beco.demo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        debug {
            // Disable obfuscation for debug builds to aid in debugging
            isMinifyEnabled = false
            isDebuggable = true
        }

        release {
            // Enable ProGuard/R8 for release builds
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        // Optional: Create a minified debug build for testing obfuscation
        create("debugMinified") {
            initWith(getByName("debug"))
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            matchingFallbacks += listOf("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = true
        checkDependencies = true
        checkGeneratedSources = false
        explainIssues = true
        absolutePaths = false

        // Enable additional checks
        enable +=
            setOf(
                "UnusedResources",
                "UnusedIds",
                "IconDensities",
                "IconDuplicates",
                "IconLocation",
                "IconMissingDensityFolder",
                "GradleDependency",
                "NewerVersionAvailable",
                "StringFormatMatches",
                "PluralsCandidate",
                "MissingTranslation",
                "ExtraTranslation",
                "TypographyFractions",
                "TypographyDashes",
                "TypographyQuotes",
                "TypographyEllipsis",
                "Overdraw",
                "UnusedNamespace",
                "HardcodedText",
                "ContentDescription",
                "SmallSp",
                "SpUsage",
                "TextFields",
                "ViewHolder",
                "Recycle",
                "CommitTransaction",
                "Wakelock",
                "DuplicateActivity",
                "GradleOverrides",
                "DeviceAdmin",
                "LogConditional",
                "StopShip",
                "Assert",
            )

        // Disable some checks that might be too strict for demo app
        disable +=
            setOf(
                "GoogleAppIndexingWarning",
                "HardcodedDebugMode",
                "AllowBackup",
                "MissingApplicationIcon",
            )

        // Set severity levels
        error +=
            setOf(
                "StopShip",
                "Assert",
                "DuplicateActivity",
                "CommitTransaction",
            )

        warning +=
            setOf(
                "HardcodedText",
                "UnusedResources",
                "IconDensities",
            )

        informational +=
            setOf(
                "LogConditional",
                "ContentDescription",
            )

        // Output options
        htmlReport = true
        xmlReport = true
        textReport = false
        sarifReport = true

        htmlOutput = file("$buildDir/reports/lint/lint-results.html")
        xmlOutput = file("$buildDir/reports/lint/lint-results.xml")
        sarifOutput = file("$buildDir/reports/lint/lint-results.sarif")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.becomap.sdk:becomap:2.0.3")
    //uncomment for development purpose
}
