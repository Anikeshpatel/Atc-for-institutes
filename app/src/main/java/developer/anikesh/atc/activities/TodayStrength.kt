package developer.anikesh.atc.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.google.firebase.firestore.FirebaseFirestore
import developer.anikesh.atc.R
import kotlinx.android.synthetic.main.activity_today_strength.*
import java.util.*

class TodayStrength : AppCompatActivity() {
    private lateinit var semViews: List<FrameLayout>
    private lateinit var brViews: List<FrameLayout>
    private var selectedSem = 1
    private var selectedBr = "Computer Science"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_strength)

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
        brViews = listOf(
            brCSView,
            brMEView,
            brCEView,
            brEEView,
            brECView
        )

        checkBtn.setOnClickListener {
            val collection = FirebaseFirestore.getInstance().collection("count")
            collection.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        val data = doc.data
                        var isFound = false
                        if (data["Branch"] as String == selectedBr &&
                            toInt(data["Sem"]!!) == selectedSem &&
                            toInt(data["Day"]!!) == Date().date &&
                                toInt(data["Month"]!!) == Date().month + 1 &&
                                toInt(data["Year"]!!) == Date().year) {

                            Toast.makeText(this, "Total Strength : ${data["Strength"]}", Toast.LENGTH_LONG).show()
                            isFound = true
                            break
                        }

                        if (!isFound) {
                            Toast.makeText(this, "No one pushed strength for \n $selectedBr - $selectedSem", Toast.LENGTH_LONG).show()
                        }
                    }
                }else {
                    Toast.makeText(this, "Enable to find", Toast.LENGTH_LONG).show()
                }
            }
        }
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

    fun changeBr(view: View) {
        brViews.forEachIndexed { index, frameLayout ->
            if (view.id == frameLayout.id) {
                frameLayout.setBackgroundResource(R.drawable.bg_round_btn)
                (frameLayout[0] as TextView).setTextColor(Color.WHITE)
                selectedBr = getBranch(index)
            }else {
                frameLayout.background = null
                (frameLayout[0] as TextView).setTextColor(Color.parseColor("#4f4f4f"))
            }
        }
    }

    private fun toInt(data: Any): Int {
        return Integer.parseInt(data.toString())
    }

    private fun getBranch(br: Int): String {
        return when(br) {
            0 -> "Computer Science"
            1 -> "Mechanical Engineering"
            2 -> "Civil Engineering"
            3 -> "Electronics Engineering"
            4 -> "Electrical Engineering"
            else -> "Computer Science"
        }
    }
}
