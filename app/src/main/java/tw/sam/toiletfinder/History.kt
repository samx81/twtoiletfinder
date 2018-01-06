package tw.sam.toiletfinder

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_history.*

class History : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(historyToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val history=preference.getString("history","")
        val db = MyDBHelper(this)
        var toiletList = mutableListOf<Toilet>()
        for(number in history.split(", ").asReversed()){
            var found  =db.getParticularStudentData(number)
            if(found!=null){
                toiletList.add(found)
            }

        }
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter=historyAdapter(toiletList)
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
