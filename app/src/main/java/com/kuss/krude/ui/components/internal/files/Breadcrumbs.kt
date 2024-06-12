package com.kuss.krude.ui.components.internal.files

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Breadcrumbs(path: String, onPath: (String) -> Unit, leftContent: @Composable () -> Unit) {
    val breadcrumbs = path.split("/").filter { it.isNotEmpty() }
    Surface(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leftContent()
            for ((index, breadcrumb) in breadcrumbs.withIndex()) {
                // /storage/emulated
                if (index < 2) {
                    continue
                }
                // /storage/emulated/0
                val isRoot = index == 2
                TextButton(onClick = {
                    onPath(breadcrumbs.subList(0, index + 1).joinToString("/"))
                }) {
                    Text(text = if (isRoot) "~" else breadcrumb,
                        style = if (isRoot) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.let {
                            if (index == breadcrumbs.lastIndex) it.primary else it.secondary
                        })
                }
                Text(
                    "/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}