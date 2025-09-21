package com.example.attendance.model

data class Semester(
    val id: String = "",
    val subjects: List<Subject> = emptyList()
)
