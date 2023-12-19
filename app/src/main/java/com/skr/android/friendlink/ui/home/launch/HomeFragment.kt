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
            Log.d(TAG, "Weather info changed. New Weather info: $weatherInfo")
            val temp = weatherInfo.temp.toInt().toString() + "°C"
            binding.temperature.text = temp

            // set weather icon
            val weatherIcon = weatherInfo.icon
            // Somehow, the weather icon is not showing up on the UI if coming from the internet
            // val weatherIconUrl = "https://openweathermap.org/img/wn/$weatherIcon@4x.png"
            // Log.d(TAG, "Weather icon url: $weatherIconUrl")
            // binding.weatherIcon.load(weatherIconUrl)
            setWeatherIcon(weatherIcon)
        }

        // get random friend id and bind the view accordingly
        getRandomFriend()

        // Get the current date
        val currDate = getCurrDate()
        // Set the current date to the TextView using the binding
        binding.dayMonth.text = currDate

        binding.revealQuestionButton.setOnClickListener {
            binding.questionText.text = homeViewModel.question.value
            Log.d(TAG, "Question: ${homeViewModel.question.value}")
            binding.questionText.visibility = View.VISIBLE
            binding.revealQuestionButton.visibility = View.GONE
        }

        return root
    }

    private fun setWeatherIcon(weatherIcon: String) {
        when (weatherIcon.dropLast(1)) {
            "01" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_01)
            "02" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_02)
            "03" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_03)
            "04" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_04)
            "09" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_09)
            "10" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_10)
            "11" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_11)
            "13" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_13)
            "50" -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_50)
            else -> binding.weatherIcon.setImageResource(R.drawable.ic_weather_sun)
        }
    }

    private fun getRandomFriend() {
        // Get current user
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        var randomFriendId = ""

        val userDocRef = currentUserId?.let { firestore.collection("users").document(it) }

        Log.d(TAG, "User ID: $currentUserId")

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = calendar.timeInMillis // Start of the current day

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.add(Calendar.MILLISECOND, -1)

        val endOfDay = calendar.timeInMillis

        userDocRef?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val lastMessageSent = document.getLong("lastMessageSent")
                Log.d(TAG, "Last message sent: $lastMessageSent")
                val isMessageSent = lastMessageSent != null && lastMessageSent in startOfDay..endOfDay
                Log.d(TAG, "Is message sent today? $isMessageSent")
                if (!isMessageSent) {
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
                                }
                                else {
                                    showNotifications()
                                    binding.revealFriendButton.setOnClickListener {
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
                                val notificationText = resources.getString(R.string.no_friend_text)
                                binding.notificationText.text = notificationText
                            }
                        } else {
                            Log.d(TAG, "No user found")
                        }
                    }?.addOnFailureListener { e ->
                        Log.e(TAG, "Error finding user in firestore", e)
                    }

                } else {
                    val notificationText = resources.getString(R.string.message_already_sent)
                    binding.notificationText.text = notificationText
                    binding.guideText.visibility = View.GONE
                    binding.heartImage.setImageDrawable(resources.getDrawable(R.drawable.hourglass, null))

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
                            binding.revealFriendButton.isEnabled = true
                        }
                    }.start()
                    binding.revealFriendButton.setTextColor(resources.getColor(R.color.white))
                }
                Log.d(TAG, "Random friend ID: $randomFriendId")
            } else {
                Log.d(TAG, "No such document")
            }
        }?.addOnFailureListener { exception ->
            Log.d(TAG, "Could not get lastMessageSent", exception)
        }

    }

    private fun showNotifications(){
        // Get current user
        val currentUser = firebaseAuth.currentUser
        val currentUserId = currentUser?.uid

        val userDocRef = currentUserId?.let { firestore.collection("users").document(it) }

        userDocRef?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val receivedList = document.get("receivedList") as? List<String>
                // only get count of messages received today
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val startOfDay = calendar.timeInMillis // Start of the current day

                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)

                val endOfDay = calendar.timeInMillis

                Log.d(TAG, "Received list: $receivedList")

                if (!receivedList.isNullOrEmpty()) {
                    var numNotifications = 0

                    for (messageId in receivedList) {
                        val messageDocRef = firestore.collection("messages").document(messageId)
                        messageDocRef.get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val timestamp = document.getLong("timestamp")
                                Log.d(TAG, "Timestamp: $timestamp")
                                Log.d(TAG, "Start of day: $startOfDay")
                                Log.d(TAG, "End of day: $endOfDay")
                                if (timestamp != null && timestamp in startOfDay..endOfDay) {
                                    numNotifications++
                                }
                                Log.d(TAG, "Number of notifications: $numNotifications")
                                val notificationText = resources.getString(R.string.notification_text, numNotifications)
                                binding.notificationText.text = notificationText
                            }
                        }.addOnFailureListener {
                            // Handle failure if needed
                        }
                    }
                } else {
                    val notificationText = resources.getString(R.string.no_messages_text)
                    binding.notificationText.text = notificationText
                }

            }
        }

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