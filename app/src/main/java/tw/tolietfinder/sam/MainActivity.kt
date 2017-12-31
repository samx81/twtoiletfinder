package tw.tolietfinder.sam

import android.content.Context
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Looper

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

import android.widget.Toast
import com.facebook.stetho.Stetho
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*


val MY_PERMISSIONS_REQUEST_LOCATION=99

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var mSettingsClient :SettingsClient
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var mLocationCallback :LocationCallback
    private var mCurrentLocation :LatLng = LatLng(0.0,0.0)

    private lateinit var tolietList:MutableList<Toliet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Stetho.initializeWithDefaults(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this);

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        tolietList = MyDBHelper(this).getAllStudentData()

        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
        startLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

            } else {
                Toast.makeText(this, " 需要定位功能 ", Toast.LENGTH_SHORT).show()
            }
        }
    }
/*
    override protected fun onActivityResult(requestCode:Int resultCode:Int, data: Intent) {
        when (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS ->
                when (resultCode) {
                    Activity.RESULT_OK ->
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                    Activity.RESULT_CANCELED ->:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                }
        }
    }
    */

    private var mLocationRequest = LocationRequest()
    fun createLocationRequest() {
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval =5000
        mLocationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private fun buildLocationSettingsRequest() {
        var builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }
    private fun createLocationCallback() {
        mLocationCallback = object:LocationCallback() {
             override fun onLocationResult(locationResult:LocationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = LatLng(locationResult.getLastLocation().latitude,locationResult.getLastLocation().longitude)
                 Snackbar.make(findViewById(android.R.id.content),
                         "Currrent Location:"+mCurrentLocation.toString(),
                         Snackbar.LENGTH_LONG)
                         .setAction("Action", null).show()

                 for (toliet in tolietList){
                     var distance = if(mCurrentLocation!=LatLng(0.0,0.0)) computeDistanceBetween(mCurrentLocation,toliet.getLatLng()).toInt()
                     else 9999
                     if (nearestTolietdis == -1 || nearestTolietdis  > distance){
                         nearestTolietdis  = distance
                         nearestToliet = toliet
                     }
                 }
                 updateNav()
            }
        }
    }

    private fun startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, {locationSettingsResponse->
                        //Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper())

                    }


                })/*
                .addOnFailureListener(this, {e ->
                        var statusCode = e as ApiException.getStatusCode()
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });*/
    }


    private lateinit var mMap: GoogleMap
    private var nearestTolietdis:Int = -1
    lateinit private var nearestToliet:Toliet

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

        mFusedLocationClient.lastLocation.addOnCompleteListener(this, {task ->

            if(task.isSuccessful && task.getResult() != null) {
                mCurrentLocation = LatLng(task.getResult().latitude,task.getResult().longitude)

                Snackbar.make(findViewById(android.R.id.content),
                        "Currrent Location:"+mCurrentLocation.toString(),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()

                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            }
            else {
                Log.w("Tag", "getLastLocation:exception", task.exception);
            }
        })
        // Add a marker in Sydney and move the camera
        val fju : Marker = mMap.addMarker(
                MarkerOptions().position(LatLng(25.0354303,121.4324641)).title("FJU University")
        )

        for (toliet in tolietList){
            var iconbitmap = getMarkerIconFromDrawable(toliet.getIcon())
            var distance = if(mCurrentLocation!=LatLng(0.0,0.0)) computeDistanceBetween(mCurrentLocation,toliet.getLatLng()).toInt()
                            else 9999
            var tMarkerOptions =MarkerOptions()
                    .position(toliet.getLatLng())
                    .title(toliet.Name)
                    .icon(iconbitmap)
            if (iconbitmap != null) tMarkerOptions.anchor(0.5.toFloat(),0.5.toFloat())
            mMap.addMarker(tMarkerOptions).setTag(toliet)
            if (nearestTolietdis == -1 || nearestTolietdis  > distance){
                nearestTolietdis  = distance
                nearestToliet = toliet
            }
        }
        var navView = nav_view.getHeaderView(0)
        navView.nearestFromHere.text= "${nearestToliet.Name} 最近距離為：$nearestTolietdis 公尺"
        navView.nearestTolietIcon.setImageResource(nearestToliet.getIcon())
        mMap.moveCamera(CameraUpdateFactory.newLatLng(fju.position))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))

    }
    private fun updateNav(){
            var navView = nav_view.getHeaderView(0)
            nearestTolietdis=computeDistanceBetween(mCurrentLocation,nearestToliet.getLatLng()).toInt()
            navView.nearestFromHere.text= "${nearestToliet.Name} 最近距離為：$nearestTolietdis 公尺"
            navView.nearestTolietIcon.setImageResource(nearestToliet.getIcon())
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

