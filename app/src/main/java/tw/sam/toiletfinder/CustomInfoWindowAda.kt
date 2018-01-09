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
        val selectedToilet = db.getParticularToiletData(tolietId)
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

            var types=db.getParticularToiletName(selectedToilet.Name)
            var typeOptions = arrayOf(0,0,0,0) //男廁女廁無障礙親子廁
            for(type in types){
                when(type){
                    "男女","男女廁","混合廁" -> {typeOptions[0]=1
                        typeOptions[1]=1}
                    "男","男廁"-> typeOptions[0]=1
                    "女","女廁"-> typeOptions[1]=1
                    "無障礙","無障礙廁"-> typeOptions[2]=1
                    "親子","親子廁"-> typeOptions[3]=1
                }
            }

            if(typeOptions[0]==0) view.Restroom.visibility=View.GONE else view.Restroom.visibility=View.VISIBLE
            if(typeOptions[1]==0) view.women.visibility=View.GONE else view.women.visibility=View.VISIBLE
            if(typeOptions[2]==0) view.restroom.visibility=View.GONE else view.restroom.visibility=View.VISIBLE
            if(typeOptions[3]==0) view.Kindlyroom.visibility=View.GONE else view.Kindlyroom.visibility=View.VISIBLE
            var sum=0
            for(i in typeOptions){
                sum+=i
            }
            if(sum==0) view.type2.visibility=View.VISIBLE else view.type2.visibility=View.GONE

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