package developer.anikesh.atc

import android.app.Application
import android.widget.Toast
import com.google.firebase.FirebaseApp
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import developer.anikesh.atc.BuildConfig.APPLICATION_ID
import developer.anikesh.atc.services.NotificationService
import developer.anikesh.atc.util.FileUtil


class App: Application() {
    override fun onCreate() {
        super.onCreate()

        FileUtil.init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "AtcNotification",
                "Push Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(notificationChannel)
            println("Notification channel is created")
        }

        if (isMainProcess(this)) {
            FirebaseApp.initializeApp(this)
            return
        }

    }

    private fun isMainProcess(context: Context?): Boolean {
        if (null == context) {
            return true
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val pid = android.os.Process.myPid()
        for (processInfo in manager.runningAppProcesses) {
            val name = processInfo.processName
            if (name != "" && pid == processInfo.pid && name == APPLICATION_ID) {
                return true
            }
        }
        return false
    }
}