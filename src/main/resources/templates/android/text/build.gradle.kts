import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep

plugins {
   id("com.android.application") version "7.3.1" apply false
   id("com.android.library") version "7.3.1" apply false
   id("com.diffplug.spotless") version "6.12.0"
   kotlin("android") version "1.7.20" apply false
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
      kotlin {
         target("**/*.kt")
         targetExclude("**/build/**/*.kt")
         ktlint().editorConfigOverride(it)
      }

      kotlinGradle {
         target("**/*.kts")
         targetExclude("**/build/**/*.kts")
         ktlint().editorConfigOverride(it)
      }
   }

   json {
      target("**/*.json")
      targetExclude("**/build/**/*.json")
      prettier().config(
         mapOf(
            "printWidth" to maxLineLength,
            "tabWidth" to indentSpaces,
         ),
      )
   }

   format("XML") {
      target("**/*.xml")
      targetExclude("**/build/**/*.xml")
      eclipseWtp(EclipseWtpFormatterStep.XML).configFile(rootProject.file("spotless/xml.prefs"))
   }
}
