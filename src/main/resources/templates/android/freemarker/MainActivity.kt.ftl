<#--
 ~ Copyright (c) 2023 Mayeku Khisa
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 -->
package ${packageName}

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import ${packageName}.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      // This allows drawing behind the status and navigation bars.
      WindowCompat.setDecorFitsSystemWindows(window, false)

      setContent {
         AppTheme {
            Box(
               modifier = Modifier
                  .fillMaxSize()
                  .background(color = MaterialTheme.colorScheme.background),
            ) {
               Catchphrase(modifier = Modifier.align(Alignment.Center))
            }
         }
      }
   }
}

@Composable
fun Catchphrase(modifier: Modifier = Modifier) {
   Text(
      text = "Catchphrase 😎",
      color = MaterialTheme.colorScheme.onBackground,
      style = MaterialTheme.typography.headlineMedium,
      modifier = modifier,
   )
}

@Preview(showBackground = true)
@Composable
fun CatchphrasePreview() {
   AppTheme {
      Catchphrase()
   }
}
