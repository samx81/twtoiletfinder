package tw.sam.toiletfinder

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Geocoder
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import kotlinx.android.synthetic.main.nav_header_main.view.*
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.*


val MY_PERMISSIONS_REQUEST_LOCATION=99

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {

    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var mSettingsClient :SettingsClient
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var mLocationCallback :LocationCallback
    private lateinit var geocoder:Geocoder
    private var httpClient = OkHttpClient()

    private var mCurrentLocation :LatLng = LatLng(25.047940, 121.513713)
    private var currentCity="臺北市"
    private var previousCity=""

    lateinit private var nearestToilet: Toilet
    private lateinit var toiletList:MutableList<Toilet>
    private var updatelist = mutableListOf<Toilet>()

    private var mMapisReady=false
    private var typeFilter = booleanArrayOf(true,true,true,true,true,true,true) // 依序為 公共，私人，運輸，餐廳，遊樂場所，醫院，無屬性
    lateinit var checkingType:MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(infotoolbar) // 工具列的佈署
        Stetho.initializeWithDefaults(this) // FB 的 debug 工具佈署
        // 抽屜最近的廁所那一欄的點擊偵測
        nav_view.getHeaderView(0).toiletnav.setOnClickListener {
            if(nearestToiletdis!=-1){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearestToilet.getLatLng(),20f))
                drawer_layout.closeDrawer(GravityCompat.START)
            }
        }
        checkingType=getCheckingType(typeFilter)
        //抽屜佈署
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, infotoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        //抽屜項目點擊偵測
        nav_view.setNavigationItemSelectedListener(this)

        //取得廁所資料 from 資料庫
        toiletList = MyDBHelper(this).getAllStudentData()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)// 檢查權限，如果沒有的話就要求
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
        }
        else{
            // 地圖佈署
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) //取得座標的模組
            mSettingsClient = LocationServices.getSettingsClient(this)
            geocoder = Geocoder(this, Locale.getDefault()) //取得地理位置的模組
            //下面這邊很麻煩懶的寫
            createLocationCallback()
            createLocationRequest()
            buildLocationSettingsRequest()
            startLocationUpdates()
        }


    }
    // 權限授權後的檢查
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this) //取得座標的模組
                mSettingsClient = LocationServices.getSettingsClient(this)
                geocoder = Geocoder(this, Locale.getDefault()) //取得地理位置的模組
                //下面這邊很麻煩懶的寫
                createLocationCallback()
                createLocationRequest()
                buildLocationSettingsRequest()
                startLocationUpdates()
            } else {
                Toast.makeText(this, " 需要定位功能 ", Toast.LENGTH_SHORT).show()
            }
        }
    }

