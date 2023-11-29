package com.skr.android.friendlink

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.skr.android.friendlink.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_friends, R.id.navigation_profile
            )
        )
        // Check if the user is logged in
        val isLoggedIn = checkIfLoggedIn()

//        if (isLoggedIn) {
//            // User is logged in, navigate to the main fragment with bottom navigation
//            navController.navigate(R.id.navigation_home)
//        } else {
//
//            // Hide the bottom navigation initially
//            navView.visibility = View.GONE
//
//            // User is not logged in, navigate to the login fragment
//            navController.navigate(R.id.loginFragment)
//
//        }

//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
    private fun checkIfLoggedIn(): Boolean {
        // logic to check if the user is logged in
        return false
    }
}