package com.example.attendance.ui.attendance
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.model.Result
class ResultAdapter(private val items: List<Result>): RecyclerView.Adapter<ResultAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.t1.text = r.subject + " (" + r.code + ") - " + r.marks.toString()
        holder.t2.text = "Grade: " + r.grade
    }
    override fun getItemCount(): Int = items.size
    class VH(v: View): RecyclerView.ViewHolder(v) {
        val t1: TextView = v.findViewById(android.R.id.text1)
        val t2: TextView = v.findViewById(android.R.id.text2)
    }
}
