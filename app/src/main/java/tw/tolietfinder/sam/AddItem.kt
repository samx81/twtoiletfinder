package tw.tolietfinder.sam

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.content_add_item.*
import org.jetbrains.anko.db.*

class AddItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            database.use{
                insert("Toliet",
                        "name" to itemname.text.toString(),
                        "address" to itemnaddr.text.toString(),
                        "type" to itemtype.text.toString(),
                        "location" to itemlocation.text.toString(),
                        "lat" to itemlat.text.toString(),
                        "leg" to legname.text.toString())
            }
        }
    }

}
