 package com.example.swapit1
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.swapit1.R
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
        val backButton = toolbar.findViewById<ImageButton>(R.id.backButton)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        chipNavigationBar = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // إعداد الـ NavController مع الـ Toolbar
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_addOffer , R.id.navigation_myAccount)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // ربط الشريط مع NavController - هنا سنستخدم مستمع يدوي بسبب chipNavigationBar
        chipNavigationBar.setOnItemSelectedListener { id ->
            if (id != navController.currentDestination?.id) {
                navController.navigate(id)
            }
        }

        // تحديث اختيار الشريط عند تغير الوجهة في NavController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            chipNavigationBar.setItemSelected(destination.id, true)

            // تعديل العنوان وزر الرجوع مثل كودك الأصلي
            if (destination.id == R.id.navigation_dashboard || destination.id == R.id.navigation_addOffer || destination.id == R.id.navigation_myAccount) {
                // إخفاء التول بار العام
                findViewById<Toolbar>(R.id.myToolbar).visibility = View.VISIBLE

                // إظهار تول بار الهوم (من داخل الفراجمنت أو عبر include)
                findViewById<MaterialToolbar>(R.id.topAppBar)?.visibility = View.GONE
                titleTextView.post {
                    if(destination.id == R.id.navigation_dashboard){
                        titleTextView.text = "سجل التبادل"
                    }else if (destination.id == R.id.navigation_addOffer){
                        titleTextView.text = "إضافة عرض"
                    }else{
                        titleTextView.text = "حسابي"
                    }

                    // تنسيق العنوان في المنتصف
                    val layoutParams = titleTextView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    titleTextView.layoutParams = layoutParams
                    titleTextView.gravity = Gravity.CENTER
                }
//                backButton.visibility = View.VISIBLE
//                backButton.setOnClickListener {
//                    onBackPressedDispatcher.onBackPressed()
//                }
            } else {
                // إخفاء التول بار العام
                findViewById<Toolbar>(R.id.myToolbar).visibility = View.GONE

                // إظهار تول بار الهوم (من داخل الفراجمنت أو عبر include)
                findViewById<MaterialToolbar>(R.id.topAppBar)?.visibility = View.VISIBLE
            }
        }

        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.my_custom_text),  // للعنصر المختار
                ContextCompat.getColor(this, R.color.grey)             // للغير مختار
            )
        )

    }


}
