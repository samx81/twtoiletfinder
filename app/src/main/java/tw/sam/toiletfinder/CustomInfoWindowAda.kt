package tw.sam.toiletfinder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
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
        val selectedToilet = db.getParticularStudentData(tolietId)
        view.infoaddress.text = selectedToilet.Address
        view.grade.text=selectedToilet.Grade
        view.type.text=selectedToilet.Type
        */
        val title = marker.title
        if (title != null) {
            view.tName.text = title
        } else {
            view.tName.text = ""
        }

        val selectedToilet: Toilet
        if (marker.tag !=null) {
            selectedToilet = marker.tag as Toilet
            view.infoaddress.text = selectedToilet.Address
            view.grade.text= selectedToilet.Grade

            if(selectedToilet.Attr.isEmpty()){
                view.type.visibility= View.GONE
            }
            else {
                view.type.visibility= View.VISIBLE
                view.type.text= selectedToilet.Attr
            }
        }

    }
}