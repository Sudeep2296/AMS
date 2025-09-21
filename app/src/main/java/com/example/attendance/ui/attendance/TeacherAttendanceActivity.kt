package com.example.attendance.ui.attendance
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.attendance.R
import com.example.attendance.ui.login.LoginActivity
import com.example.attendance.utils.FirebaseUtils
import com.google.android.material.navigation.NavigationView
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeacherAttendanceActivity : AppCompatActivity(), CardStackListener {
    private lateinit var cardStackView: CardStackView
    private lateinit var layoutManager: CardStackLayoutManager
    private lateinit var adapter: StudentCardAdapter
    private val TAG = "TeacherAttendance"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_attendance)

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

        cardStackView = findViewById(R.id.cardStackView)
        layoutManager = CardStackLayoutManager(this, this)
        layoutManager.setStackFrom(StackFrom.Top)
        cardStackView.layoutManager = layoutManager

        // Load students from Firestore
        loadStudentsFromFirestore()
    }

    private fun loadStudentsFromFirestore() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Loading students from Firestore...")
                val students = FirebaseUtils.getAllStudents()

                withContext(Dispatchers.Main) {
                    if (students.isNotEmpty()) {
                        Log.d(TAG, "Loaded ${students.size} students")
                        adapter = StudentCardAdapter(students.toMutableList())
                        cardStackView.adapter = adapter
                        Toast.makeText(this@TeacherAttendanceActivity,
                            "Loaded ${students.size} students", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "No students found in database")
                        Toast.makeText(this@TeacherAttendanceActivity,
                            "No students found. Please add students first.", Toast.LENGTH_LONG).show()
                        // Show mock data as fallback
                        adapter = StudentCardAdapter(getMockStudents())
                        cardStackView.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading students: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeacherAttendanceActivity,
                        "Error loading students: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Show mock data as fallback
                    adapter = StudentCardAdapter(getMockStudents())
                    cardStackView.adapter = adapter
                }
            }
        }
    }

    private fun getMockStudents(): MutableList<com.example.attendance.model.Student> {
        return mutableListOf(
            com.example.attendance.model.Student("USN001","John Doe","1MS20CS001"),
            com.example.attendance.model.Student("USN002","Jane Smith","1MS20CS002"),
            com.example.attendance.model.Student("USN003","Rahul Kumar","1MS20CS003"),
            com.example.attendance.model.Student("USN004","Anita Rao","1MS20CS004")
        )
    }

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.signOut()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeacherAttendanceActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@TeacherAttendanceActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeacherAttendanceActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardSwiped(direction: Direction?) {
        val top = adapter.currentTop() ?: return
        if (direction == Direction.Right) {
            Toast.makeText(this, "${top.name} marked PRESENT", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "${top.name} marked ABSENT", Toast.LENGTH_SHORT).show()
        }
        adapter.removeTop()
    }
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: android.view.View?, position: Int) {}
    override fun onCardDisappeared(view: android.view.View?, position: Int) {}
}
