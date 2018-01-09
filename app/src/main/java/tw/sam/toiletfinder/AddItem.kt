package tw.sam.toiletfinder

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import kotlinx.android.synthetic.main.activity_add_item.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response

import java.io.IOException


class AddItem : AppCompatActivity(), OnMapReadyCallback {
    lateinit var currentLocation:LatLng
    lateinit var mMap: GoogleMap
    var selectedtype = ""
    lateinit var function:String
    lateinit var toilet:Toilet
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        setSupportActionBar(infotoolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val spinnerArrAda = arrayOf("公園","寺廟教堂等宗教活動場所","觀光地區及風景區","港區","文化育樂活動場所",
                "森林遊樂區","公營事業機構設置供民眾使用者及其他","各級社教機關", "公家機關設置供民眾使用者",
                "各級機關學校","民眾團體活動場所","百貨公司","加油站","超商","市場","量販店","旅館","醫院",
                "娛樂場所","戲院" ,"餐廳" ,"鐵路局","公路車站服務區及休息站","捷運車站","高鐵","航空站")

        val spinnerAda = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,spinnerArrAda)
        spinnerAda.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typespinner.adapter = spinnerAda

        val attrspinnerArrAda = arrayOf("男女廁","男廁","女廁","無障礙廁","親子廁")

        val attrspinnerAda = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,attrspinnerArrAda)
        attrspinnerAda.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        attrspinner.adapter = attrspinnerAda

        function= intent.extras.getString("status","")
        if(function == "report"){
            supportActionBar?.title="錯誤回報"
            toilet = intent.extras.getParcelable("toilet")
            currentLocation=toilet.getLatLng()
            itemname.setText(toilet.Name)
            typespinner.setSelection(spinnerAda.getPosition(toilet.Attr))
            itemlocation.setText(toilet.Country+toilet.City)
            itemnaddr.setText(toilet.Address)
            attrspinner.setSelection(attrspinnerAda.getPosition(toilet.Type))
            legname.setText(toilet.Admin)

        }
        else{
            currentLocation = intent.extras.get("currentLocation") as LatLng
        }

        typespinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedtype = parent.getItemAtPosition(position).toString()
                }
             // to close the onItemSelected
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        var db = MyDBHelper(this)
        fab.setOnClickListener {
            submit()
        }
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(currentLocation).draggable(true).title("按住拖動標記以重新指定座標")).showInfoWindow()
        mMap.setOnMarkerDragListener(object:GoogleMap.OnMarkerDragListener{
            override fun onMarkerDragStart(p0: Marker?) {}

            override fun onMarkerDrag(p0: Marker?) {}

            override fun onMarkerDragEnd(marker:Marker){
                currentLocation = marker.position
                Snackbar.make(findViewById(android.R.id.content),currentLocation.toString(),Snackbar.LENGTH_LONG).show()
            }
        })

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,18f))
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
    fun submit() {

        var httpClient = okhttp3.OkHttpClient()

        var db = MyDBHelper(this)
        if (function == "report") {
            if (!itemlocation.text.isEmpty() && !itemname.text.isEmpty() && !itemnaddr.text.isEmpty()) {
                var request = Request.Builder().
                        url("http://1c78066d.ngrok.io/pdo/report_wrong_restroom.php?number=${toilet.Number}&comment={${itemlocation.text.substring(0..2)}" +
                                "},{${itemlocation.text.substring(3)}}," +
                                "{${itemlocation.text}},{${itemname.text}}," +
                                "{${itemnaddr.text}},{${currentLocation.latitude}}," +
                                "{${currentLocation.longitude}},{$selectedtype}").build()
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.d("Mytag", e.toString())
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        Snackbar.make(findViewById(android.R.id.content), "成功送出，請等待審核", Snackbar.LENGTH_SHORT).show()
                        db.insertCustomData(itemname.text.toString(), itemnaddr.text.toString(), selectedtype,
                                lat = currentLocation.latitude.toString(), lng = currentLocation.longitude.toString(), city = itemlocation.text.substring(3),
                                country = itemlocation.text.substring(0..2))
                        finish()
                    }
                })
            } else {
                Snackbar.make(findViewById(android.R.id.content), "有欄位為空，請輸入", Snackbar.LENGTH_LONG).show()
            }
        }
        else {
            if (!itemlocation.text.isEmpty() && !itemname.text.isEmpty() && !itemnaddr.text.isEmpty()) {
                var request = Request.Builder().
                        url("http://1c78066d.ngrok.io/pdo/insert_restroom.php?country=${itemlocation.text.substring(0..2)}" +
                                "&city=${itemlocation.text.substring(3)}" +
                                "&village=${itemlocation.text}&name=${itemname.text}" +
                                "&address=${itemnaddr.text}&latitude=${currentLocation.latitude}" +
                                "&longitude=${currentLocation.longitude}&type=$selectedtype").build()
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.d("Mytag", e.toString())
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        Snackbar.make(findViewById(android.R.id.content), "成功送出，請等待審核", Snackbar.LENGTH_SHORT).show()
                        db.insertCustomData(itemname.text.toString(), itemnaddr.text.toString(), selectedtype,
                                lat = currentLocation.latitude.toString(), lng = currentLocation.longitude.toString(), city = itemlocation.text.substring(3),
                                country = itemlocation.text.substring(0..2))
                        finish()
                    }
                })
            } else {
                Snackbar.make(findViewById(android.R.id.content), "有欄位為空，請輸入", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
