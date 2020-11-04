package ir.mahakco.apps.koltinlocationwatch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Looper
import android.provider.Settings
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

private const val CODE_REQUEST_LOCATION = 101
private const val CODE_RESULT_LOCATION = 102
private const val CODE_REQUEST_SETTING_LOCATION = 103

class LocationUtils {
    //    companion object {
//
//
//        @Volatile
//        private var INSTANCE: LocationUtils? = null
//        private lateinit var activity: Activity
//
//        fun getInstance(activity: Activity): LocationUtils =
//            INSTANCE ?: synchronized(this) {
//                this.activity = activity
//                return INSTANCE ?: LocationUtils(activity).also { INSTANCE = it }
//            }﻿
//    }
    private var activity: Activity

    private lateinit var callBack : ((locationResult: LocationResult) -> Unit)

    private var fusedLocationClient: FusedLocationProviderClient

    lateinit var settingsClient: SettingsClient

    lateinit var locationRequest: LocationRequest

    lateinit var locationCallback: LocationCallback

    constructor(activity: Activity) {
        this.activity = activity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        createLocationCallBack()
    }

    private fun createLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                callBack(locationResult)
                //Do what you want with the position here

            }
        }
    }

    fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        settingsClient = LocationServices.getSettingsClient(activity)

        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

                // All location settings are satisfied. The client can initialize location
                // requests here.

            } catch (exception :ApiException) {
                when(exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolve = exception as ResolvableApiException
                        resolve.startResolutionForResult(activity ,CODE_REQUEST_SETTING_LOCATION)
                        showDialogPermission()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        showDialogPermission()
                    }
                }
            }
        }
//        task.addOnSuccessListener { locationSettingsResponse ->
//            // All location settings are satisfied. The client can initialize
//            // location requests here.
//            // ...
////            if (haveLocationPermission()) {
//                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
////            }
//        }
//
//        task.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException) {
//                // Location settings are not satisfied, but this can be fixed
//                // by showing the user a dialog.
//                try {
//                    // Show the dialog by calling startResolutionForResult(),
//                    // and check the result in onActivityResult().
//                    //exception.startResolutionForResult(this@MainActivity,
//                    //REQUEST_CHECK_SETTINGS)
//                    showDialogPermission()
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    // Ignore the error.
//                }
//            }
//        }

    }
    private fun haveLocationPermission() = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun requestUserLocation(callback: (locationResult: LocationResult) -> Unit) {
        this.callBack = callback
        if (!haveLocationPermission()) {
            showDialogPermission()
        }
        else {
            createLocationRequest()
        }
    }

    private fun showDialogPermission() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_dialog_permission)
        val body = dialog.findViewById(R.id.body) as TextView
        body.text = "Application needs your location. Please allow accessing your location!"
        val yesBtn = dialog.findViewById(R.id.yesButton) as Button
        val noBtn = dialog.findViewById(R.id.noButton) as TextView
        yesBtn.setOnClickListener {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                CODE_REQUEST_LOCATION
            )
            dialog.dismiss()
        }
        noBtn.setOnClickListener { activity.finish() }
        dialog.show()
    }

    fun onPermissionResult(requestCode: Int,
                           permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CODE_REQUEST_LOCATION -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showDialogPermission()
                } else {
                    createLocationRequest()
//                    startGpsIntent()
                }
            }
        }
    }

    private fun startGpsIntent() {
        activity.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) , CODE_RESULT_LOCATION)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CODE_RESULT_LOCATION -> {
                if (resultCode != Activity.RESULT_OK) {
                    showDialogPermission()
                }
                else {
                    createLocationRequest()
                }
            }
        }
    }
}