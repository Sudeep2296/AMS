
package com.example.attendance

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.attendance.databinding.ActivityMainBinding
import com.example.attendance.ui.attendance.TeacherAttendanceActivity
import com.example.attendance.ui.attendance.StudentAttendanceActivity
import com.example.attendance.ui.admin.AdminDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTeacher.setOnClickListener {
            startActivity(Intent(this, TeacherAttendanceActivity::class.java))
        }
        binding.btnStudent.setOnClickListener {
            startActivity(Intent(this, StudentAttendanceActivity::class.java))
        }
        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }
    }
}
