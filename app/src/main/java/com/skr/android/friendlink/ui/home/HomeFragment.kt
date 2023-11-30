package com.skr.android.friendlink.ui.home

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentHomeBinding
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.revealFriendButton.setOnClickListener {
            findNavController().navigate(
                R.id.send_message
            )
        }

        var numNotifications = 3
        val notificationText = resources.getString(R.string.notification_text, numNotifications)
        binding.notificationText.text = notificationText

        // Get the current date
        val currDate = getCurrDate()
        // Set the current date to the TextView using the binding
        binding.dayMonth.text = currDate

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrDate(): String {
        val dateFormat = SimpleDateFormat("d MMMM", Locale.getDefault())
        val currDate = Date(System.currentTimeMillis())
        return dateFormat.format(currDate)
    }

}