package tw.sam.toiletfinder

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by sam on 2017/12/28.
 */
//
class Toilet(val Number:String, var Name:String, val Latitude :Double, val Longitude :Double, var Grade:String
             , var Type: String, var Attr:String, val Address:String,var City:String,var Country:String,var Admin:String) :Parcelable{


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    fun getIcon() : Int{
        var assast :Int = when(Attr){

            "公園","寺廟教堂等宗教活動場所","觀光地區及風景區","港區","文化育樂活動場所",
            "森林遊樂區","公營事業機構設置供民眾使用者及其他","各級社教機關",
            "公家機關設置供民眾使用者","各級機關學校","民眾團體活動場所" -> R.drawable.public_icon

            "百貨公司","加油站","超商","市場","量販店","旅館" -> R.drawable.private_icon
            "醫院" -> R.drawable.hospital_icon
            "娛樂場所","戲院" -> R.drawable.play_icon
            "餐廳" -> R.drawable.restaurant_icon
            "鐵路局","公路車站服務區及休息站","捷運車站","高鐵","航空站" -> R.drawable.transpot_icon
            else -> R.drawable.empty_icon
        }

        return assast
    }
    fun getLatLng():LatLng{
        return LatLng(Latitude,Longitude)
    }
    /*
         var _Grade= Grade
         var _Type= Type+Type2

         , Village:String, Number :String, Name:String, Address :String,
                  Administration :String

                  Grade :String, Type :String, Type2 :String

         */


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Toilet> {
        override fun createFromParcel(parcel: Parcel): Toilet {
            return Toilet(parcel)
        }

        override fun newArray(size: Int): Array<Toilet?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Number)
        parcel.writeString(Name)
        parcel.writeDouble(Latitude)
        parcel.writeDouble(Longitude)
        parcel.writeString(Grade)
        parcel.writeString(Type)
        parcel.writeString(Attr)
        parcel.writeString(Address)
        parcel.writeString(City)
        parcel.writeString(Country)
        parcel.writeString(Admin)
    }
}

