package developer.anikesh.atc.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import developer.anikesh.atc.R
import kotlinx.android.synthetic.main.activity_attendance.*
import java.util.*


class AttendanceActivity : AppCompatActivity() {

    private lateinit var semViews: List<FrameLayout>
    private var isReady = false
    private var selectedSem = 1
    private lateinit var facultyName: String
    private lateinit var branch: String
    private var lastUpdatedDate: Int = 0
    private lateinit var mContext: Context
    private lateinit var dialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        dialog = ProgressDialog(this)
        mContext = this
        initBranch()
        isBlocked()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        lastUpdatedDate = sharedPreferences.getInt("LastDate", 0)

        semViews = listOf(
            sem1View,
            sem2View,
            sem3View,
            sem4View,
            sem5View,
            sem6View,
            sem7View,
            sem8View
        )

        logOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPreferences.edit().apply {
                remove("LastDate")
                if (commit()) {
                    startActivity(Intent(mContext, LoginActivity::class.java))
                    finish()
                }
            }
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                studentCounterView.setText(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        attendanceSubmitBtn.setOnClickListener {
            if (isReady) {
                dialog.show()
                val notificationDatabase = FirebaseDatabase.getInstance().getReference("Notification")
                val cacheDatabase = FirebaseDatabase.getInstance().getReference("Cache")
                if (lastUpdatedDate != Date().date) {
                    val collection = FirebaseFirestore.getInstance().collection("count")
                    val document = collection.document(System.currentTimeMillis().toString())
                    val data = mapOf(
                        Pair("Branch", branch),
                        Pair("Date", System.currentTimeMillis()),
                        Pair("Sem", selectedSem),
                        Pair("Strength", Integer.parseInt(studentCounterView.text.toString())),
                        Pair("FacultyId", FirebaseAuth.getInstance().uid!!),
                        Pair("FacultyName", facultyName),
                        Pair("Day", Date().date),
                        Pair("Month", Date().month + 1),
                        Pair("Year", Date().year)
                    )
                    val cacheKey = cacheDatabase.child("${Date().date}${Date().month}${Date().year}$branch$selectedSem")
                    cacheKey.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(mContext, "Some other faculty already pushed for $selectedSem semester", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                            }else {
                                cacheKey.setValue(mapOf(Pair("Pushed", true))).addOnSuccessListener {
                                    notificationDatabase.push()
                                        .setValue(mapOf(
                                            Pair("title", branch),
                                            Pair("desc", "Total ${studentCounterView.text.toString()} in $selectedSem sem")
                                        )).addOnSuccessListener {
                                            Toast.makeText(mContext, "Notification has sent to admin", Toast.LENGTH_LONG).show()
                                        }
                                    dialog.dismiss()
                                    document.set(data).addOnSuccessListener {
                                        sharedPreferences.edit().apply {
                                            putInt("LastDate", Date().date)
                                            apply()
                                        }
                                        lastUpdatedDate = Date().date
                                        Toast.makeText(mContext, "Done", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    })
                }else {
                    dialog.dismiss()
                    Toast.makeText(this, "Hey, You Already Pushed Strength for Today", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun isBlocked() {
        val database = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().uid!!)
        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if ((snapshot.value as HashMap<*, *>)["IsBlocked"] as Boolean) {
                        Toast.makeText(mContext, "You are Blocked by Admin", Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(mContext, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        })
    }

    fun changeSem(view: View) {
        semViews.forEachIndexed { index, frameLayout ->
            if (view.id == frameLayout.id) {
                frameLayout.setBackgroundResource(R.drawable.bg_round_btn)
                (frameLayout[0] as TextView).setTextColor(Color.WHITE)
                selectedSem = index + 1
            }else {
                frameLayout.background = null
                (frameLayout[0] as TextView).setTextColor(Color.parseColor("#4f4f4f"))
            }
        }
    }

    private fun initBranch() {
        val database = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().uid!!)
        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val data = (snapshot.value as Map<*, *>)
                branchView.text = getBranchFromCode(data["BranchCode"].toString())
                facultyName = data["Name"].toString()
                branch = getBranchFromCode(data["BranchCode"].toString())
                isReady = true
            }
        })
    }

    private fun getBranchFromCode(branchCode: String): String {
        return when(branchCode) {
            "CS0863" -> "Computer Science"
            "ME0863" -> "Mechanical Engineering"
            "CE0863" -> "Civil Engineering"
            "EE0863" -> "Electronics Engineering"
            "EC0863" -> "Electrical Engineering"
            else -> "XXXXXX"
        }
    }
}
