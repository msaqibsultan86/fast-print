package com.notzyvex.fastprint.data

import android.content.Context
import com.notzyvex.fastprint.state.AuthMode
import com.notzyvex.fastprint.ui.theme.AccentTheme

/** Small, synchronous preferences — accent, notifications, default printer, signed-in identity. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("fastprint", Context.MODE_PRIVATE)

    var accent: AccentTheme
        get() = runCatching { AccentTheme.valueOf(sp.getString(KEY_ACCENT, null) ?: "") }
            .getOrDefault(AccentTheme.Terracotta)
        set(v) = sp.edit().putString(KEY_ACCENT, v.name).apply()

    var notifications: Boolean
        get() = sp.getBoolean(KEY_NOTIF, true)
        set(v) = sp.edit().putBoolean(KEY_NOTIF, v).apply()

    var defaultPrinter: String?
        get() = sp.getString(KEY_PRINTER, null)
        set(v) = sp.edit().putString(KEY_PRINTER, v).apply()

    var authMode: AuthMode
        get() = runCatching { AuthMode.valueOf(sp.getString(KEY_AUTH, null) ?: "") }
            .getOrDefault(AuthMode.GUEST)
        set(v) = sp.edit().putString(KEY_AUTH, v.name).apply()

    var displayName: String?
        get() = sp.getString(KEY_NAME, null)
        set(v) = sp.edit().putString(KEY_NAME, v).apply()

    var email: String?
        get() = sp.getString(KEY_EMAIL, null)
        set(v) = sp.edit().putString(KEY_EMAIL, v).apply()

    var photoUrl: String?
        get() = sp.getString(KEY_PHOTO, null)
        set(v) = sp.edit().putString(KEY_PHOTO, v).apply()

    fun clearIdentity() {
        sp.edit()
            .remove(KEY_NAME).remove(KEY_EMAIL).remove(KEY_PHOTO)
            .putString(KEY_AUTH, AuthMode.GUEST.name)
            .apply()
    }

    private companion object {
        const val KEY_ACCENT = "accent"
        const val KEY_NOTIF = "notifications"
        const val KEY_PRINTER = "default_printer"
        const val KEY_AUTH = "auth_mode"
        const val KEY_NAME = "display_name"
        const val KEY_EMAIL = "email"
        const val KEY_PHOTO = "photo_url"
    }
}
