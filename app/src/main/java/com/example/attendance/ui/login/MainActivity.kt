package com.example.attendance.ui.login
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.attendance.R
import com.example.attendance.ui.attendance.TeacherAttendanceActivity
import com.example.attendance.ui.attendance.StudentAttendanceActivity
import com.example.attendance.ui.admin.AdminDashboardActivity
import android.widget.Button
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnTeacher).setOnClickListener {
            startActivity(Intent(this, TeacherAttendanceActivity::class.java))
        }
        findViewById<Button>(R.id.btnStudent).setOnClickListener {
            startActivity(Intent(this, StudentAttendanceActivity::class.java))
        }
        findViewById<Button>(R.id.btnAdmin).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }
    }
}
