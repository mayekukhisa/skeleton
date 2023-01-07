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

import com.github.ajalt.clikt.core.PrintMessage
import com.mayekukhisa.tool.projectgen.App
import com.mayekukhisa.tool.projectgen.BuildConfig
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateCommandTest {
   private val standardOutputStream = System.out
   private val capturedOutputStream = ByteArrayOutputStream()

   private val tempDir = FileUtils.getTempDirectory()
      .resolve(relative = "${BuildConfig.NAME}-test")
      .also { it.mkdir() }

   @BeforeTest
   fun setUp() {
      System.setOut(PrintStream(capturedOutputStream))
   }

   @Test
   fun `should create basic project`() {
      val projectDir = tempDir.resolve(relative = "basic-project")
      CreateCommand().parse(arrayOf("--template", "basic", "$projectDir"))
      checkGeneratedFiles(projectDir)
   }

   @Test
   fun `should create cli-kt default project`() {
      val projectDir = tempDir.resolve(relative = "cli-kt-default-project")
      CreateCommand().parse(arrayOf("--template", "cli-kt", "$projectDir"))
      checkGeneratedFiles(projectDir)
   }

   @Test
   fun `should create cli-kt customized project`() {
      val projectDir = tempDir.resolve(relative = "cli-kt-customized-project")
      CreateCommand().parse(arrayOf("--template", "cli-kt", "--package", "com.company.project", "$projectDir"))
      checkGeneratedFiles(projectDir)
   }

   @AfterTest
   fun tearDown() {
      System.setOut(standardOutputStream)
      FileUtils.cleanDirectory(tempDir)
   }

   private fun checkGeneratedFiles(projectDir: File) {
      val bashPath = App.config.getProperty(/* key = */ "bash.path")
         ?: throw PrintMessage(message = "Error: Bash path not set", error = true)

      val process = Runtime.getRuntime().exec(/* command = */ bashPath, /* envp = */ null, /* dir = */ projectDir)

      process.outputStream.use {
         IOUtils.write(
            /* data = */ "find . -type f -exec stat -c \"%a %n\" {} \\; | LC_ALL=C sort\n",
            /* output = */ it,
            /* charset = */ Charsets.UTF_8,
         )
      }
      process.waitFor()

      assertEquals(
         expected = IOUtils.resourceToString(
            /* name = */ "/expected-files/${projectDir.name}.txt",
            /* charset = */ Charsets.UTF_8,
         ).replace(oldValue = "\r\n", newValue = "\n"),
         actual = IOUtils.toString(/* input = */ process.inputStream, /* charset = */ Charsets.UTF_8),
      )
   }
}
