package com.skr.android.friendlink.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.ListItemFriendBinding

class FriendHolder (val binding: ListItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(friend: Friend) {
        val name = friend.firstName + " " + friend.lastName
        binding.friendName.text = name
        binding.friendPhoneNumber.text = friend.phoneNumber
        // Set different background color based on registration status
        val backgroundColor = if (!friend.registered) {
            binding.root.context.getColor(R.color.grey)
        } else {
            binding.root.context.getColor(R.color.white)
        }
        binding.root.setBackgroundColor(backgroundColor)
    }
}

class FriendListAdapter (private val friends: List<Friend>) : RecyclerView.Adapter<FriendHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFriendBinding.inflate(inflater, parent, false)
        return FriendHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount() = friends.size
}