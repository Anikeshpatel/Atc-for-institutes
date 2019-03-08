package developer.anikesh.atc.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import developer.anikesh.atc.R
import developer.anikesh.atc.services.NotificationService
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isNotificationEnable = sharedPreferences.getBoolean("isPushNotificationEnable", false)
        notificationSwitch.isChecked = isNotificationEnable
        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val intent = Intent(this, NotificationService::class.java)
                ContextCompat.startForegroundService(this, intent)
                sharedPreferences.edit().apply {
                    putBoolean("isPushNotificationEnable", true)
                    apply()
                }
            }else {
                val intent = Intent(this, NotificationService::class.java)
                stopService(intent)
                sharedPreferences.edit().apply {
                    putBoolean("isPushNotificationEnable", false)
                    apply()
                }
            }
        }
    }
}
