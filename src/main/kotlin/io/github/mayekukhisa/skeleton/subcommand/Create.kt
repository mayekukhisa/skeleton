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
package io.github.mayekukhisa.skeleton.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import io.github.mayekukhisa.skeleton.Skeleton
import io.github.mayekukhisa.skeleton.Utils
import io.github.mayekukhisa.skeleton.model.TemplateManifest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import java.io.File

class Create : CliktCommand(
   help = "Generate a new project from a template",
) {
   private val defaultTemplate = Skeleton.templates.first()
   private val projectTemplate by option(
      "-t",
      "--template",
      metavar = "TEMPLATE",
      help = "The template to use",
   ).convert { value ->
      Skeleton.templates.find { it == value } ?: fail("Template not found")
   }.default(defaultTemplate, defaultTemplate)

   private val projectDir by argument(
      name = "DIRECTORY",
      help = "The directory to create the project at",
   ).path().convert { it.toFile().canonicalPath }

   init {
      context {
         helpFormatter = CliktHelpFormatter(showDefaultValues = true)
      }
   }

   override fun run() {
      if (!File(projectDir).mkdirs()) {
         echo("Error: Cannot create directory \"$projectDir\"")
         echo("       File exists or permission denied")
         throw ProgramResult(1)
      }

      getTemplateManifest().textFiles.forEach {
         val textData = Utils.resourceToString("templates/$projectTemplate/${it.src}")
         FileUtils.writeStringToFile(File("$projectDir/${it.dest}"), textData, Charsets.UTF_8)
      }

      echo("Project created at \"$projectDir\"")
   }

   fun getTemplateManifest(): TemplateManifest =
      Json.decodeFromString(Utils.resourceToString("templates/$projectTemplate/manifest.json"))
}
