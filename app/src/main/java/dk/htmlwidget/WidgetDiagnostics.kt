package dk.htmlwidget

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WidgetDiagnostics {
    fun log(context: Context, message: String) {
        try {
            val prefs = context.getSharedPreferences("widget_diagnostics", Context.MODE_PRIVATE)
            val timestamp = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date())
            val old = prefs.getString("events", "").orEmpty()
            val updated = ("$timestamp  $message\n" + old).take(10000)
            prefs.edit().putString("events", updated).apply()
        } catch (_: Exception) { }
    }
    fun read(context: Context): String = context.getSharedPreferences(
        "widget_diagnostics", Context.MODE_PRIVATE
    ).getString("events", "Ingen hændelser registreret endnu.").orEmpty()
    fun clear(context: Context) {
        context.getSharedPreferences("widget_diagnostics", Context.MODE_PRIVATE)
            .edit().remove("events").apply()
    }
}
