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

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import org.apache.commons.io.FileUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateTest {
   private val originalStdOut = System.out
   private val testOutputStream = ByteArrayOutputStream()

   private val tempDir = FileUtils.getTempDirectory()
      .resolve("skeleton-test")
      .also { it.mkdir() }

   @BeforeTest
   fun setUp() {
      System.setOut(PrintStream(testOutputStream))
   }

   @Test
   fun `fails on existing project directories`() {
      assertFailsWith<ProgramResult> { Create().parse(arrayOf(".")) }
      assertFailsWith<ProgramResult> { Create().parse(arrayOf("$tempDir")) }
   }

   @Test
   fun `generates blank projects`() {
      var projectDir = tempDir.resolve("default-project")
      with(Create()) {
         parse(arrayOf("$projectDir"))
         checkGeneratedFiles(projectDir)
      }

      projectDir = tempDir.resolve("blank-project")
      with(Create()) {
         parse(arrayOf("--template", "blank", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }
   }

   @Test
   fun `generates kotlin projects`() {
      var projectDir = tempDir.resolve("default-kotlin-project")
      with(Create()) {
         parse(arrayOf("--template", "kotlin", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }

      projectDir = tempDir.resolve("custom-kotlin-project")
      with(Create()) {
         parse(arrayOf("--template", "kotlin", "--package", "com.mycompany.myapp", "--name", "My App", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }
   }

   @Test
   fun `fails on invalid package names`() {
      val projectDir = tempDir.resolve("custom-kotlin-project")
      assertFailsWith<UsageError> {
         Create().parse(arrayOf("--template", "kotlin", "--package", "com.myCompany.myApp", "$projectDir"))
      }
      assertFailsWith<UsageError> {
         Create().parse(arrayOf("--template", "kotlin", "--package", "com.my_company.my_app", "$projectDir"))
      }
   }

   @Test
   fun `generates nextjs projects`() {
      var projectDir = tempDir.resolve("default-nextjs-project")
      with(Create()) {
         parse(arrayOf("--template", "nextjs", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }

      projectDir = tempDir.resolve("custom-nextjs-project")
      with(Create()) {
         parse(arrayOf("--template", "nextjs", "--name", "My App", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }
   }

   @Test
   fun `generates nextjs + tailwindcss projects`() {
      var projectDir = tempDir.resolve("default-nextjs+tailwindcss-project")
      with(Create()) {
         parse(arrayOf("--template", "nextjs+tailwindcss", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }

      projectDir = tempDir.resolve("custom-nextjs+tailwindcss-project")
      with(Create()) {
         parse(arrayOf("--template", "nextjs+tailwindcss", "--name", "My App", "$projectDir"))
         checkGeneratedFiles(projectDir)
      }
   }

   @AfterTest
   fun tearDown() {
      System.setOut(originalStdOut)
      /*
       To manually inspect the output of the test, uncomment the following line.
       However, You'll need to clean the temp directory to avoid test failures in subsequent runs.
       */
      FileUtils.cleanDirectory(tempDir)
   }

   private fun Create.checkGeneratedFiles(projectDir: File) {
      getTemplateManifest().textFiles.forEach {
         val generatedFile = projectDir.resolve(it.dest)
         assert(generatedFile.exists()) { "Expected $generatedFile to exist" }
         assertEquals(
            it.executable,
            generatedFile.canExecute(),
            "Expected $generatedFile to ${if (it.executable) "be executable" else "not be executable"}",
         )
      }
   }
}
