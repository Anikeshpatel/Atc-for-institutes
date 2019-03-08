package developer.anikesh.atc.activities

import android.app.ProgressDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import developer.anikesh.atc.ListAdaper
import developer.anikesh.atc.R
import developer.anikesh.atc.model.Count
import developer.anikesh.atc.requestMediaPermission
import developer.anikesh.atc.util.FileUtil
import kotlinx.android.synthetic.main.activity_strength_list.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StrengthListActivity : AppCompatActivity() {

    private lateinit var adapter: ListAdaper
    private lateinit var dataSet: ArrayList<Count>
    private lateinit var collection: CollectionReference
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_strength_list)
        dataSet = ArrayList()
        adapter = ListAdaper(dataSet)

        dialog = ProgressDialog(this)

        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(this)
        listView.setHasFixedSize(true)

        saveToExcelBtn.setOnClickListener {
            if (requestMediaPermission()){
                saveDocument()
            }
        }

        collection = FirebaseFirestore.getInstance().collection("count")
        collection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dataSet.clear()
                for (data in task.result!!) {
                    dataSet.add(
                        Count(
                            Branch = data["Branch"].toString(),
                            Strength = Integer.parseInt(data["Strength"].toString()),
                            Sem = Integer.parseInt(data["Sem"].toString()),
                            Date = data["Date"] as Long,
                            FacultyName = data["FacultyName"].toString(),
                            FacultyId = data["FacultyId"].toString()
                        )
                    )
                    /*val branch = data.data["Branch"] as String
                    val sem = Integer.parseInt(data.data["Sem"].toString())
                    val totalStudent = Integer.parseInt(data.data["Strength"].toString())
                    var isFound = false
                    lateinit var model: ListModel
                    for (mod in dataSet) {
                        isFound = mod.branch == branch
                        if (isFound) {
                            model = mod
                        }
                    }
                    println(sem)
                    println(isFound)
                    if (isFound) {
                        when(sem) {
                            1 -> model.sem1Count = totalStudent
                            2 -> model.sem2Count = totalStudent
                            3 -> model.sem3Count = totalStudent
                            4 -> model.sem4Count = totalStudent
                            5 -> model.sem5Count = totalStudent
                            6 -> model.sem6Count = totalStudent
                            7 -> model.sem7Count = totalStudent
                            8 -> model.sem8Count = totalStudent
                        }
                        println("Its existing model : ")
                        println(model)
                    }else {
                        model = ListModel(branch)
                        when(sem) {
                            1 -> model.sem1Count = totalStudent
                            2 -> model.sem2Count = totalStudent
                            3 -> model.sem3Count = totalStudent
                            4 -> model.sem4Count = totalStudent
                            5 -> model.sem5Count = totalStudent
                            6 -> model.sem6Count = totalStudent
                            7 -> model.sem7Count = totalStudent
                            8 -> model.sem8Count = totalStudent
                        }
                        println("its new model : ")
                        println(model)
                        dataSet.add(model)
                    }*/
                }
                dataSet.reverse()
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Enable to fetch data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveDocument() {
        if (!dataSet.isEmpty()) {
            dialog.show()
            val rows = ArrayList<String>()
            rows.add("Date, Branch, Sem, Strength, Faculty Name")
            for (row in dataSet) {
                rows.add("${getDateString(row.Date)}, ${row.Branch}, ${row.Sem}, ${row.Strength}, ${row.FacultyName}")
            }
            FileUtil.saveDocument(rows)
            dialog.dismiss()
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
        }else {
            Toast.makeText(this, "No data for now", Toast.LENGTH_LONG).show()
        }
    }

    private fun getDateString(timeStamp: Long): String {
        val formate = SimpleDateFormat("dd-MM-yyyy")
        val date = Date(timeStamp)
        return formate.format(date)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                saveDocument()
            } else {
                Toast.makeText(this, "Please give permission first", Toast.LENGTH_LONG).show()
            }
        }
    }
}
