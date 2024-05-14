package com.kuss.krude.ui

import android.icu.text.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
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
import com.kuss.krude.db.Hidden
import com.kuss.krude.ui.components.AsyncAppIcon
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenTableModal(
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
        LaunchedEffect(true) {
            withContext(Dispatchers.IO) {
                hiddenList.addAll(mainViewModel.getHiddenList(context))
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss.invoke()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(128.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                itemsIndexed(hiddenList) { index, hidden ->
                    OutlinedCard {
                        Column(Modifier.padding(12.dp)) {
                            AsyncAppIcon(
                                packageName = hidden.key, modifier = Modifier
                                    .size(32.dp)
                            )
                            Text(
                                text = hidden.key,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = DateFormat.getInstance().format(hidden.createdAt),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacing(x = 1)
                            OutlinedButton(onClick = {
                                mainViewModel.deleteHidden(context, hidden)
                                hiddenList.remove(hidden)
                            }) {
                                Text(text = stringResource(id = R.string.delete))
                            }
                        }
                    }

                    if (index < hiddenList.size - 1) {
                        Spacing(x = 1)
                    }
                }
            }
        }
    }
}