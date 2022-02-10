plugins {
    `kotlin-dsl`
    id("org.jetbrains.intellij")  version "1.2.1"
    id("org.jetbrains.kotlin.jvm")  version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
}

group = "com.github.theapache64"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2020.3")
}
tasks.withType<org.jetbrains.intellij.tasks.PatchPluginXmlTask> {
    changeNotes.set("""Fix java.io.IOException by @riegersan""")
    sinceBuild.set("183")
    untilBuild.set("")
}
