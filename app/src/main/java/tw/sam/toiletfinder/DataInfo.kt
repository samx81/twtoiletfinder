package tw.sam.toiletfinder

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_data_info.*
import okhttp3.*
import java.io.IOException
import android.os.AsyncTask.execute
import org.json.JSONArray


class DataInfo : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var toilet: Toilet
    private lateinit var preference:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_data_info)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setSupportActionBar(infotoolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.extras.getString("Name")

        btn.setOnClickListener { sendComment() }
        toilet = intent.extras.getParcelable("toilet")

        preference = PreferenceManager.getDefaultSharedPreferences(this)
        val preferEditor = preference.edit()
        var history  = preference.getString("history","")
        if (history== ""){
            history+="${toilet.Number}"
        }
        else if(history.split(", ").size<10){
            history+=", ${toilet.Number}"
        }
        else{
            history=history.substring(history.indexOf(", ")+2)+", ${toilet.Number}"
        }
        preferEditor.putString("history",history)
        preferEditor.apply()

        getStar()

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

        var types=MyDBHelper(this).getParticularToiletName(toilet.Name)
        var typeOptions = arrayOf(0,0,0,0) //男廁女廁無障礙親子廁
        for(type in types){
            when(type){
                "男女","男女廁","混合廁" -> {typeOptions[0]++
                    typeOptions[1]++}
                "男","男廁"-> typeOptions[0]++
                "女","女廁"-> typeOptions[1]++
                "無障礙","無障礙廁"-> typeOptions[2]++
                "親子","親子廁"-> typeOptions[3]++
            }
        }

        if(typeOptions[0]==0) {
            Restroom.visibility=View.GONE
            mannum.visibility=View.GONE
        }
        else {
            mannum.text=typeOptions[0].toString()
        }
        if(typeOptions[1]==0) {
            women.visibility=View.GONE
            wrnum.visibility=View.GONE
        }
        else wrnum.text=typeOptions[1].toString()
        if(typeOptions[2]==0) {
            restroom.visibility=View.GONE
            rrnum.visibility=View.GONE
        }
        else rrnum.text=typeOptions[2].toString()
        if(typeOptions[3]==0) {
            Kindlyroom.visibility=View.GONE
            krnum.visibility=View.GONE
        }
        else krnum.text=typeOptions[3].toString()
        var sum=0
        for(i in typeOptions){
            sum+=i
        }
        if(sum==0) type2.visibility=View.VISIBLE

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
            R.id.report_btn -> {
                var intent = Intent(this,AddItem::class.java)
                intent.putExtra("status","report")
                intent.putExtra("toilet",toilet)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(toilet.getLatLng()).title(toilet.Name))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toilet.getLatLng(),18f))
    }
    fun sendComment(){
        var httpClient = okhttp3.OkHttpClient()
        if (rb.rating !=0.0f){
            var request = Request.Builder().
                    url("http://1c78066d.ngrok.io/pdo/insert_rating.php?number=${toilet.Number}&stars=${rb.rating}&comment=${editText2.text}").build()
            httpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    Log.d("Mytag", e.toString())
                }
                override fun onResponse(call: Call?, response: Response?) {
                    Snackbar.make(findViewById(android.R.id.content),"發送成功",Snackbar.LENGTH_LONG).show()
                }
            })
        }
        else {
            Snackbar.make(findViewById(android.R.id.content),"星等無法為空",Snackbar.LENGTH_LONG).show()
        }
    }
    fun getStar(){
        var httpClient = okhttp3.OkHttpClient()
        var star=0
        var request = Request.Builder().
                url("http://1c78066d.ngrok.io/pdo/select_rating.php?number=${toilet.Number}").build()
        httpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("Mytag", e.toString())
            }
            override fun onResponse(call: Call?, response: Response?) {
                val responseData = response?.body()?.string()
                val json = JSONArray(responseData)
                Log.d("mytag",json.length().toString())
                for (i in 0..(json.length() - 1)) {
                    val item = json.getJSONObject(i)
                    star += item.get("stars").toString().toInt()
                }
                rb.rating = star.toFloat()
            }
        })
    }
}
