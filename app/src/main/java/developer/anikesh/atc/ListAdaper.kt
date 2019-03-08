package developer.anikesh.atc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import developer.anikesh.atc.model.Count
import kotlinx.android.synthetic.main.table_row_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class ListAdaper(private val listStrength: List<Count>): RecyclerView.Adapter<ListAdaper.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.table_row_item, parent, false))
    }

    override fun getItemCount(): Int {
        return listStrength.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println(listStrength)
        val data = listStrength[position]

        holder.branchView.text = data.Branch
        holder.dateView.text = getDateString(data.Date)
        holder.facultyName.text = data.FacultyName
        holder.semView.text = data.Sem.toString()
        holder.strengthView.text = data.Strength.toString()
    }

    private fun getDateString(timeStamp: Long): String {
        val formate = SimpleDateFormat("dd/MM/yyyy")
        val date = Date(timeStamp)
        return formate.format(date)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val branchView = view.branchView!!
        val dateView = view.dateView!!
        val semView = view.semView!!
        val strengthView = view.strengthView!!
        val facultyName = view.facultyNameView!!
    }
}