package com.example.attendance.ui.attendance
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.model.Student
class StudentCardAdapter(private val items: MutableList<Student>): RecyclerView.Adapter<StudentCardAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_student_card, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.name.text = s.name
        holder.usn.text = s.usn
    }
    override fun getItemCount(): Int = items.size
    fun currentTop(): Student? = if (items.isNotEmpty()) items[0] else null
    fun removeTop() { if (items.isNotEmpty()) { items.removeAt(0); notifyDataSetChanged() } }
    class VH(v: View): RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val usn: TextView = v.findViewById(R.id.tvUsn)
    }
}
