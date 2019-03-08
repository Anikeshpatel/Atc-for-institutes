package developer.anikesh.atc.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import developer.anikesh.atc.R
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this

        fetchTodayTotalStrength()

        listRecordBtn.setOnClickListener {
            startActivity(Intent(this, StrengthListActivity::class.java))
        }

        todayStrengthBtn.setOnClickListener {
            startActivity(Intent(this, TodayStrength::class.java))
        }

        facultyListBtn.setOnClickListener {
            startActivity(Intent(this, FacultyListActivity::class.java))
        }

        settingBtn.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        logOutBtn.setOnClickListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences.edit().apply {
                remove("isAdmin")
                if (commit()) {
                    startActivity(Intent(mContext, LoginActivity::class.java))
                    finish()
                }
            }
        }

    }

    private fun fetchTodayTotalStrength() {
        val branches = listOf<String>(
            "Computer Science",
            "Mechanical Engineering",
            "Civil Engineering",
            "Electronics Engineering",
            "Electrical Engineering"
        )
        val sems = listOf(
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8
        )
        var totalStrength = 0
        val collection = FirebaseFirestore.getInstance().collection("count")
        collection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (doc in task.result!!) {
                    val data = doc.data
                    var isFound = false
                    for (branch in branches) {
                        for (sem in sems) {
                            if (data["Branch"] as String == branch &&
                                toInt(data["Sem"]!!) == sem &&
                                toInt(data["Day"]!!) == Date().date &&
                                toInt(data["Month"]!!) == Date().month + 1 &&
                                toInt(data["Year"]!!) == Date().year) {
                                totalStrength += toInt(data["Strength"]!!)
                                isFound = true
                                break
                            }

                            if (isFound) {

                            }
                        }
                    }
                }
                totalView.text = totalStrength.toString()
            }else {
                Toast.makeText(this, "Enable to find", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toInt(data: Any): Int {
        return Integer.parseInt(data.toString())
    }

}
