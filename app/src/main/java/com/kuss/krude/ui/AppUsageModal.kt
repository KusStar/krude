package com.kuss.krude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageModal(mainViewModel: MainViewModel) {
    val sheetState = rememberModalBottomSheetState()

    val uiState by mainViewModel.state.collectAsState()
    val showAppUsageSheet = uiState.showAppUsageSheet

    if (showAppUsageSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                mainViewModel.setShowAppUsageSheet(false)
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
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