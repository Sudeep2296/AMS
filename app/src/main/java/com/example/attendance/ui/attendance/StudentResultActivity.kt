package com.example.attendance.ui.attendance
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.ui.login.LoginActivity
import com.example.attendance.utils.FirebaseUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class StudentResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_result)

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

        val rv = findViewById<RecyclerView>(R.id.rvResults)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ResultAdapter(getMockResults())
    }
    private fun getMockResults() = listOf(
        com.example.attendance.model.Result("Database Systems","CS301",85,"A"),
        com.example.attendance.model.Result("Operating Systems","CS302",78,"B"),
        com.example.attendance.model.Result("Data Structures","CS303",92,"A+")
    )

    private fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseUtils.signOut()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentResultActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@StudentResultActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudentResultActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
