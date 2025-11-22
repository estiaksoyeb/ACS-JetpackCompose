# Android Jetpack Compose Starter

A clean, crash-proof boilerplate for modern Android apps using Kotlin 2.0 and Jetpack Compose.
## 🚀 How to Use This Template
When you clone this repo to start a NEW app (e.g., "MyCoolApp"), follow these 3 steps:

**Step 1: Rename Package in build.gradle**

Open app/build.gradle and change these lines:
```
namespace 'com.self.mycoolapp'
applicationId "com.self.mycoolapp"
```

**Step 2: Rename Code Package**
 * Open MainActivity.kt.
 * Change the top line: package com.self.mycoolapp
 * Move the file to the matching folder path: app/src/main/java/com/self/mycoolapp/
**Step 3: Rename App Name**
Open app/src/main/res/values/strings.xml:
```
<string name="app_name">My Cool App</string>
```

**Optional: For Project**

Change the name from settings.gradle
```
rootProject.name = "ComposeStarter"
```

## ✅ Features Included
 * [x] Jetpack Compose Enabled
 * [x] Kotlin 2.0 Compiler Setup
 * [x] Material 3 Dependencies
 * [x] Crash-proof "Hello World" UI
 * [x] No XML Layouts
