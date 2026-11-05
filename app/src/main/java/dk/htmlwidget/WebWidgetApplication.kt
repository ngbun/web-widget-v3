package dk.htmlwidget

import android.app.Application
import android.content.IntentFilter
import android.os.Build
import android.content.Context
import android.content.Intent
import android.webkit.WebView

class WebWidgetApplication : Application() {
    private val wakeReceiver = WidgetWakeReceiver()

    override fun onCreate() {
        super.onCreate()
        WebView.enableSlowWholeDocumentDraw()
        WidgetDiagnostics.log(this, "App-proces startet")

        // Dynamic receiver works while the application process is alive.
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(wakeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(wakeReceiver, filter)
        }
        WidgetDiagnostics.log(this, "Oplåsningsmodtager registreret")
    }
}
