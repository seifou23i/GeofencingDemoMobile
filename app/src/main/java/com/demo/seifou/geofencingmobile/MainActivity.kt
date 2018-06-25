package com.demo.seifou.geofencingmobile

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import java.util.ArrayList

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    companion object {
        val TAG: String = "MainActivity"
        private val REQ_PERMISSION: Int = 100
    }

    private lateinit var mGeofenceList: ArrayList<Geofence>
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mAddGeofencesButton: Button

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddGeofencesButton = findViewById(R.id.add_geofences_button) as Button
        mGeofenceList = ArrayList<Geofence>()
        populateGeofenceList()// Get the geofences used (hard coded in this sample)
        buildGoogleApiClient()

        mAddGeofencesButton.setOnClickListener(View.OnClickListener {
            if (!mGoogleApiClient.isConnected) {
                Toast.makeText(this@MainActivity, "Google API Client not connected!", Toast.LENGTH_SHORT)
                        .show()
                return@OnClickListener
            }

            if (checkPermission()) {
                addGeofencesHandler()
                Toast.makeText(this@MainActivity, "Google API Client not connected!", Toast.LENGTH_SHORT)

            } else {
                askPermission()
                addGeofencesHandler()

            }
        })
    }

    private fun addGeofencesHandler() {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(),
                getGeofencePendingIntent())
                .setResultCallback(this) // Result processed in onResult().
    }

    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        Log.d(TAG, "===============> checkPermission()")
        // Ask for permission if it wasn't granted yet
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Asks for permission
    private fun askPermission() {
        Log.d(TAG, "===============> askPermission()")
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_PERMISSION)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun populateGeofenceList() {
        Constants.LANDMARKS.forEach { key, value ->
            mGeofenceList.add(Geofence.Builder().setRequestId(key)
                    .setCircularRegion(value.latitude, value.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build())
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        Log.d(TAG, "===============> getGeofencingRequest()")
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
        builder.addGeofences(mGeofenceList)
        return builder.build()
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        Log.d(TAG, "===============> getGeofencePendingIntent()")
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onStart() {
        super.onStart()
        if (!mGoogleApiClient.isConnecting || !mGoogleApiClient.isConnected) {
            mGoogleApiClient.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnecting || mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "===============> onConnected()")
    }

    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "===============> onConnected()")
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG,
                "===============> onConnectionFailed() - result: " + connectionResult.errorCode)
    }

    override fun onResult(status: Status) {
        Log.d(TAG, "===============> onResult()")
        if (status.isSuccess) {
            Toast.makeText(this, "Geofences Added", Toast.LENGTH_SHORT).show()
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(status.statusCode)
            Log.d(TAG, "===============> errorMessage: " + errorMessage)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d(TAG, "===============> onRequestPermissionsResult()")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                addGeofencesHandler()
            } else {
                // Permission denied
                Toast.makeText(this, "In order to allow location services, permission must be granted",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
}
