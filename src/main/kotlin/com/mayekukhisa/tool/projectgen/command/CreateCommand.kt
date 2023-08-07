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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.mayekukhisa.tool.projectgen.App
import com.mayekukhisa.tool.projectgen.model.TemplateFile
import com.mayekukhisa.tool.projectgen.model.TemplateManifest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.StringWriter
import java.util.Locale
import java.util.UUID
import freemarker.template.Configuration as FreeMarker

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

   private val projectName by option(
      "-n",
      "--name",
      metavar = "NAME",
      help = "The name of the project",
   ).defaultLazy("project's directory name") {
      projectDir.name
   }

   private val packageName by option(
      "-p",
      "--package",
      metavar = "PACKAGE",
      help = "The top-level package of the project",
   ).default("com.example").validate {
      if (!it.matches(regex = Regex(pattern = "^[a-z]+(\\.[a-z]+)*$"))) {
         fail(message = "Invalid package")
      }
   }

   private val projectDir by argument(
      name = "DIRECTORY",
      help = "The directory to create the project at",
   ).file().convert { File(/* pathname = */ it.canonicalPath) }

   private val projectUUID by lazy { UUID.randomUUID().toString() }
   private val templatesDir by lazy { File(/* pathname = */ App.config.getProperty(/* key = */ "templates.dir")) }

   private val freemarker by lazy {
      FreeMarker(/* incompatibleImprovements = */ FreeMarker.VERSION_2_3_32).apply {
         setDirectoryForTemplateLoading(/* dir = */ templatesDir.resolve(relative = projectTemplate.path))
         defaultEncoding = Charsets.UTF_8.name()
         locale = Locale.US
      }
   }

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
               /*
                While not all template manifest files require rendering like Freemarker files, we must render them in
                cases where they derive values from the template's input data model.
                */
               string = freemarker.renderTemplateFile(filepath = "manifest.json"),
            ).run {
               binaryFiles.forEach {
                  generateProjectFile(templateFile = it)
               }

               freemarkerFiles.forEach {
                  generateProjectFile(templateFile = it, shouldRender = true)
               }

               textFiles.forEach {
                  generateProjectFile(templateFile = it)
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

   private fun generateProjectFile(templateFile: TemplateFile, shouldRender: Boolean = false) {
      try {
         val outputFile = projectDir.resolve(relative = templateFile.targetPath)

         with(templatesDir.resolve(relative = "${projectTemplate.path}/${templateFile.sourceRoot}")) {
            if (shouldRender) {
               freemarker.setDirectoryForTemplateLoading(/* dir = */ this)
               FileUtils.writeStringToFile(
                  /* file = */ outputFile,
                  /* data = */ freemarker.renderTemplateFile(filepath = templateFile.sourcePath),
                  /* charset = */ Charsets.UTF_8,
               )
            } else {
               FileUtils.copyFile(
                  /* srcFile = */ resolve(relative = templateFile.sourcePath),
                  /* destFile = */ outputFile,
               )
            }
         }

         outputFile.setExecutable(/* executable = */ templateFile.executable, /* ownerOnly = */ false)
      } catch (e: FileNotFoundException) {
         FileUtils.deleteDirectory(projectDir)
         throw PrintMessage(message = "Error: Template file \"${templateFile.sourcePath}\" not found", error = true)
      }
   }

   private fun FreeMarker.renderTemplateFile(filepath: String): String {
      return with(StringWriter()) {
         getTemplate(/* name = */ filepath).process(
            /* dataModel = */
            mapOf(
               "projectName" to projectName,
               "projectUUID" to projectUUID,
               "packageName" to packageName,
            ),
            /* out = */ this,
         )
         toString()
      }
   }
}
