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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuss.krude.R
import com.kuss.krude.db.Star
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.InternalExtensionIcon
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.ui.components.search.ExtensionIcon
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarListModal(
    mainViewModel: MainViewModel,
    visible: Boolean, onDismiss: () -> Unit
) {

    if (visible) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val sheetState = rememberModalBottomSheetState()

        val stars = remember {
            mutableStateListOf<Star>()
        }
        val packageNameLabelMap = remember {
            mutableStateMapOf<String, String>()
        }
        val extensionIdMap = remember {
            mutableStateMapOf<String, Extension>()
        }

        fun loadStars() {
            scope.launch {
                withContext(IO) {
                    val allStars = mainViewModel.getAllStars(context)
                    Timber.d("getAllStars, ${allStars.size}")
                    stars.addAll(allStars)
                    mainViewModel.state.value.originalApps.forEach {
                        packageNameLabelMap[it.packageName] = it.label
                    }
                    mainViewModel.getExtensionsWithInternal().forEach {
                        extensionIdMap[it.id] = it
                    }
                }
            }
        }
        LaunchedEffect(true) {
            loadStars()
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
                    text = "${stringResource(id = R.string.star_table)} ${stars.size}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacing(x = 1)
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(stars, key = { it.key }) { star ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            val hasApp = remember {
                                packageNameLabelMap.containsKey(star.key)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasApp) {
                                    AsyncAppIcon(
                                        packageName = star.key, modifier = Modifier
                                            .size(48.dp)
                                    )
                                } else {
                                    if (extensionIdMap.containsKey(star.key)) {
                                        val extension = extensionIdMap[star.key]!!
                                        if (extension.type == ExtensionType.INTERNAL) {
                                            InternalExtensionIcon(extension, 48.dp)
                                        } else {
                                            ExtensionIcon(48.dp)
                                        }
                                    }
                                }
                                Spacing(x = 1)
                                IconButton(onClick = {
                                    mainViewModel.deleteStar(context, star)
                                    stars.remove(star)
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
                                    text = packageNameLabelMap[star.key]!!,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            } else {
                                extensionIdMap[star.key]?.let {
                                    Text(
                                        text = it.name,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                            Text(
                                text = star.key,
                                color = MaterialTheme.colorScheme.let {
                                    if (hasApp) it.secondary else it.primary
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = DateFormat.getInstance().format(star.createdAt),
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