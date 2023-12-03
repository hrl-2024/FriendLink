package com.skr.android.friendlink.ui.home.launch

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentHomeBinding
import java.util.Date
import java.util.Locale

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // For Firebase authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Get current user
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        var randomFriendId = ""

        val userDocRef = currentUserId?.let { firestore.collection("users").document(it) }

        Log.d(TAG, "User ID: $currentUserId")

        // Get the friend list from the user document
        userDocRef?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Retrieve the friend list field from the document data
                val friendList = document.get("friendList") as? List<String>
                Log.d(TAG, "Friend list: $friendList")

                // Ensure friendList is not null and contains at least one friend
                if (!friendList.isNullOrEmpty()) {
                    // Choose a random friend from the list
                    randomFriendId = friendList.random()

                    if (randomFriendId == "") {
                        val notificationText = resources.getString(R.string.no_friend_text)
                        binding.notificationText.text = notificationText

                        binding.revealFriendButton.isEnabled = false
                    } else {
                        var numNotifications = 3
                        val notificationText = resources.getString(R.string.notification_text, numNotifications)
                        binding.notificationText.text = notificationText

                        binding.revealFriendButton.setOnClickListener {
                            findNavController().navigate(
                                HomeFragmentDirections.actionHomeToSend(randomFriendId, currentUserId.toString())
                            )
                        }
                    }

                } else {
                    Log.d(TAG, "No friends found")
                }
            } else {
                Log.d(TAG, "No user found")
            }
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Error finding user in firestore", e)
        }
        Log.d(TAG, "Random friend ID: $randomFriendId")

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