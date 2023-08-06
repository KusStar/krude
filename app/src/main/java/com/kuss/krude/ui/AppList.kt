package com.kuss.krude.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.kuss.krude.R
import com.kuss.krude.data.AppInfoWithIcon
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.AppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppList() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    var items by remember {
        mutableStateOf(listOf<AppInfoWithIcon>())
    }
    var filteredItems by remember {
        mutableStateOf(listOf<AppInfoWithIcon>())
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            items = AppHelper.getInstalled(context)
        }
    }

    val headers by remember {
        derivedStateOf {
            items.map { it.abbr.first().uppercase() }.toSet().toList().sorted()
        }
    }
    var selectedHeaderIndex by remember { mutableIntStateOf(0) }

    var filtering by remember { mutableStateOf("") }


    val sheetState = rememberModalBottomSheetState()
    var showAppDetailSheet by remember { mutableStateOf(false) }
    var selectedDetailApp by remember { mutableStateOf<AppInfoWithIcon?>(null) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun openApp(packageName: String) {
        val intent = context
            .packageManager.getLaunchIntentForPackage(packageName)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: return

        ActivityCompat.startActivity(
            context,
            intent,
            null,
        )

        filtering = ""
    }


    fun toAppDetail(item: AppInfoWithIcon) {
        selectedDetailApp = item
        showAppDetailSheet = true

        focusManager.clearFocus()
    }

    fun openAppInfo(item: AppInfoWithIcon) {
        ActivityHelper.toDetail(context, item.packageName)
    }

    fun uninstallApp(item: AppInfoWithIcon) {
        ActivityHelper.toUninstall(context, item.packageName)
    }

    Row {
        val listState = rememberLazyStaggeredGridState()

        val firstVisibleItemIndex by remember {
            derivedStateOf { listState.firstVisibleItemIndex }
        }

        val offsets = remember { mutableStateMapOf<Int, Float>() }

        fun updateSelectedIndexIfNeeded(offset: Float) {
            val index = offsets
                .mapValues { abs(it.value - offset) }
                .entries
                .minByOrNull { it.value }
                ?.key ?: return
            if (selectedHeaderIndex == index) return
            selectedHeaderIndex = index
            val selectedItemIndex = items.indexOfFirst {
                it.abbr.first().uppercase() == headers[selectedHeaderIndex]
            }
            scope.launch {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                listState.scrollToItem(selectedItemIndex)
            }
        }

        LaunchedEffect(firstVisibleItemIndex) {
            val next = headers.indexOfFirst {
                it == items[firstVisibleItemIndex + 1].abbr.first().uppercase()
            }

            selectedHeaderIndex = next
        }

        Column(modifier = Modifier.weight(1f)) {
            LazyVerticalStaggeredGrid(
                state = listState,
                columns = StaggeredGridCells.Adaptive(128.dp),
                // content padding
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
                content = {
                    if (items.isNotEmpty()) {
                        items(items.size) { index ->
                            val item = items[index]

                            AppItem(item = item, onClick = {
                                openApp(item.packageName)
                            }, onLongClick = {
                                toAppDetail(item)
                            })
                        }
                    } else {
                        items(16) {
                            AppItemShimmer()
                        }
                    }
                }
            )

            if (filtering.isNotEmpty()) {
                Divider()
                if (filteredItems.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        itemsIndexed(filteredItems) { index, item ->
                            AppItem(
                                modifier = Modifier.width(96.dp),
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
                            .background(MaterialTheme.colorScheme.error),
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
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            Divider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
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
                        filtering = text
                        filteredItems = if (items.isNotEmpty())
                        // TODO: options for fuzzy search and exact search
//                            items.filter {
//                                it.filterTarget.contains(
//                                    text,
//                                    ignoreCase = true
//                                )
//                            }
                            items
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
                                    it.second > 60
                                }
                                .sortedByDescending { it.second }
                                .map {
                                    it.first
                                }
                        else emptyList()
                    },
                    placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
                )
                if (filtering.isNotEmpty()) {
                    Spacing(1)
                    IconButton(onClick = { filtering = "" }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectTapGestures {
                        updateSelectedIndexIfNeeded(it.y)
                    }
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        updateSelectedIndexIfNeeded(change.position.y)
                    }
                }
                .padding(4.dp)
        ) {
            headers.forEachIndexed { i, header ->
                val active = i == selectedHeaderIndex
                Text(
                    header,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .onGloballyPositioned {
                            offsets[i] = it.boundsInParent().center.y
                        }
                        .alpha(if (active) 1f else 0.6f)
                )
            }
        }

        if (showAppDetailSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAppDetailSheet = false
                },
                sheetState = sheetState,
                dragHandle = {
                    AnimatedContent(
                        targetState = sheetState.currentValue,
                        label = "DragHandle",
                        transitionSpec = {
                            scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(220, delayMillis = 90)
                            ).togetherWith(
                                fadeOut(animationSpec = tween(90))
                            )
                        }) {
                        when (it) {
                            SheetValue.Expanded -> BottomSheetDefaults.DragHandle(
                                modifier = Modifier.padding(
                                    vertical = 20.dp
                                )
                            )
                            else -> BottomSheetDefaults.DragHandle()
                        }
                    }
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    selectedDetailApp?.let { app ->
                        val info = context.packageManager.getPackageInfo(
                            app.packageName,
                            PackageManager.GET_ACTIVITIES
                        )

                        AppItem(
                            item = app,
                            iconSize = 64.dp,
                            titleFontSize = 20.sp, subtitleFontSize = 16.sp,
                            enabled = false
                        )
                        // extra info
                        Row {
                            Text(
                                text = info.versionName,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp,
                            )
                            val versionCode =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toString() else info.versionCode.toString()
                            Text(
                                text = "($versionCode)",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 14.sp,
                            )
                        }
                        Spacing(3)
                        // btns
                        Row {
                            Button(onClick = {
                                openAppInfo(app)
                            }) {
                                val text = stringResource(id = R.string.app_info)
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = text,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacing(1)
                                Text(text = text)
                            }
                            Spacing(2)
                            Button(onClick = {
                                uninstallApp(app)
                            }) {
                                val text = stringResource(id = R.string.uninstall_app)
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = text,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacing(1)
                                Text(text = text)
                            }
                        }

                        Spacing(3)
                        val activitiesListState = rememberLazyListState()
                        if (info.activities.isNotEmpty()) {
                            LazyColumn(state = activitiesListState) {
                                stickyHeader {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.background,
                                                RoundedCornerShape(16.dp)
                                            )
                                    ) {
                                        TextButton(onClick = {
                                            scope.launch {
                                                activitiesListState.animateScrollToItem(0)
                                            }
                                        }) {
                                            Text(
                                                text = stringResource(id = R.string.all_activities),
                                                fontSize = 16.sp,
                                            )
                                        }
                                    }
                                }
                                items(info.activities.filter { it.exported && it.enabled }) {
                                    TextButton(onClick = { openApp(it.packageName) }) {
                                        Column {
                                            Text(
                                                text = it.name,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 16.sp,
                                            )
                                            Text(
                                                text = it.packageName,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontSize = 12.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

//                    Button(onClick = {
//                        scope.launch { sheetState.hide() }.invokeOnCompletion {
//                            if (!sheetState.isVisible) {
//                                showAppDetailSheet = false
//                            }
//                        }
//                    }) {
//                        Text("Close")
//                    }
                }

            }
        }
    }
}