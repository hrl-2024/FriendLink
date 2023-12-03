package com.skr.android.friendlink.ui.home.send

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentSendBinding
import com.skr.android.friendlink.ui.home.feed.Message

class SendFragment : Fragment() {

    private var _binding: FragmentSendBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var randomFriendId: String
    var TAG = "SendFragment"
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSendBinding.inflate(inflater, container, false)
        val root: View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        var friendExists = false


        // Get current user
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        val userDocRef = userId?.let { firestore.collection("users").document(it) }

// Get the friend list from the user document
        userDocRef?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Retrieve the friend list field from the document data
                val friendList = document.get("friendList") as? List<String>

                // Ensure friendList is not null and contains at least one friend
                if (!friendList.isNullOrEmpty()) {
                    // Choose a random friend from the list
                    randomFriendId = friendList.random()
                    val friendDocRef = randomFriendId?.let { firestore.collection("users").document(it) }
                    friendDocRef?.get()?.addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val friendFirstName = document.getString("first")
                            val friendLastName = document.getString("last")
                            binding.friendName.text = "$friendFirstName $friendLastName"
                        }
                    }
                    friendExists = true

                } else {
                    friendExists = false
                    Log.d(TAG, "No friends found")

                }
            } else {
                Log.d(TAG, "No user found")
            }
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Error finding user in firestore", e)
        }
//        if (!friendExists) {
//            binding.friendName.text = "No friends found"
//            findNavController().navigate(R.id.action_send_to_sent)
//        }




        binding.sendButton.setOnClickListener {
            val messageText = binding.message.text.toString()

            val message = currentUser?.let { it1 ->
                Message(
                    sender = it1.uid,
                    receiver = randomFriendId,
                    message = messageText,
                    timestamp = System.currentTimeMillis()
                )
            }
            if (message != null) {
                firestore.collection("messages")
                    .add(message)
                    // Adding to sent list
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "Message sent successfully with ID: ${documentReference.id}")
                        val messageId = documentReference.id // Assuming you have the message ID

// Update the sentList for the current user by adding the new message ID to the existing list
                        userId?.let { uid ->
                            val userDocRef = firestore.collection("users").document(uid)
                            userDocRef.update("sentList", FieldValue.arrayUnion(messageId))
                                .addOnSuccessListener {
                                    Log.d(TAG, "Message added to sent list")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error adding message to sent list", e)
                                }
                        }

// Update the receivedList for the friend in a similar way
                        randomFriendId?.let { friendId ->
                            val friendDocRef = firestore.collection("users").document(friendId)
                            friendDocRef.update("receivedList", FieldValue.arrayUnion(messageId))
                                .addOnSuccessListener {
                                    Log.d(TAG, "Message added to received list")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error adding message to received list", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error sending message", e)
                    }
            }
            findNavController().navigate(R.id.action_send_to_sent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}