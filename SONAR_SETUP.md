# SonarQube Code Coverage Setup

This document explains how to configure and run code coverage analysis with SonarQube for the Becomap Android SDK.

**Note**: This configuration focuses only on the SDK module (`becomap-android-sdk`). The demo app module is excluded from analysis as it's only used for testing purposes.

## 📋 **Prerequisites**

- SonarQube server running (local or remote)
- SonarQube token for authentication
- Java 8+ (JaCoCo 0.8.11 supports up to Java 21)

## 🔧 **Configuration**

### 1. **JaCoCo Configuration**
The SDK is configured with JaCoCo for code coverage:

```gradle
// In becomap-android-sdk/build.gradle
plugins {
    id 'jacoco'
}

buildTypes {
    debug {
        testCoverageEnabled true
    }
}

jacoco {
    toolVersion = "0.8.11" // Java 21 compatible
}
```

### 2. **SonarQube Configuration**
The project includes SonarQube plugin and configuration:

```kotlin
// In build.gradle.kts
plugins {
    id("org.sonarqube") version "4.4.1.3373"
}
```

## 🚀 **Running Coverage Analysis**

### **Step 1: Generate Coverage Report**
```bash
# Run tests and generate JaCoCo coverage report
./gradlew :becomap-android-sdk:testDebugUnitTest
./gradlew :becomap-android-sdk:jacocoTestReport

# Or use the combined task
./gradlew :becomap-android-sdk:sonarCoverage
```

### **Step 2: Run SonarQube Analysis**
```bash
# Set environment variables
export SONAR_HOST_URL="http://your-sonar-server:9000"
export SONAR_TOKEN="your-sonar-token"

# Run SonarQube analysis
./gradlew sonar

# Or use the combined task
./gradlew sonarAnalysis
```

## 📊 **Coverage Reports Location**

After running the coverage tasks, reports are generated at:

- **XML Report**: `becomap-android-sdk/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`
- **HTML Report**: `becomap-android-sdk/build/reports/jacoco/jacocoTestReport/html/index.html`
- **Execution Data**: `becomap-android-sdk/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec`

## ⚙️ **SonarQube Properties**

The project is configured with the following SonarQube properties:

```properties
# Core Configuration
sonar.projectKey=becomap-android
sonar.projectName=Becomap Android SDK
sonar.projectVersion=1.0.0

# Source Configuration
sonar.sources=becomap-android-sdk/src/main/java
sonar.tests=becomap-android-sdk/src/test/java

# Coverage Configuration
sonar.coverage.jacoco.xmlReportPaths=becomap-android-sdk/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
sonar.junit.reportPaths=becomap-android-sdk/build/test-results/testDebugUnitTest

# Exclusions (including demo app and models)
sonar.exclusions=**/build/**,**/R.java,**/BuildConfig.java,**/databinding/**,**/generated/**,app/**
sonar.coverage.exclusions=**/R.class,**/R$*.class,**/BuildConfig.*,**/Manifest*.*,**/*Test*.*,**/models/**
```

## 🎯 **Available Gradle Tasks**

| Task | Description |
|------|-------------|
| `jacocoTestReport` | Generate JaCoCo coverage report (SDK only) |
| `sonarCoverage` | Generate coverage report for SonarQube (SDK only) |
| `generateCoverageReport` | Generate coverage report (SDK only) |
| `sdkCodeQualityCheck` | Run code quality checks for SDK module only |
| `sonar` | Run SonarQube analysis (SDK only) |
| `sonarAnalysis` | Run SonarQube analysis with coverage (SDK only) |

## 🔍 **Coverage Exclusions**

The following files/patterns are excluded from coverage analysis:

- **Demo App**: `app/**` (excluded entirely as it's for testing only)
- **Model Classes**: `**/models/**` (data classes with minimal logic)
- **Generated Files**: `**/R.class`, `**/BuildConfig.*`, `**/databinding/**`
- **Test Files**: `**/*Test*.*`, `**/test/**`, `**/androidTest/**`
- **Android Framework**: `**/Manifest*.*`
- **Build Artifacts**: `**/build/**`, `**/generated/**`

### Why Exclude Model Classes?

Model classes (POJOs/data classes) are excluded from coverage because they typically contain:
- **Simple getters/setters**: No business logic to test
- **Data structures**: Primarily hold data with minimal behavior
- **Serialization annotations**: Framework-handled functionality
- **Constructor logic**: Usually straightforward initialization

This exclusion focuses coverage metrics on **business logic** and **algorithmic code** where testing provides the most value.

## 🐛 **Troubleshooting**

### **JaCoCo Version Issues**
If you encounter Java version compatibility issues:
```gradle
jacoco {
    toolVersion = "0.8.11" // Use latest version for Java 21 support
}
```

### **Missing Coverage Data**
Ensure tests run with coverage enabled:
```bash
# Run tests first
./gradlew :becomap-android-sdk:testDebugUnitTest

# Then generate report
./gradlew :becomap-android-sdk:jacocoTestReport
```

### **SonarQube Connection Issues**
Check environment variables:
```bash
echo $SONAR_HOST_URL
echo $SONAR_TOKEN
```

## 📈 **Coverage Metrics**

The coverage report includes:
- **Line Coverage**: Percentage of executable lines covered
- **Branch Coverage**: Percentage of branches covered
- **Method Coverage**: Percentage of methods covered
- **Class Coverage**: Percentage of classes covered

## 🎯 **Quality Gates**

Configure quality gates in SonarQube for:
- Minimum coverage percentage (e.g., 80%)
- Maximum code duplication
- Security vulnerabilities
- Code smells and maintainability

## 📝 **CI/CD Integration**

For continuous integration, add to your pipeline:

```yaml
# Example GitHub Actions
- name: Run Tests and Coverage
  run: ./gradlew :becomap-android-sdk:sonarCoverage

- name: SonarQube Analysis
  env:
    SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: ./gradlew sonar
```
