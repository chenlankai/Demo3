package bmicalculator.bmi.calculator.weightlosstracker.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

object LocaleUtils {
    private const val PREFS_NAME = "bmi_prefs"
    private const val KEY_LANGUAGE = "selected_language" // 修正 Key 名

    fun setLocale(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    fun applyLocale(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 关键点：如果存储中没有值，返回 null，从而触发 Locale.getDefault().language
        return prefs.getString(KEY_LANGUAGE, null) ?: Locale.getDefault().language
    }

    private fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_LANGUAGE, language)
        }
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = if (language.contains("-")) {
            val parts = language.split("-")
            Locale(parts[0], parts[1].replace("r", ""))
        } else {
            Locale(language)
        }
        
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)

        return context.createConfigurationContext(config)
    }
}
