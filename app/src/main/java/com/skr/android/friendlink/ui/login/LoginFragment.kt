package com.skr.android.friendlink.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentLoginBinding
import com.skr.android.friendlink.ui.login.LoginViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val loginViewModel =
            ViewModelProvider(this).get(LoginViewModel::class.java)

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.login
//        loginViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle button click to navigate to RegisterFragment
        binding.registerButton.setOnClickListener {
            navigateToRegisterFragment()
        }
        binding.loginButton.setOnClickListener{
            navigateToHome()
        }
    }

    private fun navigateToRegisterFragment() {
        // Navigate to the RegisterFragment using the action defined in the navigation graph
        findNavController().navigate(R.id.action_loginFragment_to_navigation_friends)
    }

    private fun navigateToHome(){
        // Navigate to the HomeFragment using the action defined in the navigation graph
        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}