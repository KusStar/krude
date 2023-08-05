package com.kuss.krude

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.kuss.krude.data.AppInfoWithIcon
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.PinyinHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PinyinHelper.initDict()

        setContent {
            AppCompatTheme() {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                ) {
                    AppList()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!ActivityHelper.isDefaultLauncher(this)) {
            super.onBackPressed()
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppList() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    var items by remember {
        mutableStateOf(listOf<AppInfoWithIcon>())
    }

    LaunchedEffect(Unit) {
        withContext(IO) {
            items = AppHelper.getInstalled(context)
        }
    }

    val headers by remember { derivedStateOf { items.map { it.abbr.first().uppercase() }.toSet().toList().sorted() } }
    var selectedHeaderIndex by remember { mutableIntStateOf(0) }

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
            val next = headers.indexOfFirst { it == items[firstVisibleItemIndex+1].abbr.first().uppercase() }

            selectedHeaderIndex = next
        }

        LazyVerticalStaggeredGrid(
            state = listState,
            columns = StaggeredGridCells.Adaptive(128.dp),
            modifier = Modifier.weight(1f),
            // content padding
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 16.dp,
                end = 12.dp,
                bottom = 12.dp
            ),
            content = {
                items(items.size) { index ->
                    val item = items[index]
                    TextButton(
                        onClick = {
                            val intent = context
                                .packageManager.getLaunchIntentForPackage(item.packageName)
                                ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                ?: return@TextButton

                            ActivityCompat.startActivity(
                                context,
                                intent,
                                null,
                            )
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                bitmap = item.icon.asImageBitmap(),
                                contentDescription = "icon",
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = item.label,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = item.packageName,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }

                    }
                }
            }
        )

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
                .padding(end = 8.dp)
        ) {
            headers.forEachIndexed { i, header ->
                val active = i == selectedHeaderIndex
                Text(
                    header,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .onGloballyPositioned {
                            offsets[i] = it.boundsInParent().center.y
                        }
                        .alpha(if (active) 1f else 0.6f)
                )
            }
        }
    }
}

