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
package com.mayekukhisa.tool.projectgen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.versionOption
import com.mayekukhisa.tool.projectgen.model.Template
import com.mayekukhisa.tool.projectgen.model.TemplatesCatalog
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Properties

class App : CliktCommand(
   name = BuildConfig.NAME,
   help = "A project template generator tool",
   epilog = "Homepage: https://github.com/mayekukhisa/skeleton#readme",
   printHelpOnEmptyArgs = true,
) {
   init {
      versionOption(version = BuildConfig.VERSION)
      eagerOption(name = "--list-templates", help = "List available templates and exit") {
         throw PrintMessage(
            message = if (templates.isEmpty()) {
               "No templates found"
            } else {
               templates.joinToString(separator = System.lineSeparator()) { it.name }
            },
         )
      }
   }

   override fun run() = Unit

   companion object {
      val configFile: File by lazy {
         val osName = System.getProperty(/* key = */ "os.name").lowercase()
         val userHome = System.getProperty(/* key = */ "user.home")

         val configFilepath = when {
            osName.contains("win") -> "$userHome\\AppData\\Local\\${BuildConfig.NAME}\\config.properties"
            osName.contains("mac") -> "$userHome/Library/Application Support/${BuildConfig.NAME}/config.properties"
            else -> "$userHome/.config/${BuildConfig.NAME}/config.properties"
         }

         File(configFilepath).apply {
            if (!exists()) {
               parentFile.mkdirs()
               createNewFile()
            }
         }
      }

      val config = Properties().apply { configFile.inputStream().use(::load) }

      val templates: List<Template> by lazy {
         val templatesCatalog = config.getProperty(/* key = */ "templates.dir")
            ?.let {
               File(/* parent = */ it, /* child = */ "catalog.json").apply {
                  if (!exists()) {
                     FileUtils.writeStringToFile(
                        /* file = */ this,
                        /* data = */ Json.encodeToString(value = TemplatesCatalog(templates = emptyList())),
                        /* charset = */ Charsets.UTF_8,
                     )
                  }
               }
            }
            ?: throw PrintMessage(message = "Error: Templates directory not set", error = true)

         try {
            with(Json { ignoreUnknownKeys = true }) {
               decodeFromString<TemplatesCatalog>(
                  FileUtils.readFileToString(/* file = */ templatesCatalog, /* charsetName = */ Charsets.UTF_8),
               ).templates
            }
         } catch (e: SerializationException) {
            throw PrintMessage(message = "Error: Invalid templates catalog", error = true)
         }
      }
   }
}
