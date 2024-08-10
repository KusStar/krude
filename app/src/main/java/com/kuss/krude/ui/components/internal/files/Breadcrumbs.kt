package com.kuss.krude.ui.components.internal.files

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kuss.krude.utils.fadingEdge
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Breadcrumbs(path: String, onPath: (String) -> Unit, leftContent: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val breadcrumbs = path.split("/").filter { it.isNotEmpty() }
    val scrollState = rememberLazyListState()
    LaunchedEffect(path) {
        scope.launch {
            scrollState.animateScrollToItem(breadcrumbs.size - 1)
        }
    }
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(end = 8.dp),
        state = scrollState
    ) {
        stickyHeader {
            val rightFade = Brush.horizontalGradient(
                0f to Color.Transparent,
                0f to Color.White,
                0.8f to Color.White,
                1f to Color.Transparent
            )
            Surface(
                Modifier
                    .fillMaxWidth()
                    .fadingEdge(rightFade),
                color = Color.Transparent
            ) {
                leftContent()
            }
        }
        itemsIndexed(breadcrumbs) { index, breadcrumb ->
            // /storage/emulated
            if (index >= 2) {
                // /storage/emulated/0
                val isRoot = index == 2
                TextButton(onClick = {
                    if (isRoot) {
                        onPath(FileHelper.ROOT_PATH)
                    } else {
                        onPath("/" + breadcrumbs.subList(0, index + 1).joinToString("/"))
                    }
                }) {
                    Text(
                        text = if (isRoot) "~" else breadcrumb,
                        style = if (isRoot) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.let {
                            if (index == breadcrumbs.lastIndex) it.primary else it.secondary
                        },
                        maxLines = 1
                    )
                }
                Text(
                    "/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        item {
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}