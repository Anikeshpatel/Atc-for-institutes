package developer.anikesh.atc.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import developer.anikesh.atc.R
import developer.anikesh.atc.activities.SettingActivity
import developer.anikesh.atc.activities.StrengthListActivity

class NotificationService: Service() {

    val notifDatabase = FirebaseDatabase.getInstance().getReference("Notification")

    override fun onCreate() {
        super.onCreate()
        println("onCreate Service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("OnStart Command")

        val intent = Intent(this, SettingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, intent, 0)

        val notification = NotificationCompat.Builder(this, "AtcNotification")
            .setContentTitle("Atc Notification")
            .setContentText("Atc is running in background for notification")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(101, notification)

        notifDatabase.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                showNotification(dataSnapshot.value as HashMap<*, *>)
                notifDatabase.removeValue()
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })

        return START_NOT_STICKY
    }

    private fun showNotification(data: HashMap<*, *>) {
        val intent = Intent(this, StrengthListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0, intent, 0)

        val notification = NotificationCompat.Builder(this, "AtcNotification")
            .setContentTitle(data["title"].toString())
            .setContentText(data["desc"].toString())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        with(NotificationManagerCompat.from(this)) {
            notify(100, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy Service")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}