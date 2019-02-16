package com.junction.findmyway.camera

import com.androidnetworking.error.ANError
import org.json.JSONArray
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.junction.findmyway.camera.data.Compass
import com.junction.findmyway.camera.data.CompassAndText
import com.junction.findmyway.camera.data.GpsAndMagnetic
import org.json.JSONObject


class NetworkManager {
    fun sendGPSandCompassData(gpsAndMagnetic: GpsAndMagnetic) {
        AndroidNetworking.post(endPoint + gpsAndCompassPoint)
            .addBodyParameter("latitude", gpsAndMagnetic.gps.latitude.toString())
            .addBodyParameter("longitude", gpsAndMagnetic.gps.longitude.toString())
            .addBodyParameter("degree", gpsAndMagnetic.compass.degree.toString())
            .setTag("test")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    print(response.toString())
                }

                override fun onError(error: ANError) {
                    print(error.toString())
                }
            })
    }

    fun getCompassData(fxn: (CompassAndText) -> Unit) {
        AndroidNetworking.get(endPoint + compassPoint)
            .setPriority(Priority.HIGH)
            .build().getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val degree = response.getDouble("degree").toFloat()
                    val text = response.getString("text")
                    fxn(CompassAndText(Compass(degree), text))
                }

                override fun onError(error: ANError) {
                    print(error.toString())
                }
            })
    }

    companion object {
        private val endPoint = "http://192.168.8.86:8080"
        private val compassPoint = "/api/trails/follow/1"
        private val gpsAndCompassPoint = "/api/trails"
    }
}