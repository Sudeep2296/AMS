package com.example.attendance.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendance.databinding.ActivityLoginBinding
import com.example.attendance.model.Student
import com.example.attendance.model.User
import com.example.attendance.ui.admin.AdminDashboardActivity
import com.example.attendance.ui.attendance.StudentAttendanceActivity
import com.example.attendance.ui.attendance.TeacherDashboardActivity
import com.example.attendance.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            CoroutineScope(Dispatchers.Main).launch {
                redirectBasedOnRole()
            }
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.signIn(email, password)
                withContext(Dispatchers.Main) {
                    redirectBasedOnRole()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                }
            }
        }
    }

    private suspend fun redirectBasedOnRole() {
        try {
            val user = FirebaseUtils.getCurrentUser()
            if (user != null) {
                Log.d(TAG, "Getting user data for: ${user.uid}")
                var userData = FirebaseUtils.getUserFromFirestore(user.uid)

                // Auto-create user if missing
                if (userData == null) {
                    val role = when {
                        user.email?.contains("admin") == true -> "admin"
                        user.email?.contains("teacher") == true -> "teacher"
                        else -> "student"
                    }

                    val name = user.email?.substringBefore("@") ?: "User"
                    val newUserData = User(uid = user.uid, email = user.email ?: "", name = name, role = role)

                    try {
                        FirebaseUtils.saveUserToFirestore(newUserData)

                        if (role == "student") {
                            val student = Student(
                                id = user.uid,
                                name = name,
                                usn = user.email?.substringBefore("@")?.uppercase() + "001"
                            )
                            FirebaseUtils.addStudentToFirestore(student)
                        }

                        userData = newUserData
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to create user data: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Failed to create user profile", Toast.LENGTH_SHORT).show()
                            FirebaseUtils.signOut()
                        }
                        return
                    }
                }

                withContext(Dispatchers.Main) {
                    if (userData != null) {
                        val intent = when (userData.role) {
                            "admin" -> Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                            "teacher" -> Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                            "student" -> Intent(this@LoginActivity, StudentAttendanceActivity::class.java)
                            else -> {
                                Toast.makeText(this@LoginActivity, "Invalid user role", Toast.LENGTH_SHORT).show()
                                FirebaseUtils.signOut()
                                return@withContext
                            }
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                        FirebaseUtils.signOut()
                    }
                }
            } else {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in redirectBasedOnRole: ${e.message}", e)
            Toast.makeText(this, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            FirebaseUtils.signOut()
        }
    }
}
