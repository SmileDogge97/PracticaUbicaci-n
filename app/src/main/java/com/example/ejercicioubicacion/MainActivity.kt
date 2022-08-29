package com.example.ejercicioubicacion

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : AppCompatActivity() {

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_SOLICITUD_PERMISO = 100

    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequest:LocationRequest ?= null
    var callback:LocationCallback ?= null

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = FusedLocationProviderClient(this)
        iniciarLocationRequest()
    }

    private fun iniciarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validaPermisoUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(
            this,
            permisoFineLocation
        ) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(
            this,
            permisoCoarseLocation
        ) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        //obtiene la última ubicación registrada
       /*fusedLocationClient?.lastLocation?.addOnSuccessListener(this, object : OnSuccessListener<Location> {
                override fun onSuccess(location: Location?) {
                    if (location != null) {
                        Log.e("Location",location.latitude.toString() + " - " + location.longitude.toString());
                        Toast.makeText(
                            applicationContext,
                            location.latitude.toString() + " - " + location.longitude.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            })*/

        callback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (ubicacion in locationResult.locations){
                    Toast.makeText(applicationContext, ubicacion.latitude.toString() + ", "+ubicacion.longitude.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
        fusedLocationClient?.requestLocationUpdates(locationRequest!!,
            callback as LocationCallback, null)
    }

    private fun pedirPermiso() {
        val deboProveerContexto =
            ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if (deboProveerContexto) {
            //mandar un mensaje con explicación adicional
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        requestPermissions(
            arrayOf(permisoFineLocation, permisoCoarseLocation),
            CODIGO_SOLICITUD_PERMISO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODIGO_SOLICITUD_PERMISO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //obtener ubicación
                    obtenerUbicacion()
                } else {
                    Toast.makeText(
                        this,
                        "No diste permiso para acceder a la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (validaPermisoUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermiso()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }

    private fun detenerActualizacionUbicacion() {
        fusedLocationClient?.removeLocationUpdates(callback!!)
    }
}