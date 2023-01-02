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
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.path
import io.github.mayekukhisa.skeleton.Skeleton
import io.github.mayekukhisa.skeleton.Utils
import io.github.mayekukhisa.skeleton.model.BasicProject
import io.github.mayekukhisa.skeleton.model.KotlinProject
import io.github.mayekukhisa.skeleton.model.Project
import io.github.mayekukhisa.skeleton.model.TemplateFile
import io.github.mayekukhisa.skeleton.model.TemplateManifest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.StringWriter
import java.util.Locale
import freemarker.template.Configuration as FreeMarker

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

   private val projectName by option(
      "-n",
      "--name",
      metavar = "NAME",
      help = "The name of the project; may not apply to all templates",
   ).defaultLazy("project's directory name") {
      projectDir.substringAfterLast('/')
   }

   private val projectPackage by option(
      "-p",
      "--package",
      metavar = "PACKAGE",
      help = "The package name for the project; may not apply to all templates",
   ).default("com.example").validate {
      if (!it.matches(Regex("^[a-z]+(\\.[a-z]+)*$"))) {
         fail("Invalid package name")
      }
   }

   private val projectDir by argument(
      name = "DIRECTORY",
      help = "The directory to create the project at",
   ).path().convert { it.toFile().canonicalPath }

   private val freemarker = FreeMarker(FreeMarker.VERSION_2_3_31).apply {
      setClassLoaderForTemplateLoading(javaClass.classLoader, "templates")
      defaultEncoding = Charsets.UTF_8.name()
      locale = Locale.US
   }

   private val projectModel: Project by lazy {
      if (projectTemplate == "kotlin") {
         KotlinProject(projectPackage, projectDir, projectName)
      } else {
         BasicProject(projectDir)
      }
   }

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

      with(getTemplateManifest()) {
         val templatePath = "templates/$projectTemplate"

         binaryFiles.forEach {
            val binaryData = IOUtils.resourceToByteArray("$templatePath/${it.src}", javaClass.classLoader)
            generateProjectFile(it, binaryData)
         }

         freemarkerFiles.forEach {
            val textData = freemarker.renderTemplate("$projectTemplate/${it.src}", projectModel)
            generateProjectFile(it, textData)
         }

         textFiles.forEach {
            val textData = Utils.resourceToString("$templatePath/${it.src}")
            generateProjectFile(it, textData)
         }
      }

      echo("Project created at \"$projectDir\"")
   }

   fun getTemplateManifest(): TemplateManifest =
      Json.decodeFromString(
         /*
          Not all template manifests need to be rendered, we can just read them as-is.
          However, we need to render those that derive output paths from a project property.
          (e.g. The package name for a Kotlin project)
          */
         freemarker.renderTemplate("$projectTemplate/manifest.json", projectModel),
      )

   private fun generateProjectFile(templateFile: TemplateFile, data: Any) {
      val outputFile = File("$projectDir/${templateFile.dest}")

      if (data is ByteArray) {
         FileUtils.writeByteArrayToFile(outputFile, data)
      } else {
         FileUtils.writeStringToFile(outputFile, data.toString(), Charsets.UTF_8)
      }

      outputFile.setExecutable(templateFile.executable, false)
   }

   private fun FreeMarker.renderTemplate(template: String, projectModel: Project): String =
      with(StringWriter()) {
         getTemplate(template).process(projectModel, this)
         toString()
      }
}
