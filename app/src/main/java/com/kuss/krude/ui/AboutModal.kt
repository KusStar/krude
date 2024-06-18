package com.kuss.krude.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.kuss.krude.BuildConfig
import com.kuss.krude.R
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.utils.SponsorHelper
import com.kuss.krude.utils.ToastUtils
import com.kuss.krude.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutModal(visible: Boolean, settingsViewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val settingsState by settingsViewModel.state.collectAsState()
    val devMode = settingsState.devMode

    var devClick by remember {
        mutableIntStateOf(0)
    }

    if (visible) {
        ModalBottomSheet(
            onDismissRequest = {
                onDismiss()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "icon",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (devClick >= 7) {
                                    if (devMode) {
                                        ToastUtils.show(context, "已退出开发者模式")
                                    } else {
                                        ToastUtils.show(context, "已加入开发者模式")
                                    }
                                    settingsViewModel.setDevMode(!devMode)
                                    devClick = 0
                                } else {
                                    devClick += 1
                                }
                            }),
                    colorFilter = if (devMode) ColorFilter.tint(
                        Color.Yellow,
                        blendMode = BlendMode.Darken
                    ) else null
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = BuildConfig.VERSION_NAME,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 16.sp,
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

                var showLicenseModal by remember {
                    mutableStateOf(false)
                }

                var showSponsorModal by remember {
                    mutableStateOf(false)
                }

                var showReportModal by remember {
                    mutableStateOf(false)
                }

                val uriHandler = LocalUriHandler.current

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = stringResource(id = R.string.sponsor)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.sponsor)) },
                    onClick = {
                        showSponsorModal = true
                    },
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = stringResource(id = R.string.feedback_issue)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.feedback_issue)) },
                    onClick = {
                        showReportModal = true
                    },
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = stringResource(id = R.string.update)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.update)) },
                    onClick = {
                        uriHandler.openUri("https://github.com/KusStar/krude/releases")
                    },
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = stringResource(id = R.string.open_source_license)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.open_source_license)) },
                    onClick = {
                        showLicenseModal = true
                    },
                )

                if (showSponsorModal) {
                    var toWechatDialog by remember {
                        mutableStateOf(false)
                    }
                    val sponsorSheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )

                    val coroutineScope = rememberCoroutineScope()
                    ModalBottomSheet(
                        onDismissRequest = {
                            showSponsorModal = false
                        },
                        sheetState = sponsorSheetState,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                                .fillMaxSize()
                                .padding()
                        ) {
                            Text(
                                text = stringResource(id = R.string.sponsor),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacing(x = 2)
                            Image(
                                painter = painterResource(id = R.drawable.wechat_reward),
                                contentDescription = "wechat_reward",
                                Modifier
                                    .size(256.dp)
                                    .clip(
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacing(x = 1)
                            Button(onClick = {
                                coroutineScope.launch {
                                    withContext(IO) {
                                        SponsorHelper.saveWechatSponsor(context)
                                        withContext(Main) {
                                            Toast.makeText(
                                                context,
                                                "已保存到相册",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            toWechatDialog = true
                                        }
                                    }
                                }
                            }) {
                                Text(stringResource(id = R.string.save_wechat_sponsor))
                            }
                            Spacing(x = 1)
                            Button(onClick = {
                                uriHandler.openUri(SponsorHelper.ALIPAY_SPONSOR_URI)
                            }) {
                                Text(stringResource(id = R.string.jump_alipay_sponsor))
                            }
                        }
                    }

                    if (toWechatDialog) {
                        AlertDialog(
                            title = {
                                Text(text = "保存成功！")
                            },
                            text = {
                                Text(text = "请打开微信-扫一扫-相册，选择刚刚保存的赞赏码，赞赏开发者")
                            },
                            onDismissRequest = {
                                toWechatDialog = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        ActivityHelper.openWechatScan(context)
                                    }
                                ) {
                                    Text("跳转到微信")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        toWechatDialog = false
                                    }
                                ) {
                                    Text("关闭")
                                }
                            }
                        )
                    }
                }

                if (showReportModal) {
                    val reportSheetState = rememberModalBottomSheetState()
                    ModalBottomSheet(
                        onDismissRequest = {
                            showReportModal = false
                        },
                        sheetState = reportSheetState,
                        modifier = ModalSheetModifier
                    ) {
                        Column(verticalArrangement = Arrangement.Center) {
                            SettingsMenuLink(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = stringResource(id = R.string.github_issues)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.github_issues)) },
                                onClick = {
                                    uriHandler.openUri("https://github.com/KusStar/krude/issues")
                                },
                            )
                            SettingsMenuLink(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Mail,
                                        contentDescription = stringResource(id = R.string.open_source_license)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.send_email)) },
                                onClick = {
                                    uriHandler.openUri("mailto:kussssss@outlook.com")
                                },
                            )
                        }
                    }
                }

                if (showLicenseModal) {
                    val licenseSheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )
                    ModalBottomSheet(
                        onDismissRequest = {
                            showLicenseModal = false
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
