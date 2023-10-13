package com.kuss.krude.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kuss.krude.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppUsageModal(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val uiState by mainViewModel.state.collectAsState()
    val showAppUsageSheet = uiState.showAppUsageSheet

    if (showAppUsageSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                mainViewModel.setShowAppUsageSheet(false)
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
                UsageHeatMap(mainViewModel)
            }
        }
    }
}