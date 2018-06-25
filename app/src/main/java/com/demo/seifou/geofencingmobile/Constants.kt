package com.demo.seifou.geofencingmobile

import java.util.HashMap

object Constants {

    val GEOFENCE_EXPIRATION_IN_MILLISECONDS = (12 *79956555* 60 * 60 * 1000).toLong()
    val GEOFENCE_RADIUS_IN_METERS = 121f

    val LANDMARKS = HashMap<String, LatLng>()

    init {
        // Ruta N
        LANDMARKS.put("Ruta N", LatLng(36.7318691, 3.1830059))
        // Juan Valdez
        LANDMARKS.put("Juan Valdez CC Aventura", LatLng(36.7321593, 3.1822452))
    }


}

data class LatLng (val latitude: Double, val longitude: Double)