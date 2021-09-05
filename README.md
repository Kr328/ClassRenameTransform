# HiddenApiRefinePlugin

A Gradle plugin that improves the experience when developing Android apps, especially system tools, that use hidden APIs.

## Background

When developing system tools, it's inevitable to use hidden APIs. There are two ways to use hidden APIs, the reflection way and the linking way. For some types of system tools, a large amount of hidden APIs is required, using the reflection way is too inconvenient. So the linking way is commonly used in those projects.

In short, the linking way is to create Java-only modules with stub classes and then `compileOnly` them in the main Android modules. This is the same as what Android SDK's `android.jar` does.

However the linking way, or "the stub way", have some problems:

1. "Bridge classes" is required if only some members of the class are hidden.
2. Kotlin will try to link public classes from the stub module, `implementation` a second Java-only which `compileOnly` the stub module can workaround the problem.
3. Interface implementation will be removed by R8 if the stub module is not `compileOnly` directly from the main module, however doing this will bring back problem 2.

This plugin is to solve these problems.

## What do this plugin do

This plugin uses the Transform API of the Android Gradle Plugin to create a transform that modifies class names, the names are user specified by annotation `@RefineAs`. So that all the problems will be gone and the stub module does not need to be a Java-only module.

The idea and the implementation is from [@Kr328](https://github.com/Kr328).

## Usage

![gradle-plugin](https://img.shields.io/maven-central/v/dev.rikka.tools.refine/gradle-plugin?label=gradle-plugin)
![annotation](https://img.shields.io/maven-central/v/dev.rikka.tools.refine/annotation?label=annotation)
![runtime](https://img.shields.io/maven-central/v/dev.rikka.tools.refine/runtime?label=runtime)

### Add gradle plugin to root project

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'dev.rikka.tools.refine:gradle-plugin:3.0.0'
    }
}
```

### Create "hidden-api" module

1. Create a new Android library module

2. Add annotation dependency

   ```gradle
   dependencies {
       annotationProcessor 'dev.rikka.tools.refine:annotation-processor:3.0.0'
       compileOnly 'dev.rikka.tools.refine:annotation:3.0.0'
   }
   ```

3. Add the hidden classes

   Here, we use a hidden `ActivityManager` API as the example.

   ```java
   package android.app;

   import dev.rikka.tools.refine.RefineAs;

   @RefineAs(ActivityManager.class)
   public class ActivityManagerHidden {
       public void forceStopPackageAsUser(String packageName, int userId) {
           throw new RuntimeException();
       }
   }
   ```

   This line `@RefineAs(ActivityManager.class)` will let the plugin renames `android.app.ActivityManagerHidden` to `android.app.ActivityManager`.

### Use hidden API

1. Apply this plugin in the final Android application module

   ```gradle
   plugins {
       id('dev.rikka.tools.refine.gradle-plugin')
   }
   ```

2. Import "hidden-api" module with `compileOnly`

   ```gradle
   dependencies {
       compileOnly project(':hidden-api')
   }
   ```

3. Import the "Refine" class (Optional, if you don't need the "unsafeCast" method)

   ```gradle
   dependencies {
       implementation("dev.rikka.tools.refine:runtime:3.0.0")
   }
   ```

4. Use the hidden API

   ```java
   ActivityManager activityManager = context.getSystemService(ActivityManager.class);
   Refine.unsafeCast<ActivityManagerHidden>(activityManager).forceStopPackageAsUser(packageName, userId);
   ```

   After R8, this "cast" will be "removed" or "inlined". This line will be identical to a normal method call.
