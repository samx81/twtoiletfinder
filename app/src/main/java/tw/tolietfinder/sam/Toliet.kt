package tw.tolietfinder.sam

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.support.v7.app.AppCompatActivity
/**
 * Created by sam on 2017/12/28.
 */
class Toliet(val id:Int,var Name:String,val Latitude :Double,val Longitude :Double,var Grade:String
             ,var Type: String,var Attr:String, val Address:String) {

    fun getIcon() : Int{
        var assast :Int = when(Attr){

            "公園","寺廟教堂等宗教活動場所","觀光地區及風景區","港區","文化育樂活動場所",
            "森林遊樂區","公營事業機構設置供民眾使用者及其他","各級社教機關",
            "公家機關設置供民眾使用者","各級機關學校商","民眾團體活動場所" -> R.drawable.public_icon

            "百貨公司","加油站","超商","市場","量販店","旅館" -> R.drawable.private_icon
            "醫院" -> R.drawable.hospital_icon
            "娛樂場所","戲院" -> R.drawable.play_icon
            "餐廳" -> R.drawable.restaurant_icon
            "鐵路局","公路車站服務區及休息站","捷運車站","高鐵","航空站" -> R.drawable.transpot_icon
            else -> -1
        }

        return assast
    }
    /*
         var _Grade= Grade
         var _Type= Type+Type2

         , Village:String, Number :String, Name:String, Address :String,
                  Administration :String

                  Grade :String, Type :String, Type2 :String

         */
}

