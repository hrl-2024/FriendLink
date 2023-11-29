package com.skr.android.friendlink

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = "RegisterActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.registerButton.setOnClickListener{
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                         Log.d(TAG, "createUserWithEmail:success")
                         val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                         Log.w(TAG, "createUserWithEmail:failure", task.exception)
                         Toast.makeText(baseContext, "Authentication failed.",
                         Toast.LENGTH_SHORT).show()
                        // updateUI(null)
                    }

                }
        }

    }
}