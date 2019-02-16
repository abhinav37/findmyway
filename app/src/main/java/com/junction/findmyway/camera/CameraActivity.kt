/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.junction.findmyway.camera

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.jacksonandroidnetworking.JacksonParserFactory
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.location.LocationManager
import android.support.v4.content.ContextCompat
import com.junction.findmyway.camera.data.Compass
import com.junction.findmyway.camera.data.GPS
import com.junction.findmyway.camera.data.GpsAndMagnetic
import java.security.Permission


class CameraActivity : AppCompatActivity(), SensorEventListener, LocationListener {


    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private val networkManager: NetworkManager = NetworkManager()

    private var currentDegree = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        AndroidNetworking.initialize(applicationContext);
        AndroidNetworking.setParserFactory(JacksonParserFactory())
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0.01f, this)
        }
        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container, Camera2BasicFragment.newInstance())
            .commit()


    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    //region Magentic sensor
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        // get the angle around the z-axis rotated
        if (event != null) {
            val degree: Float = event.values[0]
            val ra = RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )

            // how long the animation will take place
            ra.duration = 210

            // set the animation after the end of the reservation status
            ra.fillAfter = true

            // Start the animation
            //image.startAnimation(ra)
            currentDegree = -degree
        }
    }
    //endregion

    //region Location Block
    override fun onLocationChanged(loc: Location?) {
        if (loc != null)
            networkManager.sendGPSandCompassData(
                GpsAndMagnetic(
                    Compass(currentDegree),
                    GPS(loc.latitude, loc.longitude)
                )
            )
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}
    //endregion
}
