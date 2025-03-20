package com.kuss.krude.ui.components.ai

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.StackedAppIcons
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.utils.GptData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun GptItem(item: GptData) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBrowser = item.apps.any { it.`package` == "krude.browser.search" }
    if (isBrowser) {
        Spacing(1)
        HorizontalDivider()
        Spacing(1)
    }
    Box {
        var dropDownVisible by remember {
            mutableStateOf(
                false
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    dropDownVisible = true
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.padding(
                    start = 12.dp,
                    end = 0.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
            ) {
                Text(item.search)
            }
            if (isBrowser) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search_engine",
                        modifier = Modifier
                            .size(
                                24.dp
                            )
                    )
                }

            } else {
                StackedAppIcons(item.apps.map { it.`package` })
            }
        }
        CascadeDropdownMenu(
            expanded = dropDownVisible,
            onDismissRequest = {
                dropDownVisible = false
            }) {
            item.apps.forEach { gptApp ->
                DropdownMenuItem(
                    leadingIcon = {
                        if (isBrowser) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "search_engine",
                                modifier = Modifier.size(
                                    ButtonDefaults.IconSize
                                )
                            )
                        } else {
                            AsyncAppIcon(
                                packageName = gptApp.`package`,
                                modifier = Modifier.size(
                                    ButtonDefaults.IconSize
                                )
                            )
                        }
                    },
                    text = { Text(text = "在${gptApp.name}中搜索") },
                    onClick = {
                        dropDownVisible = false
                        scope.launch(IO) {
                            val intent =
                                CustomTabsIntent.Builder()
                                    .build()
                            intent.launchUrl(
                                context,
                                gptApp.scheme.replace(
                                    "queryplaceholder",
                                    item.search
                                ).toUri()
                            )
                        }
                    })
            }
        }
    }
}