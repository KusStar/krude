package com.kuss.krude.utils

import android.text.TextUtils
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import java.lang.reflect.Method
import java.lang.reflect.Modifier


fun findMethodByReflection(classMethod: Class<*>?, methodName: String): Method? {
    return try {
        if (!TextUtils.isEmpty(methodName)) {
            classMethod?.let { clazz ->
                clazz.methods.find { it.name.equals(methodName) && Modifier.isStatic(it.modifiers) }
            } ?: run {
                null
            }
        } else {
            null
        }
    } catch (e: Throwable) {
        null
    }
}

fun loadClassByReflection(className: String): Class<*>? {
    return try {
        val classLoader = ::loadClassByReflection.javaClass.classLoader
        classLoader?.loadClass(className)
    } catch (e: Throwable) {
        null
    }
}

fun invokeMethod(method: Method, obj: Any, vararg args: Any): Boolean {
    return try {
        method.invoke(obj, *(args))
        true
    } catch (e: Throwable) {
        false
    }
}

object DynamicFeatureUtils {
    @Composable
    fun dfCameraView(paddingValues: PaddingValues): Boolean {
        return loadDF(
            paddingValues = paddingValues,
            className = "com.kuss.krude.scanner.CameraViewKt",
            methodName = "CameraViewDF"
        )
    }

    @Composable
    fun ShowDFNotFoundScreen(paddingValues: PaddingValues) {
        Text(
            text = "Dynamic Feature Not Found",
            modifier = androidx.compose.ui.Modifier.padding(paddingValues),
            color = MaterialTheme.colorScheme.primary
        )
    }

    // thanks: https://github.com/TaharJeridi/ComposeDFSample/blob/50f31b202574277184a3e20346e8349996f7a319/app/src/main/java/it/tjeridi/composedfsample/utilities/DynamicFeatureUtils.kt#L44
    @Composable
    private fun loadDF(
        paddingValues: PaddingValues,
        className: String,
        methodName: String,
        objectInstance: Any = Any()
    ): Boolean {
        val dfClass = loadClassByReflection(className)
        if (dfClass != null) {
            val composer = currentComposer
            val method = findMethodByReflection(
                dfClass,
                methodName
            )
            if (method != null) {
                val isMethodInvoked =
                    invokeMethod(method, objectInstance, paddingValues, composer, 0)
                if (!isMethodInvoked) {
                    ShowDFNotFoundScreen(paddingValues)
                    return false
                }
                return true
            } else {
                ShowDFNotFoundScreen(paddingValues)
                return false
            }
        } else {
            ShowDFNotFoundScreen(paddingValues)
            return false
        }
    }
}