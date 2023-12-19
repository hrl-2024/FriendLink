package com.skr.android.friendlink.ui.home.send

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.Model
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.BuildConfig
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentSendBinding
import com.skr.android.friendlink.ui.home.feed.Message
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private const val TAG = "SendFragment"

private const val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY

class SendFragment : Fragment() {

    private var _binding: FragmentSendBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val binding get() = _binding!!

    private val args: SendFragmentArgs by navArgs()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSendBinding.inflate(inflater, container, false)
        val root: View = binding.root
        var randomFriendId: String = args.randomFriendId
        val currentUserId = args.currentUserId

        // Set up Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Get the friend's name
        firestore.collection("users")
            .document(randomFriendId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val friendFirstName = document.getString("first")
                    val friendLastName = document.getString("last")
                    binding.friendName.text = "$friendFirstName $friendLastName"
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        binding.sendButton.setOnClickListener {
            val messageText = binding.message.text.toString()

            val message = Message(
                sender = currentUserId,
                receiver = randomFriendId,
                message = messageText,
                timestamp = System.currentTimeMillis()
            )

            if (message != null) {
                sendMessage(message, currentUserId, randomFriendId)
            }
            findNavController().navigate(R.id.action_send_to_loading)
        }

        // Create the prompt
        val openAI = OpenAI(
            OPENAI_API_KEY,
            timeout = Timeout(socket = 3.seconds),
        )

        // Retrieve model
        val completionRequest = CompletionRequest(
            model = ModelId("davinci"),
            prompt = "Give me a thoughtful prompt: ",
            echo = false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "Fetching prompt")
                val completion: TextCompletion = openAI.completion(completionRequest)
//                completion.choices.forEach {
//                    it.message.content?.let { it1 -> Log.d(TAG, it1) }
//                }
                Log.d(TAG, "Completion: $completion")
            }
        }

        return root
    }

    private fun sendMessage(
        message: Message,
        currentUserId: String,
        toFriendId: String
    ) {
        firestore.collection("messages")
            .add(message)
            // Adding to sent list
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Message sent successfully with ID: ${documentReference.id}")
                val messageId = documentReference.id // Assuming you have the message ID

                // Update the sentList for the current user by adding the new message ID to the existing list
                currentUserId.let { uid ->
                    val userDocRef = firestore.collection("users").document(uid)
                    userDocRef.update("sentList", FieldValue.arrayUnion(messageId))
                        .addOnSuccessListener {
                            Log.d(TAG, "Message added to sent list")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding message to sent list", e)
                        }
                    userDocRef.update("lastMessageSent", System.currentTimeMillis())
                        .addOnSuccessListener {
                            Log.d(TAG, "Last message sent updated")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating last message sent", e)
                        }
                }

                // Update the receivedList for the friend in a similar way
                toFriendId.let { friendId ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}