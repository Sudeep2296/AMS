package com.example.attendance.utils

import android.util.Log
import com.example.attendance.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseUtils {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private const val TAG = "FirebaseUtils"

    // Authentication methods
    fun getCurrentUser() = auth.currentUser

    suspend fun signIn(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun signOut() = auth.signOut()

    suspend fun createUser(email: String, password: String, name: String, role: String) {
        try {
            Log.d(TAG, "Creating user account for: $email with role: $role")

            // Validate input parameters
            if (email.isBlank() || password.isBlank() || name.isBlank() || role.isBlank()) {
                throw IllegalArgumentException("Email, password, name, and role cannot be empty")
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // Check if user creation was successful
            val firebaseUser = result.user
            if (firebaseUser == null) {
                throw Exception("Failed to create user account")
            }

            Log.d(TAG, "Auth user created successfully with UID: ${firebaseUser.uid}")

            // Save to Firestore
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                role = role
            )
            saveUserToFirestore(user)

            Log.d(TAG, "User account created successfully: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user account: ${e.message}", e)
            throw e
        }
    }

    // Firestore methods
    suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
    }

    suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            if (uid.isBlank()) {
                Log.w(TAG, "UID is blank or empty")
                return null
            }

            Log.d(TAG, "Getting user data for UID: $uid")
            val document = firestore.collection("users").document(uid).get().await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                Log.d(TAG, "User data retrieved successfully for: $uid")
                return user
            } else {
                Log.w(TAG, "User document does not exist for UID: $uid")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user from Firestore for UID: $uid", e)
            null
        }
    }

    suspend fun addTeacher(email: String, name: String, tempPassword: String) {
        try {
            Log.d(TAG, "Creating teacher account for: $email")

            // Validate input parameters
            if (email.isBlank() || name.isBlank() || tempPassword.isBlank()) {
                throw IllegalArgumentException("Email, name, and password cannot be empty")
            }

            // Create auth user
            val result = auth.createUserWithEmailAndPassword(email, tempPassword).await()

            // Check if user creation was successful
            val firebaseUser = result.user
            if (firebaseUser == null) {
                throw Exception("Failed to create user account")
            }

            Log.d(TAG, "Auth user created successfully with UID: ${firebaseUser.uid}")

            // Save to Firestore with teacher role
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                role = "teacher"
            )
            saveUserToFirestore(user)

            Log.d(TAG, "Teacher account created successfully: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating teacher account: ${e.message}", e)
            throw e
        }
    }

    // Admin credentials (you can change these)
    val adminCredentials = Pair("admin@attendance.com", "admin123")

    // Student management methods
    suspend fun getAllStudents(): List<com.example.attendance.model.Student> {
        return try {
            Log.d(TAG, "Fetching all students from Firestore")
            val querySnapshot = firestore.collection("students").get().await()
            val students = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(com.example.attendance.model.Student::class.java)
            }
            Log.d(TAG, "Retrieved ${students.size} students")
            students
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching students: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addStudentToFirestore(student: com.example.attendance.model.Student) {
        try {
            Log.d(TAG, "Adding student to Firestore: ${student.name}")
            firestore.collection("students").document(student.id).set(student).await()
            Log.d(TAG, "Student added successfully: ${student.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding student: ${e.message}", e)
            throw e
        }
    }

    suspend fun createStudentUser(email: String, password: String, name: String, usn: String) {
        try {
            Log.d(TAG, "Creating student account for: $email")

            // Validate input parameters
            if (email.isBlank() || password.isBlank() || name.isBlank() || usn.isBlank()) {
                throw IllegalArgumentException("Email, password, name, and USN cannot be empty")
            }

            // Create auth user
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: throw Exception("Failed to create user account")

            Log.d(TAG, "Auth user created successfully with UID: ${firebaseUser.uid}")

            // Create User document in Firestore
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                role = "student"
            )
            saveUserToFirestore(user)

            // Create Student document in Firestore
            val student = com.example.attendance.model.Student(
                id = firebaseUser.uid,
                name = name,
                usn = usn
            )
            addStudentToFirestore(student)

            Log.d(TAG, "Student account created successfully: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating student account: ${e.message}", e)
            throw e
        }
    }
}
