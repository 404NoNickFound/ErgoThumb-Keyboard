package io.github.nonickfound.ergothumb

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class ThumbGridApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(Prefs.appLang(this))
        )
    }
}