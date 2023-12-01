package com.skr.android.friendlink.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentRegisterBinding

private const val TAG = "RegisterFragment"

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // For Firebase authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val firstName = binding.firstName.text.toString().trim()
            val lastName = binding.lastName.text.toString().trim()
            val phoneNumber = binding.phoneNumber.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
                Snackbar.make(it, "All fields are required", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = firebaseAuth.currentUser
                        val uid = user?.uid

                        uid?.let {
                            // Create a new user with a first and last name
                            val newUser = hashMapOf(
                                "first" to firstName,
                                "last" to lastName,
                                "email" to email,
                                "phoneNumber" to phoneNumber
                            )
                            val documentReference: DocumentReference =
                                firestore.collection("users").document(uid)
                            documentReference.set(newUser)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User details added to Firestore")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding user details to Firestore", e)
                                }
                        }

                        Snackbar.make(it, "Registration successful", Snackbar.LENGTH_SHORT).show()

                        findNavController().navigate(R.id.action_register_to_profile)

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Snackbar.make(it, "Registration failed", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}