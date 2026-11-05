package dk.htmlwidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Tries to refresh stale website widgets when the device is unlocked. */
class WidgetWakeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return
        WidgetDiagnostics.log(context, "Oplåsning registreret")

        val manager = AppWidgetManager.getInstance(context)
        val ids = listOf(
            HtmlWidgetProvider::class.java,
            HtmlWidgetProvider1x1::class.java,
            HtmlWidgetProvider2x2::class.java
        ).flatMap { cls ->
            manager.getAppWidgetIds(ComponentName(context, cls)).toList()
        }.distinct()
        if (ids.isEmpty()) {
            WidgetDiagnostics.log(context, "Ingen aktive widgets fundet")
            return
        }

        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val staleIds = ids.filter { id ->
            val url = prefs.getString("url_$id", null)
            if (url.isNullOrBlank()) return@filter false
            val minutes = prefs.getInt("refreshMinutes_$id", 15)
            if (minutes <= 0) return@filter false
            val last = prefs.getLong("lastSuccessfulUpdate_$id", 0L)
            val lastWake = prefs.getLong("lastWakeRefresh_$id", 0L)
            val intervalMs = minutes.toLong() * 60_000L
            val overdue = last <= 0L || now - last >= intervalMs
            val notRecentlyQueued = lastWake <= 0L || now - lastWake >= 60_000L
            overdue && notRecentlyQueued
        }
        WidgetDiagnostics.log(context, "Widgets: ${ids.size}; forældede: ${staleIds.size}; ID: ${staleIds.joinToString()}")
        if (staleIds.isEmpty()) return

        // Throttle duplicate broadcasts (manifest + dynamic receiver).
        prefs.edit().apply {
            staleIds.forEach { putLong("lastWakeRefresh_$it", now) }
        }.apply()
        WidgetDiagnostics.log(context, "Starter refresh efter oplåsning")
        HtmlWidgetProvider.refresh(context, staleIds.toIntArray(), false)
    }
}
