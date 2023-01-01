/*
 * Copyright 2023 Mayeku Khisa.
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
   kotlin("jvm") version "1.7.21"
}

kotlin {
   jvmToolchain(11)
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
         targetExclude("**/build/**/*.md")
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
   implementation("com.github.ajalt.clikt:clikt:3.5.1")
   testImplementation(kotlin("test"))
}

group = "com.mayekukhisa.tool.projectgen"
version = "0.1.0"

application {
   mainClass.set("${project.group}.MainKt")
}

val generatedSrcDir = layout.buildDirectory.dir("generated-src/kotlin")
sourceSets {
   val main by getting {
      kotlin.srcDir(generatedSrcDir)
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
}
