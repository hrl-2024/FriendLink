package com.skr.android.friendlink.ui.friends

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.databinding.FragmentFriendsBinding
import kotlinx.coroutines.launch

private const val TAG = "FriendsFragment"

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val friendsViewModel: FriendsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val friendsViewModel =
            ViewModelProvider(this).get(FriendsViewModel::class.java)
        // Set up Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            val friendsList = getFriends()

            val usersCollection = firestore.collection("users")

            friendsList.forEach { friend ->
                Log.d(TAG, "Checking if ${friend.phoneNumber} is registered")
                usersCollection.whereEqualTo("phoneNumber", friend.phoneNumber).get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            Log.d(TAG, "${friend.phoneNumber} is registered")
                            // If a user exists with this phone number
                            val userProfilePic = document.getString("profilePicture")
                            val userEmail = document.getString("email")

                            // Update the Friend object with retrieved details
                            friend.profilePicture = userProfilePic ?: ""
                            friend.email = userEmail ?: ""
                            friend.registered = true
                            friend.id = document.id

                        }
                        Log.d(TAG, "${friendsList}")

                        Log.d(TAG, "Got ${friendsList.size} phone numbers from contacts")
                        val friendListAdapter = FriendListAdapter(
                            friendsList,
                            registeredClickListener = { clickedFriend ->
                                showFriendRequestDialog(clickedFriend)
                            },
                            unregisteredClickListener = { clickedFriend ->
                                sendSMSDialog(clickedFriend)
                            }
                        )
                        binding.friendRecyclerView.adapter = friendListAdapter
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                    }
            }

        }
    }

    private fun showFriendRequestDialog(friend: Friend) {
        AlertDialog.Builder(requireContext())
            .setTitle("Add Friend")
            .setMessage("Do you want to become friends with ${friend.firstName} ${friend.lastName}?")
            .setPositiveButton("Add") { _, _ ->
                // Implement the logic to send a friend request
                // Example: You can perform the necessary action here
                addFriend(friend)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addFriend(friend: Friend) {
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid
        val userDocRef = currentUserId?.let { firestore.collection("users").document(it) }
        val friendDocRef = friend.id?.let { firestore.collection("users").document(it) }

        userDocRef?.get()?.addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val friendList = documentSnapshot.get("friendList") as? List<String> ?: emptyList()
                if (friendList.contains(friend.id)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Already Friends")
                        .setMessage("You are already friends with ${friend.firstName} ${friend.lastName}")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    // Friend is not in the friend list, proceed to add the friend
                    val newFriendList = friendList.toMutableList()
                    newFriendList.add(friend.id)
                    userDocRef.update("friendList", newFriendList)

                    // Add yourself to the friend's friend list
                    friendDocRef?.get()?.addOnSuccessListener { friendDocumentSnapshot ->
                        if (friendDocumentSnapshot != null && friendDocumentSnapshot.exists()) {
                            val friendFriendList = friendDocumentSnapshot.get("friendList") as? List<String> ?: emptyList()
                            val newFriendFriendList = friendFriendList.toMutableList()
                            currentUserId?.let { newFriendFriendList.add(it) }
                            friendDocRef.update("friendList", newFriendFriendList)
                        }
                    }
                }
            }
        }
    }


    private fun sendSMSDialog(friend: Friend) {

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${friend.phoneNumber}")
            putExtra("sms_body", "Hey! I'm using FriendLink, a new social media app. Download it!")
        }
        startActivity(smsIntent)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("Range")
    fun getFriends() : List<Friend> {

        // check permission
        if (ContextCompat.checkSelfPermission(requireActivity(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                1
            )

            return emptyList()
        }

        val friendsList = mutableListOf<Friend>()

        val contentResolver = requireActivity().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            "DISPLAY_NAME ASC"
        )

        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
              // split name by whitespace
              val nameList = name.split("\\s".toRegex())
              val firstName = nameList[0]
              val lastName = nameList[1]
                val phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt()

                if (phoneNumber <= 0) {
                    continue
                } else {
                    val cursorPhone = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                        arrayOf(id),
                        null
                    )
                    if (cursorPhone!!.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            Log.d(TAG, "Name: $name")
                            Log.d(TAG, "Phone Number: $phoneNumValue")
                            val phoneNumberStr = extractNumbersFromString(phoneNumValue)

                            friendsList.add(Friend(id, firstName,lastName, "", phoneNumberStr,"",false))
                        }
                    }
                    cursorPhone.close()
                }
            }
        }

        cursor.close()

        return friendsList
    }
    private fun extractNumbersFromString(input: String): String {
        // Use regex to match only numeric digits
        val regex = Regex("[^0-9]")
        return input.replace(regex, "")
    }
}

