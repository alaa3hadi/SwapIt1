package com.example.swapit1

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.swapit1.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var chipNavigationBar: ChipNavigationBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        val titleTextView = toolbar.findViewById<TextView>(R.id.customToolbarTitle)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHost.navController

        // فقط تحميل NavGraph بدون تغيير startDestination
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.mobile_navigation)
        navController.graph = graph

        chipNavigationBar = findViewById(R.id.nav_view)
        chipNavigationBar.setItemSelected(R.id.navigation_home, true)
        chipNavigationBar.visibility = View.VISIBLE

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_addOffer,
                R.id.navigation_myAccount
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        chipNavigationBar.setOnItemSelectedListener { id ->
            if (id != navController.currentDestination?.id) {
                navController.navigate(id)
            }
        }

        val bottomNavDestinations = setOf(
            R.id.navigation_home,
            R.id.navigation_dashboard,
            R.id.navigation_addOffer,
            R.id.navigation_myAccount
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in bottomNavDestinations) {
                chipNavigationBar.visibility = View.VISIBLE
                chipNavigationBar.setItemSelected(destination.id, true)
            } else {
                chipNavigationBar.visibility = View.GONE
            }

            if (destination.id == R.id.navigation_dashboard ||
                destination.id == R.id.navigation_addOffer ||
                destination.id == R.id.navigation_myAccount
            ) {
                toolbar.visibility = View.VISIBLE
                findViewById<MaterialToolbar>(R.id.topAppBar)?.visibility = View.GONE

                titleTextView.post {
                    titleTextView.text = when (destination.id) {
                        R.id.navigation_dashboard -> "سجل التبادل"
                        R.id.navigation_addOffer  -> "إضافة عرض"
                        else                      -> "حسابي"
                    }
                    val lp = titleTextView.layoutParams as
                            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    lp.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    lp.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    lp.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    lp.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    titleTextView.layoutParams = lp
                    titleTextView.gravity = Gravity.CENTER
                }
            } else {
                toolbar.visibility = View.GONE
                findViewById<MaterialToolbar>(R.id.topAppBar)?.visibility = View.VISIBLE
            }
        }

        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}