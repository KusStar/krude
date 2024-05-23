package com.kuss.krude.utils

import android.content.Context
import timber.log.Timber
import java.util.Locale

object LocaleHelper {
    var currentLocale: String = "zh"

    fun init(context: Context) {
        currentLocale = context.resources.configuration.locales.get(0).language
        Timber.d("currentLocale: $currentLocale")
    }

    fun setLocale(context: Context, language: String): Context? {
        currentLocale = language
        return updateResources(context, language);
    }
    private fun updateResources(context: Context, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}