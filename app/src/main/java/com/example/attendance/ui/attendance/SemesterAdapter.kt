package com.example.attendance.ui.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.model.Semester

class SemesterAdapter : RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder>() {

    private var semesters: List<Semester> = emptyList()

    fun setData(data: List<Semester>) {
        semesters = data
        notifyDataSetChanged()
    }

    class SemesterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val semesterTitle: TextView = view.findViewById(R.id.txtSemester)
        val subjectList: TextView = view.findViewById(R.id.txtSubjects)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_semester, parent, false)
        return SemesterViewHolder(view)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        val semester = semesters[position]
        holder.semesterTitle.text = "Semester ${semester.id}"

        val subjectsText = semester.subjects.joinToString("\n") { subj ->
            "${subj.code} - ${subj.name}: ${subj.marks} (${subj.grade})"
        }

        holder.subjectList.text = subjectsText
    }

    override fun getItemCount() = semesters.size
}
