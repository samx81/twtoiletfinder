package tw.sam.toiletfinder

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_data_info.*

class DataInfo : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var toilet: Toilet
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_info)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setSupportActionBar(infotoolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.extras.getString("Name")

        toilet = intent.extras.getParcelable("toilet")

        citytext.text = toilet.Country+toilet.City
        addrtext.text = toilet.Address
        owntext.text = toilet.Admin
        if(toilet.Attr.isEmpty()){
            type.visibility= View.GONE
        }
        else {
            type.visibility= View.VISIBLE
            type.text = toilet.Attr
        }
        grade.text = toilet.Grade
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.data_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(toilet.getLatLng()).title(toilet.Name))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toilet.getLatLng(),15f))
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
    }
}
