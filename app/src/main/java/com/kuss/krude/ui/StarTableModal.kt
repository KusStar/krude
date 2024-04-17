package com.kuss.krude.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuss.krude.R
import com.kuss.krude.db.Star
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .weight(weight)
            .padding(8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarTableModal(
    mainViewModel: MainViewModel,
    visible: Boolean, onDismiss: () -> Unit
) {

    if (visible) {
        val context = LocalContext.current
        val sheetState = rememberModalBottomSheetState()

        val stars = remember {
            mutableStateListOf<Star>()
        }
        LaunchedEffect(true) {
            withContext(IO) {
                val allStars = mainViewModel.getAllStars(context)
                Timber.d("getAllStars, ${allStars.size}")
                stars.addAll(allStars)
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss.invoke()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
        ) {
            val column1Weight = .3f
            val column2Weight = .5f
            val column3Weight = .2f
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                item {
                    Row(Modifier.background(MaterialTheme.colorScheme.surfaceBright)) {
                        TableCell(text = "keyword", weight = column1Weight)
                        TableCell(text = "packageName", weight = column2Weight)
                        Row(modifier = Modifier.weight(column3Weight)) {
                        }
                    }
                }
                items(stars) {
                    Row(Modifier.fillMaxWidth()) {
                        TableCell(text = it.keyword, weight = column1Weight)
                        TableCell(text = it.packageName, weight = column2Weight)
                        Row(modifier = Modifier.weight(column3Weight), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = {
                                mainViewModel.deleteStar(context, it)
                                stars.remove(it)
                            }) {
                                Text(text = stringResource(id = R.string.delete))
                            }
                        }
                    }
                }
            }
        }
    }
}