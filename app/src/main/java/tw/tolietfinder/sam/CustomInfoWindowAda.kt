package tw.tolietfinder.sam

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import android.widget.TextView
import kotlinx.android.synthetic.main.custom_info_window.view.*


/**
 * Created by sam on 2017/12/30.
 */
class CustomInfoWindowAda (context: Context) : GoogleMap.InfoWindowAdapter{
    val db = MyDBHelper(context)
    var inflater = LayoutInflater.from(context)
    private var mWindow = inflater.inflate(R.layout.custom_info_window, null)
    //private var mContents =  inflater.inflate(R.layout.custom_info_contents, null)


    override fun getInfoWindow(marker: Marker): View? {

            render(marker, mWindow)
            return mWindow
    }

    override fun getInfoContents(marker: Marker): View? {

        //render(marker, mContents)
        return  null//mContents
    }

    private fun render(marker: Marker, view: View) {
        /*
        val tolietId = marker.snippet.toInt()

        val title = marker.title
        if (title != null) {
            view.tName.text = title
        } else {
            view.tName.text = ""
        }
        val selectedToliet = db.getParticularStudentData(tolietId)
        view.infoaddress.text = selectedToliet.Address
        view.grade.text=selectedToliet.Grade
        view.type.text=selectedToliet.Type
        */
        val title = marker.title
        if (title != null) {
            view.tName.text = title
        } else {
            view.tName.text = ""
        }

        val selectedToliet : Toliet
        if (marker.tag !=null) {
            selectedToliet = marker.tag as Toliet
            view.infoaddress.text = selectedToliet.Address
            view.grade.text=selectedToliet.Grade
            view.type.text=selectedToliet.Attr
        }

    }
}