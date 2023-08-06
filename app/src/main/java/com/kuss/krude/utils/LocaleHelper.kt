package com.kuss.krude.utils

import android.content.Context
import com.kuss.krude.MainActivity
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context? {
        MainActivity.myLang = language
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