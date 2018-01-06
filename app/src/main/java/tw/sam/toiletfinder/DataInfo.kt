package tw.sam.toiletfinder

import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
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
    private lateinit var preference:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_info)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setSupportActionBar(infotoolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.extras.getString("Name")

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
            history=history.substring(history.indexOf(", "))+", ${toilet.Number}"
        }
        preferEditor.putString("history",history)
        preferEditor.apply()
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

        var typeOptions = arrayOf(0,0,0,0) //男廁女廁無障礙親子廁
        when(toilet.Type){
            "男女","男女廁","混合廁"-> {typeOptions[0]=1
                typeOptions[1]=1}
            "男","男廁"-> typeOptions[0]=1
            "女","女廁"-> typeOptions[1]=1
            "無障礙","無障礙廁"-> typeOptions[2]=1
            "親子","親子廁"-> typeOptions[3]=1
        }

        if(typeOptions[0]==0) Restroom.visibility=View.GONE
        if(typeOptions[1]==0) women.visibility=View.GONE
        if(typeOptions[2]==0) restroom.visibility=View.GONE
        if(typeOptions[3]==0) Kindlyroom.visibility=View.GONE
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
                AlertDialog.Builder(this).setMultiChoiceItems(arrayOf<CharSequence>("測試1","測試2"),null,DialogInterface.OnMultiChoiceClickListener
                { dialogInterface, i, b ->
                    if(b) Snackbar.make(findViewById(android.R.id.content),"$i",Snackbar.LENGTH_LONG)
                            .setAction("Action",null)
                            .show() })
                        .setPositiveButton("OK",DialogInterface.OnClickListener {
                            dialogInterface, i ->  })
                        .create().show()
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
