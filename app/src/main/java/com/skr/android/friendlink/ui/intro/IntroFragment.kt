package com.skr.android.friendlink.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentRegisterBinding

// If in the future we want to add an nice intro for our app, we can use this fragment
class IntroFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // For Firebase authentication
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()

        // Get current user
        val currentUser = firebaseAuth.currentUser

        // Check if user is signed in
        if (currentUser == null) {
            findNavController().navigate(R.id.go_to_login)
        } else {
            findNavController().navigate(R.id.go_to_home)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}