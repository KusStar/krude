package com.kuss.krude.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.utils.Reverse
import com.kuss.krude.viewmodel.settings.DominantHandDefaults


@Composable
fun CloseBtn(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible = visible) {
        IconButton(onClick = {
            onClick()
        }) {
            Icon(
                Icons.Filled.Clear,
                contentDescription = "Clear",
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun MoreBtns(
    search: String,
    searchResult: List<SearchResultItem>,
    fuzzySearch: Boolean,
    onStarIcon: () -> Unit,
    onFuzzyIcon: () -> Unit,
    onMoreIcon: () -> Unit,
    dominantHand: String
) {
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        Row(
            horizontalArrangement =
            if (dominantHand == DominantHandDefaults.LEFT) Arrangement.Start else Arrangement.Reverse,
        ) {
            AnimatedVisibility(visible = search.isNotEmpty() && searchResult.isNotEmpty()) {
                IconButton(onClick = {
                    onStarIcon()
                }) {
                    Icon(
                        Icons.TwoTone.Star,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Star",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }
            IconButton(onClick = {
                onFuzzyIcon()
            }) {
                Icon(
                    imageVector = if (fuzzySearch) Icons.Filled.BlurOn else Icons.Filled.BlurOff,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "fuzzysearch",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
            IconButton(onClick = { onMoreIcon() }) {
                Icon(
                    Icons.Filled.MoreVert,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "MoreVert",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }
}