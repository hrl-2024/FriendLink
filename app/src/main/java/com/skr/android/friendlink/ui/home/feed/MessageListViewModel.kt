package com.skr.android.friendlink.ui.home.feed

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessageListViewModel : ViewModel() {
    // Replace MutableLiveData with MutableStateFlow for messages
    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())

    // Expose StateFlow instead of LiveData for observing messages
    val messages: StateFlow<List<Message>>
        get() = _messages.asStateFlow()


    // Function to fetch messages from Firestore
    fun fetchMessages(forSent: Boolean = false) {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        val userDocRef = userId?.let { firestore.collection("users").document(it) }

        userDocRef?.get()?.addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {

                var messageList = emptyList<String>()

                if (forSent) {
                    messageList = documentSnapshot.get("sentList") as? List<String> ?: emptyList()
                } else {
                    messageList = documentSnapshot.get("receivedList") as? List<String> ?: emptyList()
                }

                if (messageList.isNotEmpty()) {
                    firestore.collection("messages")
                        .whereIn(FieldPath.documentId(), messageList)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val messages = mutableListOf<Message>()

                            for (document in querySnapshot.documents) {
                                val message = Message(
                                    sender = document.getString("sender") ?: "",
                                    receiver = document.getString("receiver") ?: "",
                                    message = document.getString("message") ?: "",
                                    timestamp = document.getLong("timestamp") ?: 0L
                                )
                                messages.add(message)
                            }
                            if (messages.isNotEmpty()) {
                                _messages.value = messages
                            } else {
                                // No messages found
                                _messages.value = emptyList()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.d("MessageListViewModel", "Error getting documents: ", e)
                            // Handle failure in fetching messages from Firestore
                        }
                } else {
                    // No messages found
                    _messages.value = emptyList()
                }
            }
        }
    }
}