package com.example.attendance.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "", // "student", "teacher", "admin"
    val createdAt: Long = System.currentTimeMillis()
)
