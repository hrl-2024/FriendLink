package com.skr.android.friendlink.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skr.android.friendlink.databinding.ListItemFriendBinding

class FriendHolder (val binding: ListItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(friend: Friend) {
        binding.friendName.text = friend.name
        binding.friendPhoneNumber.text = friend.phoneNumber
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