package tw.sam.toiletfinder

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_history.*

class History : AppCompatActivity() {
    var submitPage = false
    lateinit var submitHisAda : historyAdapter
    lateinit var clickedHis: historyAdapter
    val db = MyDBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(historyToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val history=preference.getString("history","")
        var toiletList = mutableListOf<Toilet>()
        for(number in history.split(", ").asReversed()){
            var found  =db.getParticularToiletData(number)
            if(found!=null){
                toiletList.add(found)
            }

        }
        historyRecycler.layoutManager = LinearLayoutManager(this)
        clickedHis = historyAdapter(toiletList)
        historyRecycler.adapter=clickedHis
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.submitHistory->{
                if(!submitPage){
                    submitHisAda = historyAdapter(db.getAlCustomData())
                    historyRecycler.adapter=submitHisAda
                    supportActionBar?.title = "貢獻紀錄"
                    submitPage=!submitPage
                }
                else{
                    supportActionBar?.title = "歷史紀錄"
                    historyRecycler.adapter = clickedHis
                    submitPage=!submitPage
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
