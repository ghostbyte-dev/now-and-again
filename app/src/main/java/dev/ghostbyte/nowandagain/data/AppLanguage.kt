package dev.ghostbyte.nowandagain.data

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.annotation.RequiresApi

@RequiresApi(33)
object AppLanguage {
    /** null = follow the system */
    fun current(context: Context): String? {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        return if (locales.isEmpty) null else locales[0].language
    }

    fun set(context: Context, languageTag: String?) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            if (languageTag == null) LocaleList.getEmptyLocaleList()
            else LocaleList.forLanguageTags(languageTag)
    }
}