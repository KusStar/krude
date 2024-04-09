package com.kuss.krude.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.krude.R
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.utils.ModalSheetModifier

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutModal(visible: Boolean, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    if (visible) {
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "icon",
                    modifier = Modifier.size(128.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacing(x = 1)

                val urlHandler = LocalUriHandler.current

//                developer info
                val developerString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(stringResource(R.string.created_by))
                    }

                    pushStringAnnotation(tag = "KusStar", annotation = "https://github.com/KusStar")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("@KusStar")
                    }
                    pop()
                }


                ClickableText(
                    text = developerString,
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        developerString.getStringAnnotations(
                            tag = "KusStar",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            urlHandler.openUri(it.item)
                        }
                    })
                Spacing(x = 1)
//                open source info
                val ossString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(stringResource(R.string.open_sourced_at))
                    }

                    pushStringAnnotation("krude", "https://github.com/KusStar/krude")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("GitHub/KusStar/krude")
                    }
                    pop()
                }

                ClickableText(
                    text = ossString,
                    style = MaterialTheme.typography.bodyLarge,
                    onClick = { offset ->
                        ossString.getStringAnnotations(tag = "krude", start = offset, end = offset)
                            .firstOrNull()?.let {
                            urlHandler.openUri(it.item)
                        }
                    })

                Spacing(x = 2)

                val showLicenseModal = remember {
                    mutableStateOf(false)
                }

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = stringResource(id = R.string.open_source_license)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.open_source_license)) },
                    onClick = {
                          showLicenseModal.value = true
                    },
                )

                if (showLicenseModal.value) {
                    val licenseSheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )
                    ModalBottomSheet(
                        onDismissRequest = {
                            showLicenseModal.value = false
                        },
                        sheetState = licenseSheetState,
                        modifier = ModalSheetModifier
                    ) {
                        AndroidView(factory = {
                            WebView(it).apply {
                                settings.javaScriptEnabled = true
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                            }
                        }, update = {
                            it.loadUrl("file:///android_asset/open_source_licenses.html")
                        },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}
