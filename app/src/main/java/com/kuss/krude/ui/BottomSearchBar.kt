package com.kuss.krude.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.data.AppInfoWithIcon
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch


@Composable
fun BottomSearchBar(
    mainViewModel: MainViewModel,
    openApp: (String) -> Unit,
    toAppDetail: (AppInfoWithIcon) -> Unit
) {
    val uiState by mainViewModel.state.collectAsState()
    val apps = uiState.apps
    val filtering = uiState.filtering
    val filteredApps = uiState.filteredApps

    val scope = rememberCoroutineScope()
    val focusRequester = remember {
        FocusRequester()
    }
    AnimatedVisibility(
        visible = filtering.isNotEmpty(),
    ) {
        Divider()
        Crossfade(targetState = filteredApps.isNotEmpty(), label = "filteredItems") {
            val height = 108.dp
            if (it) {
                LazyRow(
                    modifier = Modifier
                        .height(height)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(filteredApps) { _, item ->
                        AppItem(
                            modifier = Modifier
                                .width(96.dp),
                            item = item,
                            titleFontSize = 14.sp,
                            titleSingleLine = true,
                            showSubtitle = false,
                            onClick = {
                                openApp(item.packageName)
                            },
                            onLongClick = {
                                toAppDetail(item)
                            }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.no_match_app),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }


    Divider()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = filtering.isNotEmpty()) {
            IconButton(onClick = { mainViewModel.setFiltering("") }) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
        TextField(
            enabled = apps.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(focusRequester),
            value = filtering,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.primary
            ),
            onValueChange = { text ->
                mainViewModel.setFiltering(text)
                scope.launch {
                    val next = if (apps.isNotEmpty())
                    // TODO: options for fuzzy search and exact search
//                            items.filter {
//                                it.filterTarget.contains(
//                                    text,
//                                    ignoreCase = true
//                                )
//                            }
                        apps
                            .map {
                                val ratio = FuzzySearch.partialRatio(
                                    it.abbr.lowercase(),
                                    text.lowercase()
                                ) + FuzzySearch.partialRatio(
                                    it.filterTarget.lowercase(),
                                    text.lowercase()
                                )
                                Pair(
                                    it,
                                    ratio
                                )
                            }
                            .filter {
                                it.second > 80
                            }
                            .sortedByDescending { it.second }
                            .map {
                                it.first
                            }
                    else emptyList()

                    mainViewModel.setFilteredApps(next)
                }
            },
            placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
        )
    }
}

