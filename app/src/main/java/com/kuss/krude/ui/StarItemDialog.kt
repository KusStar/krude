package com.kuss.krude.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kuss.krude.R
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.viewmodel.MainViewModel
import kotlin.time.Duration

data class StarItemState(
    val visible: Boolean = false,
    val item: SearchResultItem? = null
)

@Composable
fun StarItemDialog(
    mainViewModel: MainViewModel,
) {
    val state by mainViewModel.starItemState.collectAsState()
    fun onDismiss() {
        mainViewModel.setStarItemDialogVisible(false)
    }
    if (state.visible) {
        val context = LocalContext.current
        var keyword by remember { mutableStateOf("") }
        Dialog(onDismissRequest = {
            onDismiss()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                state.item?.let { item ->
                    val name = if (item.isApp()) item.asApp()?.label else item.asExtension()?.name
                    val id = item.key()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Star $name with:")
                        Spacing(1)
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = {
                                keyword = it
                            },
                            placeholder = {
                                Text(stringResource(R.string.star_keyword_hint))
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextButton(
                                onClick = {
                                    onDismiss()
                                },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(stringResource(R.string.dismiss))
                            }
                            TextButton(
                                onClick = {
                                    onDismiss()
                                    mainViewModel.insertStar(
                                        context,
                                        mainViewModel.getSettingsState().enableExtension,
                                        id,
                                        keyword = keyword,
                                        false
                                    )
                                    mainViewModel.getMessageBarState()
                                        .showSuccess(
                                            "Starred $name with $keyword",
                                            Duration.parse("2s")
                                        )
                                },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}