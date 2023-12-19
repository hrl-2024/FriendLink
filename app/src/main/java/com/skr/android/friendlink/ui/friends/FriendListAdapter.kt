package com.skr.android.friendlink.ui.friends

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.ListItemFriendBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FriendHolder (val binding: ListItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    fun bind(friend: Friend, clickListener: (Friend) -> Unit) {
        val name = friend.firstName + " " + friend.lastName
        binding.friendName.text = name
        binding.friendPhoneNumber.text = friend.phoneNumber
        // Set different background color based on registration status
//        val backgroundColor = if (!friend.registered) {
//            binding.root.context.getColor(R.color.transparent)
//        } else {
//            binding.root.context.getColor(R.color.transparent)
//        }

        // Desaturate the profile image if the contact is not registered on FriendLink
        if (!friend.registered) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            binding.profileImg.setImageDrawable(binding.root.context.getDrawable(R.drawable.profile_pic))
            binding.profileImg.colorFilter = filter
        } else {
            Log.d("FriendListAdapter", "friend.profilePicture: ${friend.profilePicture}")
            Glide.with(binding.root.context)
                .load(friend.profilePicture)
                .into(binding.profileImg)
            binding.profileImg.colorFilter = null
        }

//        binding.root.setBackgroundColor(backgroundColor)
        binding.root.setOnClickListener {
            clickListener(friend)
        }
    }
}

class FriendListAdapter (private val friends: List<Friend>,private val registeredClickListener: (Friend) -> Unit, private val unregisteredClickListener: (Friend) -> Unit) : RecyclerView.Adapter<FriendHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFriendBinding.inflate(inflater, parent, false)
        return FriendHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val friend = friends[position]
        val clickListener: (Friend) -> Unit = if (friend.registered) {
            registeredClickListener
        } else {
            unregisteredClickListener
        }
        holder.bind(friend, clickListener)
    }

    override fun getItemCount() = friends.size
}