package com.kuss.krude.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuss.krude.ui.components.search.AsyncAppIcon

@Composable
fun StackedAppIcons(packageNames: List<String>) {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box {
            packageNames.forEachIndexed { index, packageName ->
                Box(modifier = Modifier.offset(x = 8.dp * index)) {
                    AsyncAppIcon(packageName = packageName,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        }
    }
}