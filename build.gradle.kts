/*
 * Copyright (c) 2023 Mayeku Khisa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   val kotlinVersion = "1.7.21"

   application
   id("com.diffplug.spotless") version "6.12.0"
   kotlin("jvm") version kotlinVersion
   kotlin("plugin.serialization") version kotlinVersion
}

group = "io.github.mayekukhisa.skeleton"
version = "0.1.0"

repositories {
   mavenCentral()
}

spotless {
   val indentSpaces = 3
   val maxLineLength = 120

   /*
    Spotless does not yet support .editorconfig settings with Ktlint.
    We have to provide them manually.
    */
   mapOf(
      "indent_size" to indentSpaces,
      "max_line_length" to maxLineLength,
      "ij_kotlin_allow_trailing_comma" to true,
      "ij_kotlin_allow_trailing_comma_on_call_site" to true,
   ).let {
      val licenseHeaderPath = "spotless/copyright.kt"

      kotlin {
         target("**/*.kt")
         targetExclude("**/build/**/*.kt", licenseHeaderPath)
         ktlint().editorConfigOverride(it)
         licenseHeaderFile(rootProject.file(licenseHeaderPath))
            .updateYearWithLatest(true)
      }

      kotlinGradle {
         target("**/*.kts")
         targetExclude("**/build/**/*.kts")
         ktlint().editorConfigOverride(it)
         licenseHeaderFile(rootProject.file(licenseHeaderPath), "^(?![\\/ ]\\*).*")
            .updateYearWithLatest(true)
      }
   }

   mapOf(
      "printWidth" to maxLineLength,
      "tabWidth" to indentSpaces,
   ).let {
      json {
         target("**/*.json")
         targetExclude("**/build/**/*.json")
         prettier().config(it)
      }

      format("Markdown") {
         target("**/*.md")
         targetExclude("**/build/**/*.md")
         prettier().config(it)
      }
   }
}

dependencies {
   implementation("com.github.ajalt.clikt:clikt:3.5.1")
   implementation("commons-io:commons-io:2.11.0")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
   testImplementation(kotlin("test"))
}

tasks.test {
   useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
   kotlinOptions.jvmTarget = "11"
}

application {
   mainClass.set("$group.MainKt")
}
