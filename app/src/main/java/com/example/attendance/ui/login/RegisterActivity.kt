package com.example.attendance.ui.login

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendance.databinding.ActivityRegisterBinding
import com.example.attendance.utils.FirebaseUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performRegistration(name, email, password)
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration(name: String, email: String, password: String) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create user with student role
                FirebaseUtils.createUser(email, password, name, "student")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
            }
        }
    }
}
