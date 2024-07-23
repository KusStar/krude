package com.kuss.krude.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kuss.krude.R
import me.saket.cascade.CascadeDropdownMenu

enum class AppDropdownType {
    OPEN_IN_FREEFORM_WINDOW, STAR, HIDE, APP_STATS, UNINSTALL, APP_INFO,
}

@Composable
fun AppItemDropdowns(
    visible: Boolean, onDismiss: () -> Unit, onDropdown: (AppDropdownType) -> Unit
) {
    CascadeDropdownMenu(expanded = visible, onDismissRequest = { onDismiss() }) {
        OnOpenInFreeformDropdown {
            onDropdown(AppDropdownType.OPEN_IN_FREEFORM_WINDOW)
            onDismiss()
        }
        OnStarDropdown {
            onDropdown(AppDropdownType.STAR)
            onDismiss()
        }
        OnHideDropdown {
            onDropdown(AppDropdownType.HIDE)
            onDismiss()
        }
        OnAppInfoDropdown {
            onDropdown(AppDropdownType.APP_INFO)
            onDismiss()
        }
        OnAppStatsDropdown {
            onDropdown(AppDropdownType.APP_STATS)
            onDismiss()
        }
        OnUninstallDropdown {
            onDropdown(AppDropdownType.UNINSTALL)
            onDismiss()
        }
    }
}

enum class ExtensionDropdownType {
    STAR, OPEN_IN_FREEFORM_WINDOW
}

@Composable
fun ExtensionItemDropdowns(
    visible: Boolean, onDismiss: () -> Unit, onDropdown: (ExtensionDropdownType) -> Unit
) {
    CascadeDropdownMenu(expanded = visible, onDismissRequest = { onDismiss() }) {
        OnStarDropdown {
            onDropdown(ExtensionDropdownType.STAR)
            onDismiss()
        }
        OnOpenInFreeformDropdown {
            onDropdown(ExtensionDropdownType.OPEN_IN_FREEFORM_WINDOW)
            onDismiss()
        }
    }
}

@Composable
fun OnHideDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.HideSource,
            contentDescription = stringResource(id = R.string.hide),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }, text = { Text(text = stringResource(id = R.string.hide)) }, onClick = {
        onClick()
    })
}

@Composable
fun OnUninstallDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.Delete,
            contentDescription = stringResource(id = R.string.uninstall_app),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }, text = { Text(text = stringResource(id = R.string.uninstall_app)) }, onClick = {
        onClick()
    })
}

@Composable
fun OnStarDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.Star,
            contentDescription = stringResource(id = R.string.star),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }, text = { Text(text = stringResource(id = R.string.star)) }, onClick = {
        onClick()
    })
}

@Composable
fun OnOpenInFreeformDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.PictureInPicture,
            contentDescription = stringResource(id = R.string.open_in_freeform_window),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    },
        text = { Text(text = stringResource(id = R.string.open_in_freeform_window)) },
        onClick = {
            onClick()
        })
}

@Composable
fun OnAppStatsDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.Troubleshoot,
            contentDescription = stringResource(id = R.string.app_stats),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }, text = { Text(text = stringResource(id = R.string.app_stats)) }, onClick = {
        onClick()
    })
}

@Composable
fun OnAppInfoDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.Info,
            contentDescription = stringResource(id = R.string.app_info),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }, text = { Text(text = stringResource(id = R.string.app_info)) }, onClick = {
        onClick()
    })
}