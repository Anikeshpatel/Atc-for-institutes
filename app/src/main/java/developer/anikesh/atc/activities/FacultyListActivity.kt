package developer.anikesh.atc.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import developer.anikesh.atc.FacultyListAdapter
import developer.anikesh.atc.R
import developer.anikesh.atc.model.Faculty
import kotlinx.android.synthetic.main.activity_faculty_list.*

class FacultyListActivity : AppCompatActivity() {

    private lateinit var adapter: FacultyListAdapter
    private val faculties = ArrayList<Faculty>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_list)
        adapter = FacultyListAdapter(faculties)

        facultyList.adapter = adapter
        facultyList.layoutManager = LinearLayoutManager(this)
        facultyList.setHasFixedSize(true)

        val database = FirebaseDatabase.getInstance().getReference("Users")

        database.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                faculties.clear()
                for (user in dataSnapshot.value as Map<*, *>) {
                    val data = user.value as HashMap<*, *>
                    val faculty = Faculty(
                        id = user.key as String,
                        Name = data["Name"].toString(),
                        BranchCode = data["BranchCode"].toString(),
                        ProfilePic = data["ProfilePic"].toString(),
                        IsBlocked = data["IsBlocked"] as Boolean
                    )
                    faculties.add(faculty)
                }
                adapter.notifyDataSetChanged()
            }
        })
    }
}
