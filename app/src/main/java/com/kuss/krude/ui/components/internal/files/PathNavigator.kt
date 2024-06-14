package com.kuss.krude.ui.components.internal.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Stack


class PathNavigator() {
    private val backStack = Stack<String>()
    private val forwardStack = Stack<String>()
    var currentPath by mutableStateOf("")
    var canGoBack by mutableStateOf(false)
    var canForward by mutableStateOf(false)

    private fun updateNavigationStates() {
        canForward = forwardStack.isNotEmpty()
        canGoBack = backStack.isNotEmpty()
    }

    fun goTo(page: String) {
        if (currentPath == page) return
        backStack.push(currentPath)
        currentPath = page
        forwardStack.clear()
        updateNavigationStates()
    }

    fun goBack(): String {
        if (!backStack.isEmpty()) {
            forwardStack.push(currentPath)
            currentPath = backStack.pop()
            updateNavigationStates()
        }
        return currentPath
    }

    fun goForward(): String {
        if (!forwardStack.isEmpty()) {
            backStack.push(currentPath)
            currentPath = forwardStack.pop()
            updateNavigationStates()
        }
        return currentPath
    }
}

@Composable
fun rememberPathNavigator(): PathNavigator {
    return remember { PathNavigator() }
}