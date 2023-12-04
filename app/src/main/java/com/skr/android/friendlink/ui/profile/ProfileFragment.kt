package com.skr.android.friendlink.ui.profile

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentProfileBinding
import com.skr.android.friendlink.ui.home.feed.MessageListAdapter
import com.skr.android.friendlink.ui.home.feed.MessageListViewModel
import kotlinx.coroutines.launch

private const val TAG = "ProfileFragment"

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val messageListViewModel: MessageListViewModel by viewModels()

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Get current user
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        val userDocRef = userId?.let { firestore.collection("users").document(it) }

        userDocRef?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val firstName = document.getString("first")
                val lastName = document.getString("last")
                val email = currentUser.email
                val phoneNumber = document.getString("phoneNumber")
//                val profilepicURL = currentUser.photoUrl
                val profilepicURL = document.getString("profilePicture")

                // Update UI with retrieved user data
                binding.firstName.text = "$firstName"
                binding.lastName.text = "$lastName"
                binding.phoneNumber.text = "$phoneNumber"
                if (profilepicURL != null) {
                    Glide.with(requireContext())
                        .load(profilepicURL)
                        .into(binding.profilePic)
                }
            }
        }
        binding.profilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        binding.logout.setOnClickListener {
            firebaseAuth.signOut()
            findNavController().navigate(R.id.action_profile_to_intro) // to intro (to safe check if we are actually logged out)
        }

        messageListViewModel.fetchMessages(forSent = true)

        binding.sendButton.setOnClickListener {
            // Fetch sent messages
            messageListViewModel.fetchMessages(forSent = true)
        }

        binding.receivedButton.setOnClickListener {
            // Fetch received messages
            messageListViewModel.fetchMessages()
        }

        // For recyclerView
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(context)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageListViewModel.messages.collect { messages ->
                    // If messages exist, display the RecyclerView with the adapter
                    binding.messageRecyclerView.adapter = MessageListAdapter(requireContext(), messages)
                }
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                uploadImageToStorage(selectedImageUri)
            }
        }
    }
    private fun uploadImageToStorage(selectedImageUri: Uri) {
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid
        val profilePicRef = userId?.let { storage.reference.child("profilepics/$it") }
        profilePicRef?.putFile(selectedImageUri)?.addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                val profilePicURL = uri.toString()
                val userDocRef = userId?.let { firestore.collection("users").document(it) }
                userDocRef?.update("profilePicture", profilePicURL)

                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(profilePicURL))
                    .build()
                currentUser.updateProfile(profileUpdates)
                Glide.with(requireContext())
                    .load(profilePicURL)
                    .into(binding.profilePic)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}