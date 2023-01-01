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
package io.github.mayekukhisa.skeleton

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.versionOption
import io.github.mayekukhisa.skeleton.model.TemplatesCatalog
import io.github.mayekukhisa.skeleton.subcommand.Create
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Skeleton : CliktCommand(
   help = "A project template generator tool",
   printHelpOnEmptyArgs = true,
   invokeWithoutSubcommand = true,
) {

   init {
      versionOption("0.1.0")
      eagerOption(
         "--list-templates",
         help = "List available templates and exit",
      ) {
         throw PrintMessage(templates.joinToString(separator = "\n") { it })
      }
   }

   override fun run() = Unit

   companion object {
      val templates: List<String>

      init {
         val templatesCatalog = Json.decodeFromString<TemplatesCatalog>(
            Utils.resourceToString("templates/catalog.json"),
         )
         templates = templatesCatalog.entries
      }
   }
}

fun main(args: Array<String>) {
   Skeleton()
      .subcommands(Create())
      .main(args)
}
