// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("room_version", "2.5.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) version "1.9.0" apply false
    id("com.android.library") version "8.0.2" apply false
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
