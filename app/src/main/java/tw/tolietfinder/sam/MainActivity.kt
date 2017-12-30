package tw.tolietfinder.sam

import android.content.Context
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable

import android.location.Location
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.facebook.stetho.Stetho
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.maps.android.clustering.ClusterManager
import junit.framework.Test


val MY_PERMISSIONS_REQUEST_LOCATION=99

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Stetho.initializeWithDefaults(this);

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

            } else {
                Toast.makeText(this, " 需要定位功能 ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var mMap: GoogleMap
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setPadding(0,this.resources.getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material),0,0)

        mMap.setInfoWindowAdapter(CustomInfoWindowAda(this))

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),MY_PERMISSIONS_REQUEST_LOCATION)
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener<Location>{location ->
            Snackbar.make(findViewById(android.R.id.content),
                    "Currrent Location:"+LatLng(location.latitude,location.longitude).toString(),
                    Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            if(location != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(
                        LatLng(location.latitude, location.longitude)))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            }
        })
        // Add a marker in Sydney and move the camera
        val fju : Marker = mMap.addMarker(
                MarkerOptions().position(LatLng(25.0354303,121.4324641)).title("FJU University")
        )
        val db = MyDBHelper(this)
        var tolietList = db.getAllStudentData()

        for (toliet in tolietList){
            var iconbitmap = getMarkerIconFromDrawable(toliet.getIcon())
            var tMarkerOptions =MarkerOptions()
                    .position(
                            LatLng(toliet.Latitude,toliet.Longitude)
                    )
                    .title(toliet.Name).snippet(toliet.id.toString())
                    .icon(iconbitmap)
            if (iconbitmap != null) tMarkerOptions.anchor(0.5.toFloat(),0.5.toFloat())
            mMap.addMarker(tMarkerOptions
            ).setTag(toliet)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(fju.position))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))

    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
                startActivity(Intent(this,DataInfo::class.java))
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {
                startActivity(Intent(this,AddItem::class.java))
            }
            R.id.nav_share -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    fun getMarkerIconFromDrawable(id :Int) : BitmapDescriptor? {
        if (id==-1) return null
        lateinit var drawable :Drawable
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            drawable = resources.getDrawable(id,theme)
        } else {
            drawable  = resources.getDrawable(id)
        }
        var bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888);
        var canvas = Canvas()
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

