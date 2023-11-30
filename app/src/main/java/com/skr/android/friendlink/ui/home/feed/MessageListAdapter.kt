package com.skr.android.friendlink.ui.home.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skr.android.friendlink.databinding.ListItemMessageBinding

class MessageHolder (val binding: ListItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message) {
        binding.messageSenderName.text = message.sender
        binding.messageText.text = message.message
        binding.messageTime.text = message.timestamp.toString()
    }
}

class MessageListAdapter (private val messages: List<Message>) : RecyclerView.Adapter<MessageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMessageBinding.inflate(inflater, parent, false)
        return MessageHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount() = messages.size
}