package com.skr.android.friendlink.ui.home.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentSentBinding
import android.os.Handler
import android.os.Looper
import com.skr.android.friendlink.databinding.FragmentLoadingBinding

class LoadingFragment : Fragment() {

    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to the 'Message Sent' page after 3 seconds
            findNavController().navigate(R.id.action_loading_to_sent)
        }, 3000)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}