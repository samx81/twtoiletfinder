package tw.tolietfinder.sam

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.content_add_item.*


class AddItem : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {

        }
    }

}
