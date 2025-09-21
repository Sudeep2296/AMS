package com.example.attendance.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.attendance.R
import com.example.attendance.ui.login.LoginActivity
import com.example.attendance.utils.FirebaseUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AdminDashboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    // Modern file picker using ActivityResultContracts
    private val csvFilePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { parseAndUploadCSV(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard_new)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup drawer
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        // Add hamburger icon
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation menu clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.nav_home -> {
                    // Already on home, just close drawer
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            true
        }

        // Add Teacher functionality
        findViewById<Button>(R.id.btnAddTeacher).setOnClickListener {
            val teacherEmail = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTeacherEmail).text.toString().trim()
            val teacherName = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTeacherName).text.toString().trim()
            val tempPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTempPassword).text.toString()

            if (teacherEmail.isEmpty() || teacherName.isEmpty() || tempPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addTeacher(teacherEmail, teacherName, tempPassword)
        }

        // CSV Upload functionality - using modern file picker
        val uploadButton = findViewById<Button>(R.id.btnUploadCSV)
        uploadButton.setOnClickListener {
            csvFilePicker.launch("text/*")
        }
    }

    private fun parseAndUploadCSV(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?

            // Skip header
            reader.readLine()

            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(",")
                if (tokens.size >= 7) {
                    val usn = tokens[0].trim()
                    val semester = tokens[2].trim()
                    val subjectCode = tokens[3].trim()
                    val subjectName = tokens[4].trim()
                    val marks = tokens[5].trim().toInt()
                    val grade = tokens[6].trim()

                    val subjectData = hashMapOf(
                        "subjectName" to subjectName,
                        "marks" to marks,
                        "grade" to grade
                    )

                    db.collection("results")
                        .document(usn)
                        .collection("semesters")
                        .document(semester)
                        .collection("subjects")
                        .document(subjectCode)
                        .set(subjectData)
                }
            }
            Toast.makeText(this, "Results uploaded successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error uploading: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun addTeacher(email: String, name: String, tempPassword: String) {
        findViewById<Button>(R.id.btnAddTeacher).isEnabled = false
        findViewById<Button>(R.id.btnAddTeacher).text = "Adding Teacher..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.addTeacher(email, name, tempPassword)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Teacher added successfully!", Toast.LENGTH_SHORT).show()
                    clearForm()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Failed to add teacher: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    findViewById<Button>(R.id.btnAddTeacher).isEnabled = true
                    findViewById<Button>(R.id.btnAddTeacher).text = "Add Teacher"
                }
            }
        }
    }

    private fun clearForm() {
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTeacherEmail).text?.clear()
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTeacherName).text?.clear()
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTempPassword).text?.clear()
    }

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.signOut()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AdminDashboardActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
