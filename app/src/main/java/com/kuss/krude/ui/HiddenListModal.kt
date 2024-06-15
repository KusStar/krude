package com.kuss.krude.ui

import android.icu.text.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuss.krude.R
import com.kuss.krude.db.Hidden
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.ui.components.search.ExtensionIcon
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenListModal(
    mainViewModel: MainViewModel,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        val context = LocalContext.current
        val sheetState = rememberModalBottomSheetState()
        val hiddenList = remember {
            mutableStateListOf<Hidden>()
        }
        val packageNameToNameMap = remember {
            mutableMapOf<String, String>()
        }
        LaunchedEffect(true) {
            withContext(Dispatchers.IO) {
                hiddenList.addAll(mainViewModel.getHiddenList(context))
                mainViewModel.state.value.originalApps.forEach {
                    packageNameToNameMap[it.packageName] = it.label
                }
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss.invoke()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "${stringResource(id = R.string.hidden_list)} ${hiddenList.size}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacing(x = 1)
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(hiddenList, key = { it.key }) { hidden ->
                        Column(Modifier.padding(vertical = 6.dp)) {
                            val hasApp = remember {
                                packageNameToNameMap.containsKey(hidden.key)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasApp) {
                                    AsyncAppIcon(
                                        packageName = hidden.key, modifier = Modifier
                                            .size(48.dp)
                                    )
                                } else {
                                    ExtensionIcon(iconSize = 48.dp)
                                }
                                Spacing(x = 1)
                                IconButton(onClick = {
                                    mainViewModel.deleteHidden(context, hidden)
                                    hiddenList.remove(hidden)
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            if (hasApp) {
                                Text(
                                    text = packageNameToNameMap[hidden.key]!!,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            Text(
                                text = hidden.key,
                                color = MaterialTheme.colorScheme.let {
                                    if (hasApp) it.secondary else it.primary
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = DateFormat.getInstance().format(hidden.createdAt),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}