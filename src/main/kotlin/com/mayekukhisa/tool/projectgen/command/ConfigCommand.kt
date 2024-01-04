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
package com.mayekukhisa.tool.projectgen.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.splitPair
import com.mayekukhisa.tool.projectgen.App

class ConfigCommand : CliktCommand(
   name = "config",
   help = "Manage configuration values",
   printHelpOnEmptyArgs = true,
) {
   private val configAction by mutuallyExclusiveOptions(
      option("--set", metavar = "KEY=VALUE", help = "Set the value for the key").splitPair().convert {
         ConfigAction.Set(it)
      },
      option("--get", metavar = "KEY", help = "Get the value for the key").convert { ConfigAction.Get(it) },
      option("--unset", metavar = "KEY", help = "Remove the configuration that matches the key").convert {
         ConfigAction.Unset(it)
      },
   ).single()

   init {
      eagerOption(name = "--list", help = "List all key/value pairs") {
         throw PrintMessage(
            message = if (App.config.isEmpty) {
               "No configurations found"
            } else {
               App.config.entries.joinToString(separator = System.lineSeparator()) { (key, value) -> "$key=$value" }
            },
         )
      }
   }

   override fun run() {
      when (configAction) {
         is ConfigAction.Set -> {
            App.config.apply {
               val stringPair = (configAction as ConfigAction.Set).stringPair
               setProperty(/* key = */ stringPair.first, /* value = */ stringPair.second)
               App.configFile.outputStream().use { store(/* out = */ it, /* comments = */ null) }
            }
         }

         is ConfigAction.Get -> {
            echo(
               message = App.config.getProperty(/* key = */ (configAction as ConfigAction.Get).key)
                  ?: throw ProgramResult(statusCode = 1),
            )
         }

         is ConfigAction.Unset -> {
            App.config.apply {
               remove(/* key = */ (configAction as ConfigAction.Unset).key)
               App.configFile.outputStream().use { store(/* out = */ it, /* comments = */ null) }
            }
         }

         else -> throw ProgramResult(statusCode = 1) // Should never happen though
      }
   }
}

private sealed class ConfigAction {
   data class Set(val stringPair: Pair<String, String>) : ConfigAction()
   data class Get(val key: String) : ConfigAction()
   data class Unset(val key: String) : ConfigAction()
}
