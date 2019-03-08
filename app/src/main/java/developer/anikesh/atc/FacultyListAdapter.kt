package developer.anikesh.atc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import developer.anikesh.atc.model.Faculty
import kotlinx.android.synthetic.main.faculty_list_item.view.*

class FacultyListAdapter(var dataSet: List<Faculty>): RecyclerView.Adapter<FacultyListAdapter.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.faculty_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataSet[position]
        Glide.with(mContext)
            .load(data.ProfilePic)
            .into(holder.avatarView)
        holder.nameView.text = data.Name
        holder.branchView.text = getBranchFromCode(data.BranchCode)
        if (data.IsBlocked) {
            holder.blockBtn.text = "unBlock"
        }else {
            holder.blockBtn.text = "Block"
        }
        holder.blockBtn.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("Users")
                .child(data.id)
                .child("IsBlocked")
            if (data.IsBlocked) {
                database.setValue(false)
                    .addOnSuccessListener {
                        Toast.makeText(mContext, "Blocked", Toast.LENGTH_LONG).show()
                    }
            }else {
                database.setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(mContext, "Blocked", Toast.LENGTH_LONG).show()
                    }
            }
        }
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

    class ViewHolder(var view: View): RecyclerView.ViewHolder(view) {
        var avatarView = view.facultyAvatarView!!
        var nameView = view.facultyNameView!!
        var branchView = view.facultyBranchView!!
        var blockBtn = view.blockUnblockBtn!!
    }
}