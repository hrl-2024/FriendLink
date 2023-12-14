package com.skr.android.friendlink.ui.home.send

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentSentBinding

private const val TAG = "SentFragment"
class SentFragment : Fragment() {

    private var _binding: FragmentSentBinding? = null

    private val binding get() = _binding!!

    private val sentViewModel: SentViewModel by viewModels()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sentViewModel.quote.observe(viewLifecycleOwner) { quote ->
            binding.quote.text = quote.quote
            val author = "--" + quote.author
            binding.author.text = author
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}