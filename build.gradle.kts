/*
 * Copyright 2023-2024 Mayeku Khisa.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   application
   id("com.diffplug.spotless") version "6.12.0"

   val kotlinVersion = "1.8.10"
   kotlin("jvm") version kotlinVersion
   kotlin("plugin.serialization") version kotlinVersion
}

kotlin {
   jvmToolchain(17)
}

spotless {
   with(rootProject.file("spotless/copyright/kotlin.txt")) {
      /*
       Spotless does not yet support .editorconfig settings with Ktlint.
       We have to provide them manually.
       */
      mapOf(
         "indent_size" to 3,
         "max_line_length" to 120,
         "ij_kotlin_allow_trailing_comma" to true,
         "ij_kotlin_allow_trailing_comma_on_call_site" to true,
      ).let {
         kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint().editorConfigOverride(it)
            licenseHeaderFile(this@with).updateYearWithLatest(true)
         }

         kotlinGradle {
            target("**/*.kts")
            targetExclude("**/build/**/*.kts")
            ktlint().editorConfigOverride(it)
            licenseHeaderFile(this@with, "^(?![\\/ ]\\*).").updateYearWithLatest(true)
         }
      }
   }

   with(rootProject.file("spotless/config/prettierrc.yml")) {
      json {
         target("**/*.json")
         targetExclude("**/build/**/*.json")
         prettier().configFile(this@with)
      }

      format("Markdown") {
         target("**/*.md")
         targetExclude("/CHANGELOG.md", "**/build/**/*.md")
         prettier().configFile(this@with)
      }

      format("Yaml") {
         target("**/*.yml")
         targetExclude("**/build/**/*.yml")
         prettier().configFile(this@with)
      }
   }
}

repositories {
   mavenCentral()
}

dependencies {
   implementation("com.github.ajalt.clikt:clikt:3.5.2")
   implementation("commons-io:commons-io:2.11.0")
   implementation("org.freemarker:freemarker:2.3.32")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

   testImplementation(kotlin("test"))
}

group = "com.mayekukhisa.tool.projectgen"
version = rootProject.file("version.txt").readText().trim()

application {
   mainClass.set("${project.group}.MainKt")
}

val generatedSrcDir = layout.buildDirectory.dir("generated-src/kotlin")
sourceSets {
   val main by getting {
      kotlin.srcDir(generatedSrcDir)
   }
}

distributions {
   main {
      contents {
         from(".") {
            include("LICENSE", "NOTICE", "version.txt")
         }
      }
   }
}

val generateBuildConfig = tasks.register("generateBuildConfig") {
   doLast {
      generatedSrcDir.map { it.file("${project.group}/BuildConfig.kt") }.get().asFile.apply {
         parentFile.mkdirs()
         writeText(
            """
            package ${project.group}

            object BuildConfig {
               const val NAME = "${project.name}"
               const val VERSION = "${project.version}"
            }
            """.trimIndent(),
         )
      }
   }
}

tasks {
   named<Test>("test") {
      useJUnitPlatform()
   }

   named<KotlinCompile>("compileKotlin") {
      dependsOn(generateBuildConfig)
   }

   named<Tar>("distTar") {
      compression = Compression.GZIP
      archiveExtension.set("tar.gz")
   }
}
