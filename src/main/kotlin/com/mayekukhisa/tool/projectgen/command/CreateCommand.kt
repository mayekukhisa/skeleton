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
package com.mayekukhisa.tool.projectgen.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.mayekukhisa.tool.projectgen.App
import com.mayekukhisa.tool.projectgen.model.TemplateManifest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException

class CreateCommand : CliktCommand(
   name = "create",
   help = "Generate a new project from a template",
) {
   private val projectTemplate by option(
      "-t",
      "--template",
      metavar = "TEMPLATE",
      help = "The template to use",
   ).convert { value ->
      runCatching {
         App.templates.find { it.name == value }
      }.getOrNull() ?: fail(message = value)
   }.required()

   private val projectDir by argument(
      name = "DIRECTORY",
      help = "The directory to create the project at",
   ).file().convert { File(/* pathname = */ it.canonicalPath) }

   private val templatesDir by lazy { File(/* pathname = */ App.config.getProperty(/* key = */ "templates.dir")) }

   override fun run() {
      if (!projectDir.mkdirs()) {
         throw PrintMessage(
            message = "Error: Cannot create directory \"$projectDir\"" + System.lineSeparator() +
               "       File exists or permission denied",
            error = true,
         )
      }

      try {
         with(Json { ignoreUnknownKeys = true }) {
            decodeFromString<TemplateManifest>(
               string = FileUtils.readFileToString(
                  /* file = */ templatesDir.resolve(relative = "${projectTemplate.path}/manifest.json"),
                  /* charsetName = */ Charsets.UTF_8,
               ),
            ).run {
               textFiles.forEach {
                  try {
                     FileUtils.copyFile(
                        /* srcFile = */ templatesDir.resolve(relative = "${projectTemplate.path}/${it.sourcePath}"),
                        /* destFile = */ projectDir.resolve(relative = it.targetPath),
                     )
                  } catch (e: FileNotFoundException) {
                     FileUtils.deleteDirectory(projectDir)
                     throw PrintMessage(message = "Error: Template file \"${it.sourcePath}\" not found", error = true)
                  }
               }
            }
         }
      } catch (e: FileNotFoundException) {
         FileUtils.deleteDirectory(projectDir)
         throw PrintMessage(message = "Error: Template manifest not found", error = true)
      } catch (e: SerializationException) {
         FileUtils.deleteDirectory(projectDir)
         throw PrintMessage(message = "Error: Invalid template manifest", error = true)
      }

      echo(message = "Project created at \"$projectDir\"")
   }
}
