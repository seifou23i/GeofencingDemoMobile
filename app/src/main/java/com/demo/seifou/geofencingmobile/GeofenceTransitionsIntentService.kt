package com.demo.seifou.geofencingmobile

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * Created by ubuntu on 6/25/18.
 */
class GeofenceTransitionsIntentService : IntentService(GeofenceTransitionsIntentService.TAG) {

    companion object {
        val TAG = "GeofenceTransitionsIS"
    }

    init {
        Log.d(TAG, "===============> GeofenceTransitionsIntentService()")
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "===============> onHandleIntent()")
        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.errorCode)
            return
        }
        // Get the transition type.
        val geofenceTransition = event.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            val triggeringGeofences = event.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition,
                    triggeringGeofences)

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails)



            val contextText = String.format(this.resources.getString(R.string.abc_action_bar_home_description), "seifou")

            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = android.support.v4.app.NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(this.resources.getString(R.string.abc_action_bar_home_description))
                    .setContentText(contextText)
                    .setContentIntent(pendingNotificationIntent)
                    .setStyle(android.support.v4.app.NotificationCompat.BigTextStyle().bigText(contextText))
                    .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            notificationManager.notify(0, notification)





            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, "===============> Error with transition.")
        }
    }

    private fun getGeofenceTransitionDetails(context: Context, geofenceTransition: Int,
                                             triggeringGeofences: List<Geofence>): String {
        Log.d(TAG, "===============> getGeofenceTransitionDetails()")
        val geofenceTransitionString = getTransitionString(geofenceTransition)
        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = triggeringGeofences.map { geofence -> geofence.requestId }
        return geofenceTransitionString + ": " + TextUtils.join(", ", triggeringGeofencesIdsList)
    }

    private fun sendNotification(description: String) {
        Log.d(TAG, "===============> sendNotification()")
        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(this)

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(description)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent)

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Get an instance of the Notification manager
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }

    private fun getTransitionString(transitionType: Int): String {
        Log.d(TAG, "===============> getTransitionString()")
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> return getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> return getString(R.string.geofence_transition_exited)
            else -> return getString(R.string.unknown_geofence_transition)
        }
    }
}
