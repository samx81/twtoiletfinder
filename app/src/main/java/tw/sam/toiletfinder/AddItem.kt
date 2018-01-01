package tw.sam.toiletfinder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.content_add_item.*


class AddItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        setSupportActionBar(infotoolbar)

        var db = MyDBHelper(this)
        fab.setOnClickListener {
            /*db.insertStudentData(itemname.text.toString(),itemnaddr.text.toString(),
                    itemtype.text.toString(), itemlocation.text.toString(),
                    itemlat.text.toString(),legname.text.toString())*/
        }
    }

}
