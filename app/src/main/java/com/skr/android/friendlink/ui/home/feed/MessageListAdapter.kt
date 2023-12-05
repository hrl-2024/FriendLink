package com.skr.android.friendlink.ui.home.feed

import android.content.Context
import android.icu.text.DateIntervalFormat.FormattedDateInterval
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.databinding.ListItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageHolder (val binding: ListItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    fun bind(context: Context, message: Message) {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // get user from firestore using message.senderId
        Log.d("MessageListAdapter", "message.senderId: ${message.sender}")
        val userDocRef = message.sender?.let { firestore.collection("users").document(it) }
        userDocRef?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Retrieve the friend list field from the document data
                val firstName = document.get("first") as? String
                val lastName = document.get("last") as? String
                val profilePictureURL = document.get("profilePicture") as? String
                val fullName = "$firstName $lastName"
                val timestampInMillis = message.timestamp // Replace this with your timestamp in milliseconds
                val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                val formattedDate = dateFormatter.format(Date(timestampInMillis))
                binding.messageTime.text = formattedDate
                binding.messageSenderName.text = fullName
                binding.messageText.text = message.message
                binding.messageTime.text = formattedDate
                Glide.with(context)
                    .load(profilePictureURL)
                    .into(binding.messageSenderPic)
            }
        }
    }
}

class MessageListAdapter (private val context: Context, private val messages: List<Message>) : RecyclerView.Adapter<MessageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMessageBinding.inflate(inflater, parent, false)
        return MessageHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = messages[position]
        holder.bind(context, message)
    }

    override fun getItemCount() = messages.size
}