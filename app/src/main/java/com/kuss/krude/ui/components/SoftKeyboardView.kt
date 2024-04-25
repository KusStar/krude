package com.kuss.krude.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SoftKeyboardView(
    showLeftSideBackspace: Boolean,
    onBack: () -> Unit,
    onClick: (key: Char, isDeleting: Boolean) -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val bottomKeys = if (showLeftSideBackspace) "-zxcvbnm-" else "zxcvbnm-"
    val keymaps = listOf("qwertyuiop", "asdfghjkl", bottomKeys)

    BackHandler(enabled = true, onBack = onBack)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    ) {
        keymaps.forEach {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                it.toList().forEach {
                    val isDeleting = it == '-'
                    Column(
                        modifier = Modifier
                            .height(56.dp)
                            .width(if (isDeleting && showLeftSideBackspace) 48.dp else 40.dp)
                            .padding(horizontal = if (isDeleting && showLeftSideBackspace) 6.dp else 3.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDeleting)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                            .clickable() {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClick(it, isDeleting)
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isDeleting) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Delete",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = "$it",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }

        bottomContent()
    }
}