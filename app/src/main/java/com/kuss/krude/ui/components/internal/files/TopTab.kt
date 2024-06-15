package com.kuss.krude.ui.components.internal.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kuss.krude.utils.ToastUtils
import me.saket.cascade.CascadeDropdownMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTab(
    onBack: () -> Unit,
    selectedTabIndex: Int,
    changeTab: (Int) -> Unit,
    openedTabs: SnapshotStateList<String>,
    goToPath: (String) -> Unit,
    newTab: (String) -> Unit,
    closeTab: (Int) -> Unit,
    pathNavigator: PathNavigator,
    closeAllTabs: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            onBack()
        }) {
            Icon(
                Icons.AutoMirrored.Default.ExitToApp,
                contentDescription = "Back to First Page",
                modifier = Modifier
                    .size(ButtonDefaults.IconSize)
                    .graphicsLayer {
                        rotationZ = 180f
                    },
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        ScrollableTabRow(
            modifier = Modifier.weight(1f),
            edgePadding = 0.dp,
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            divider = {}) {
            openedTabs.forEachIndexed { index, path ->
                val active = selectedTabIndex == index
                Tab(modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp),
                    selected = active,
                    onClick = {
                        changeTab(index)
                        goToPath(path)
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (path.startsWith(FileHelper.PATH_PREFIX)) FileHelper.formatPath(
                                    path
                                ) else path.ifEmpty { "~" },
                                modifier = Modifier.widthIn(max = 128.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                            AnimatedVisibility(index > 0 && active) {
                                CompositionLocalProvider(
                                    LocalMinimumInteractiveComponentEnforcement provides false,
                                ) {
                                    IconButton(
                                        onClick = {
                                            closeTab(index)
                                        },
                                        Modifier
                                            .padding(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier
                                                .size(ButtonDefaults.IconSize)
                                        )
                                    }
                                }
                            }
                        }
                    })
            }
        }
        IconButton(onClick = {
            newTab(pathNavigator.currentPath)
        }) {
            Icon(
                Icons.Default.Add,
                contentDescription = "New Tab",
                modifier = Modifier
                    .size(ButtonDefaults.IconSize),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        var showMore by remember { mutableStateOf(false) }
        val clipboardManager: ClipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        Box {
            IconButton(onClick = {
                showMore = true
            }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            CascadeDropdownMenu(expanded = showMore, onDismissRequest = {
                showMore = false
            }) {
                DropdownMenuItem(
                    text = {
                        Text("Close all tab")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ClearAll,
                            contentDescription = "Close all tab icon",
                        )
                    },
                    onClick = {
                        closeAllTabs()
                        showMore = false
                    })
                DropdownMenuItem(
                    text = {
                        Text("Copy path")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy path icon",
                        )
                    },
                    onClick = {
                        val currentPath = pathNavigator.currentPath.ifEmpty { FileHelper.ROOT_PATH }

                        clipboardManager.setText(AnnotatedString(currentPath))

                        ToastUtils.show(context, "Path copied to clipboard")

                        showMore = false
                    })
            }
        }
    }
}