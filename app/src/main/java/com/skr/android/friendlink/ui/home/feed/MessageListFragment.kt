package com.skr.android.friendlink.ui.home.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.skr.android.friendlink.databinding.FragmentMessageListBinding
import kotlinx.coroutines.launch

class MessageListFragment : Fragment() {

    private var _binding: FragmentMessageListBinding? = null
    private val binding get() = checkNotNull(_binding) {
        "Cannot access binding before onCreateView() or after onDestroyView()"
    }

    private val messageListViewModel: MessageListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageListBinding.inflate(inflater, container, false)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageListViewModel.fetchMessages()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageListViewModel.messages.collect { messages ->
                        // If messages exist, display the RecyclerView with the adapter
                        binding.messageRecyclerView.adapter = MessageListAdapter(requireContext(), messages)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // avoid memory leak
    }
}