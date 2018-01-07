package tw.sam.toiletfinder

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.content_add_item.*


class AddItem : AppCompatActivity(), OnMapReadyCallback {
    lateinit var currentLocation:LatLng
    lateinit var mMap: GoogleMap
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
        currentLocation = intent.extras.get("currentLocation") as LatLng

        var db = MyDBHelper(this)
        fab.setOnClickListener {

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
}
