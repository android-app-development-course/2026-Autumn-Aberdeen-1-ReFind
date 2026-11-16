package com.refind.app.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * In-app language state, decoupled from the system configuration so a
 * switch is a recomposition — not an Activity recreation.
 *
 * `current` is an observable "en"/"zh" tag; [tr] resolves strings through a
 * locale-patched context. Persisted in shared preferences by [init].
 */
object AppLanguage {
    private const val PREFS = "refind_prefs"
    private const val KEY = "lang"

    var current by mutableStateOf("en")
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY, null)
        current = saved ?: Locale.getDefault().language.ifBlank { "en" }
        if (current != "en" && current != "zh") current = "en"
    }

    fun set(context: Context, lang: String) {
        current = if (lang.startsWith("zh")) "zh" else "en"
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, current).apply()
    }

    fun toggle(context: Context) {
        set(context, if (current == "zh") "en" else "zh")
    }
}

/**
 * Localized string for the in-app language. Reads through a configuration
 * context patched with [AppLanguage.current]; recomposes on language change.
 */
@Composable
fun tr(@StringRes res: Int, vararg args: Any): String {
    val context = LocalContext.current
    val lang = AppLanguage.current
    return remember(res, lang, args) {
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(Locale(lang))
        val localized = context.createConfigurationContext(config)
        localized.resources.getString(res, *args)
    }
}
