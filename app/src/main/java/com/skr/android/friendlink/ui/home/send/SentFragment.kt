package com.skr.android.friendlink.ui.home.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentSentBinding

class SentFragment : Fragment() {

    private var _binding: FragmentSentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.closeButton.setOnClickListener {
            findNavController().navigate(R.id.action_sent_to_messageList)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}