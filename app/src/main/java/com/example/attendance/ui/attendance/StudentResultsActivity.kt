package com.example.attendance.ui.attendance

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.model.Semester
import com.example.attendance.model.Subject
import com.example.attendance.ui.login.LoginActivity
import com.example.attendance.utils.FirebaseUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SemesterAdapter
    private lateinit var semesterFilter: Spinner
    private val db = FirebaseFirestore.getInstance()
    private var allSemesters = mutableListOf<Semester>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_results)

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

        recyclerView = findViewById(R.id.recyclerResults)
        recyclerView.layoutManager = LinearLayoutManager(this)

        semesterFilter = findViewById(R.id.semesterFilter)

        adapter = SemesterAdapter()
        recyclerView.adapter = adapter

        loadStudentResults()

        // Setup semester filter
        setupSemesterFilter()
    }

    private fun loadStudentResults() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { userDoc ->
                    val usn = userDoc.getString("usn")
                    if (usn != null) {
                        fetchResultsFromFirestore(usn)
                    } else {
                        Toast.makeText(this, "USN not found!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchResultsFromFirestore(usn: String) {
        db.collection("results").document(usn).collection("semesters")
            .get()
            .addOnSuccessListener { semesterDocs ->
                val semesterList = mutableListOf<Semester>()
                for (semesterDoc in semesterDocs) {
                    val semesterId = semesterDoc.id
                    semesterDoc.reference.collection("subjects")
                        .get()
                        .addOnSuccessListener { subjectDocs ->
                            val subjects = subjectDocs.map { subj ->
                                Subject(
                                    subj.id,
                                    subj.getString("subjectName") ?: "",
                                    subj.getLong("marks")?.toInt() ?: 0,
                                    subj.getString("grade") ?: ""
                                )
                            }
                            semesterList.add(Semester(semesterId, subjects))
                            allSemesters = semesterList
                            adapter.setData(semesterList)
                            updateSemesterFilter()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading results", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSemesterFilter() {
        semesterFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterResults()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun updateSemesterFilter() {
        val semesters = mutableListOf("All Semesters")
        semesters.addAll(allSemesters.map { "Semester ${it.id}" })
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        semesterFilter.adapter = filterAdapter
    }

    private fun filterResults() {
        val selectedFilter = semesterFilter.selectedItem.toString()
        val filteredSemesters = if (selectedFilter == "All Semesters") {
            allSemesters
        } else {
            val semesterNumber = selectedFilter.replace("Semester ", "")
            allSemesters.filter { it.id == semesterNumber }
        }
        adapter.setData(filteredSemesters)
    }

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.signOut()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentResultsActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(this@StudentResultsActivity, LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentResultsActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
