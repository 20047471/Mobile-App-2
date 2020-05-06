package ie.wit.fragments

import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import ie.wit.main.BackingTrackApp
import ie.wit.utils.getAllBackingTracks
import ie.wit.utils.setMapMarker
import ie.wit.utils.trackLocation

class MapsFragment : SupportMapFragment(), OnMapReadyCallback{

    lateinit var app: BackingTrackApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as BackingTrackApp
        getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        app.mMap = googleMap
        app.mMap.isMyLocationEnabled = true
        app.mMap.uiSettings.isZoomControlsEnabled = true
        app.mMap.uiSettings.setAllGesturesEnabled(true)
        app.mMap.clear()
        trackLocation(app)
        setMapMarker(app)
        getAllBackingTracks(app)
    }
}