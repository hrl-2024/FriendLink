package com.skr.android.friendlink.ui.home.launch

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skr.android.friendlink.DailyMessageBoolean
import com.skr.android.friendlink.R
import com.skr.android.friendlink.databinding.FragmentHomeBinding
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // For Firebase authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var countDownTimer: CountDownTimer

    // For current location
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager

    // For location by GPS
    private var locationByGps: Location? = null
    val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByGps= location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    // For location by network
    private var locationByNetwork: Location? = null
    val networkLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationByNetwork= location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Get current location
        getCurrentLocation()
        Log.d(TAG, "Current location: ${currentLocation?.latitude}, ${currentLocation?.longitude}")

        // Create the view model
        val lat = currentLocation?.latitude as Double
        val lon = currentLocation?.longitude as Double

        val viewModelFactory = HomeViewModelFactory(lat, lon)
        val homeViewModel =
            ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        // set the temperature
        homeViewModel.weatherInfo.observe(viewLifecycleOwner) { weatherInfo ->
            val temp = weatherInfo.temp.toInt().toString() + "°C"
            binding.temperature.text = temp
        }

        // get random friend id and bind the view accordingly
        getRandomFriend()

        // Get the current date
        val currDate = getCurrDate()
        // Set the current date to the TextView using the binding
        binding.dayMonth.text = currDate

        return root
    }

    private fun getRandomFriend() {
        // Get current user
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        var randomFriendId = ""

        val userDocRef = currentUserId?.let { firestore.collection("users").document(it) }

        Log.d(TAG, "User ID: $currentUserId")

        //        DELETE THIS LATER
        DailyMessageBoolean.resetBoolean(requireContext())

        val isAvailable = DailyMessageBoolean.isBooleanAvailable(requireContext())
        Log.d(TAG, "Is available: $isAvailable")

        if (isAvailable) {
            // Get the friend list from the user document
            userDocRef?.get()?.addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Retrieve the friend list field from the document data
                    val friendList = document.get("friendList") as? List<String>
                    Log.d(TAG, "Friend list: $friendList")

                    // Ensure friendList is not null and contains at least one friend
                    if (!friendList.isNullOrEmpty()) {
                        // Choose a random friend from the list
                        randomFriendId = friendList.random()

                        if (randomFriendId == "") {
                            val notificationText = resources.getString(R.string.no_friend_text)
                            binding.notificationText.text = notificationText

                            binding.revealFriendButton.isEnabled = false
                        } else {
                            var numNotifications = 3
                            val notificationText =
                                resources.getString(R.string.notification_text, numNotifications)
                            binding.notificationText.text = notificationText

                            binding.revealFriendButton.setOnClickListener {
                                DailyMessageBoolean.useBoolean(requireContext())
                                findNavController().navigate(
                                    HomeFragmentDirections.actionHomeToSend(
                                        randomFriendId,
                                        currentUserId.toString()
                                    )
                                )
                            }
                        }
                    } else {
                        Log.d(TAG, "No friends found")
                    }
                } else {
                    Log.d(TAG, "No user found")
                }
            }?.addOnFailureListener { e ->
                Log.e(TAG, "Error finding user in firestore", e)
            }

        } else {
            binding.revealFriendButton.isEnabled = false
            val timeUntilNextDay = getTimeUntilNextDay()
            countDownTimer = object : CountDownTimer(timeUntilNextDay, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = ((millisUntilFinished % (1000 * 60 * 60)) % (1000 * 60)) / 1000

                    val formattedTime =
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    binding.revealFriendButton.text = formattedTime
                }

                override fun onFinish() {
                    binding.revealFriendButton.text = "00:00:00"
                    // If needed, reset or perform actions when countdown finishes
                }
            }.start()
            binding.revealFriendButton.setTextColor(resources.getColor(R.color.white))
        }
        Log.d(TAG, "Random friend ID: $randomFriendId")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrDate(): String {
        val dateFormat = SimpleDateFormat("d MMMM", Locale.getDefault())
        val currDate = Date(System.currentTimeMillis())
        return dateFormat.format(currDate)
    }

    private fun getTimeUntilNextDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        return calendar.timeInMillis - System.currentTimeMillis()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askLocationPermission(): Boolean {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
        return isLocationPermissionGranted()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (isLocationPermissionGranted()) {
            locationManager = requireActivity().getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsLocationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, networkLocationListener)
            locationByGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            locationByNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            currentLocation = if (locationByGps != null) {
                locationByGps
            } else if (locationByNetwork != null) {
                locationByNetwork
            } else {
                null
            }
        } else {
            askLocationPermission()
            getCurrentLocation()
        }
    }
}