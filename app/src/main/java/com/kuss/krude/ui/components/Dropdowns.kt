package com.kuss.krude.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kuss.krude.R

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
fun OnAppDetailDropdown(onClick: () -> Unit) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Default.Info,
            contentDescription = stringResource(id = R.string.app_detail),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    },
        text = { Text(text = stringResource(id = R.string.app_detail)) },
        onClick = {
            onClick()
        })
}