/* 這邊就留著
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

    // 多久檢查一次位置的設定
    fun createLocationRequest() {
        mLocationRequest.interval = 50000
        mLocationRequest.fastestInterval =20000
        mLocationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    //建立檢查位置的設定
    private fun buildLocationSettingsRequest() {
        var builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    //主要在檢查完位置後要做的事情
    private fun createLocationCallback() {
        mLocationCallback = object:LocationCallback() {
            override fun onLocationResult(locationResult:LocationResult) {
                super.onLocationResult(locationResult)

                mCurrentLocation = LatLng(locationResult.getLastLocation().latitude,locationResult.getLastLocation().longitude)

                snackbarshow("Currrent Location:"+mCurrentLocation.toString())

                getCurrentCity()

                if(mMapisReady){
                    //從本地資料庫撈資料放在地圖上
                    placeMarker(mMap,toiletList,currentCity)
                    mMapisReady=false
                }

                //搜尋最近的廁所，要拿去放在抽屜裡面用的
                getNearest(toiletList)

                if(!downloading){
                    getNearest(updatelist)
                }


                //找到目前位置後移動地圖的鏡頭
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation,18f))

                //更新抽屜訊息
                updateNav()

                //如果後台沒在下載，地理位置也沒變，不下載新資料
                if(!downloading && previousCity!=currentCity) {
                    getDataFromDB(currentCity)
                    placeMarker(mMap,toiletList,currentCity)
                }

                if(updatelist.size>0 && finishupdate){
                    snackbarshow("放置圖示中..")
                    placeMarker(mMap,updatelist,currentCity)

                    finishupdate=false
                }
            }
        }
    }

    private fun startLocationUpdates() {
        // 持續更新位置的背景處理函式
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, {locationSettingsResponse->

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

    fun getCurrentCity(){
        //取得地理位置
        try {
            var getLocation=geocoder.getFromLocation(mCurrentLocation.latitude,mCurrentLocation.longitude,1)
            if(!getLocation.isEmpty()){
                previousCity=currentCity
                currentCity=getLocation[0].locality
            }

            snackbarshow(currentCity)
        } catch (e:IOException) {
            e.printStackTrace()
        }
    }

    private lateinit var mMap: GoogleMap
    private var nearestToiletdis:Int = -1

    //地圖主要的操作函式
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setPadding(0,this.resources.getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material),0,0) //工具欄會擋住地圖

        //自訂資訊視窗，然後增加點擊偵測
        mMap.setInfoWindowAdapter(CustomInfoWindowAda(this))
        mMap.setOnInfoWindowClickListener(this)

        //檢查權限，如果可以就開目前地點，更新目前地點
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = true

            //目前地點檢查
            mFusedLocationClient.lastLocation.addOnCompleteListener(this, {task ->
                if(task.isSuccessful && task.result != null) {
                    mCurrentLocation = LatLng(task.getResult().latitude,task.getResult().longitude)

                    snackbarshow("地圖確認當前座標:"+mCurrentLocation.toString())
                    getCurrentCity()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation,15f))
                }
                else {
                    Log.w("Tag", "getLastLocation:exception", task.exception);
                }
            })
        }
        else {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),MY_PERMISSIONS_REQUEST_LOCATION)
        }


        //輔大的Marker，測試用
        val fju : Marker = mMap.addMarker(
                MarkerOptions().position(LatLng(25.0354303,121.4324641)).title("FJU University")
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(25.047940, 121.513713)))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
        mMapisReady=true
    }

    private fun updateNav(){
        var navView = nav_view.getHeaderView(0) //撈出layout
        if(this::nearestToilet.isInitialized){
            nearestToiletdis =computeDistanceBetween(mCurrentLocation, nearestToilet.getLatLng()).toInt() //算最近距離
            navView.nearestFromHere.text= "${nearestToilet.Name}\n最近距離為：$nearestToiletdis 公尺"
            navView.nearestTolietIcon.setImageResource(nearestToilet.getIcon())
        }
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
            R.id.clear_btn -> mMap.clear() //清除icon
            R.id.place_btn -> //台北車站的Marker，測試用
                mMap.addMarker(
                MarkerOptions().position(LatLng(25.047780, 121.517333)).title("FJU University")
        )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //抽屜項目選擇後的動作
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {

            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {
                startActivity(Intent(this,History::class.java))
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

    //生圖示用的函式
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

    //資訊視窗點擊後的動作
    override fun onInfoWindowClick(p0: Marker?) {

        var toiletInfo = Intent(this,DataInfo::class.java) //做包裹
        if(p0 != null) {
            toiletInfo.putExtra("toilet",p0.tag as Toilet)
            toiletInfo.putExtra("Name",p0.title)
        }
        Log.d("Mytag",  p0?.tag.toString());
        startActivity(toiletInfo) //送進詳細資訊欄
    }

    //下方小黑條的函式
    fun snackbarshow(str:String){
        Snackbar.make(findViewById(android.R.id.content), str,
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }
    private var downloading= false
    private var finishupdate=false
    //資料庫取得函式
    fun getDataFromDB(city:String){
        Log.d("mytag",city)
        var request = Request.Builder().
                url("http://1c78066d.ngrok.io/pdo/select_city.php?city=$currentCity").build()
        var db=MyDBHelper(this)

        downloading=true

        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                snackbarshow("get data failed")
                Log.d("Mytag", e.toString())
            }
            override fun onResponse(call: Call?, response: Response?) {
                val responseData = response?.body()?.string()
                val json = JSONArray(responseData)
                Log.d("mytag",json.length().toString())
                for (i in 0..(json.length() - 1)) {
                    Log.d("mytag",json.length().toString())
                    snackbarshow("更新資料中:($i/${json.length()})")
                    val item = json.getJSONObject(i)
                    val num = item.get("number").toString()
                    var name = item.get("name").toString()
                    val country = item.get("country").toString() //城市
                    val city = item.get("city").toString() //鄉鎮區
                    val address = item.get("address").toString()
                    //6
                    val admin = item.get("administration").toString()
                    val latitude = item.get("latitude").toString()
                    val longitude = item.get("longitude").toString()
                    val grade = item.get("grade").toString()
                    var type = item.get("owned_by").toString() //類型

                    var attr = item.get("type").toString() //屬性

                    if (name.indexOf("-")!=-1) name = name.split("-")[0]
                    if (attr=="" && name.indexOf("-")!=-1) attr = name.split("-")[1]
                    if (db.getParticularStudentData(num) == null) {
                        db.insertStudentData(num, name, address, type,attr, grade, latitude, longitude, city, country, admin)

                        if (type== "超市") type = "超商"
                        var newToilet = Toilet(num, name, latitude.toDouble(), longitude.toDouble(), grade, attr,type , address, city, country, admin)
                        updatelist.add(newToilet)
                    }
                    finishupdate=true
                }
                downloading=false
            }
        })
    }

    fun placeMarker(googleMap: GoogleMap, toilets:MutableList<Toilet>, location:String){
        var lastitem =""
        for (toliet in toilets){
            if(toliet.City==location && checkingType.indexOf(toliet.Attr)!=-1){
                var iconbitmap = getMarkerIconFromDrawable(toliet.getIcon()) //生圖示
                //檢查一下會不會被沒有類型的資料蓋掉
                if (lastitem!=toliet.Name || toliet.getIcon() != R.drawable.empty_icon){
                    var tMarkerOptions =MarkerOptions()
                            .position(toliet.getLatLng())
                            .title(toliet.Name)
                            .icon(iconbitmap)
                    //調整錨點
                    if (toliet.getIcon() != R.drawable.empty_icon) tMarkerOptions.anchor(0.5.toFloat(),0.5.toFloat())

                    //放進地圖且加上物件（可能是這邊會造成地圖記憶體過量，但這樣比較方便
                    googleMap.addMarker(tMarkerOptions).setTag(toliet)
                    snackbarshow("Adding Toilet:(${toliet.Name})")
                }
            }
        }
    }
    fun getCheckingType(types:BooleanArray): MutableList<String>{
        var checkingType= mutableListOf<String>()
        for(i in 0..types.size-1) {
            if (types[i]) {
                when (i) {
                    0 -> checkingType.addAll(arrayOf("公園", "寺廟教堂等宗教活動場所", "觀光地區及風景區", "港區", "文化育樂活動場所",
                            "森林遊樂區", "公營事業機構設置供民眾使用者及其他", "各級社教機關",
                            "公家機關設置供民眾使用者", "各級機關學校", "民眾團體活動場所"))

                    1 -> checkingType.addAll(arrayOf("百貨公司", "加油站", "超商", "市場", "量販店", "旅館"))
                    2 -> checkingType.addAll(arrayOf("鐵路局", "公路車站服務區及休息站", "捷運車站", "高鐵", "航空站"))
                    3 -> checkingType.add("餐廳")
                    4 -> checkingType.addAll(arrayOf("娛樂場所", "戲院"))
                    5 -> checkingType.add("醫院")
                    6 -> checkingType.add("")
                }
            }
        }
        return checkingType
    }
    fun getNearest(toilets:MutableList<Toilet>){
        for (toliet in toilets){
            if(checkingType.indexOf(toliet.Attr)!=-1){
                var distance = if(mCurrentLocation!=LatLng(0.0,0.0)) computeDistanceBetween(mCurrentLocation,toliet.getLatLng()).toInt()
                else 9999
                if (nearestToiletdis == -1 || nearestToiletdis > distance){
                    nearestToiletdis = distance
                    nearestToilet = toliet
                }
            }
        }
    }
}
