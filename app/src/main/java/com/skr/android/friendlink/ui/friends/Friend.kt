package com.skr.android.friendlink.ui.friends

data class Friend(
    var id: String,
    var firstName: String,
    var lastName: String,
    var email: String,
    var phoneNumber: String,
    var profilePicture : String,
    var registered: Boolean
)
