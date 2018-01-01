package tw.sam.toiletfinder

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class MyDBHelper(context: Context) : SQLiteOpenHelper(context, "testing_db.db", null, 1) {

    companion object {
        val TABLE_STUDENT: String = "TABLE_STUDENT"
        val ID: String = "ID_"
        val NAME: String = "NAME"
        val AGE: String = "AGE"
    }

    val STUDENT_DATABASE_CREATE ="CREATE TABLE toliet (rowid INTEGER PRIMARY KEY," +
            "Country 	TEXT," +
            "City 	TEXT," +
            "Village 	TEXT," +
            "Number 	TEXT," +
            "Name 	TEXT," +
            "Address 	TEXT," +
            "Administration 	TEXT," +
            "Latitude 	TEXT," +
            "Longitude 	TEXT," +
            "Grade 	TEXT," +
            "Type 	TEXT," +
            "Type2 	TEXT)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(STUDENT_DATABASE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    fun insertStudentData(num:String,name: String, address: String, attr: String,grade: String,lat: String,lng: String,country:String,city:String,admin:String): Long {
        val values = ContentValues()
        values.put("Number", num)
        values.put("Name", name)
        values.put("Address", address)
        values.put("Type2", attr)
        values.put("Grade", grade)
        values.put("Latitude", lat)

        values.put("Longitude", lng)
        values.put("Country",country)
        values.put("City",city)
        values.put("Administration",admin)
        return getWritableDatabase().insert("toliet", null, values)
    }

    fun getAllStudentData(): MutableList<Toilet> {
        val stuList: MutableList<Toilet> = mutableListOf<Toilet>()
        val cursor: Cursor = getReadableDatabase().query("toliet", arrayOf("Number","Name","Country","City", "Address" ,
                "Administration" , "Latitude" , "Longitude" , "Grade" , "Type" , "Type2"), null, null, null, null, null)
        try {
            if (cursor.count != 0) {
                cursor.moveToFirst()
                if (cursor.count > 0) {
                    do {
                        val rowId=cursor.getColumnIndex("rowid")

                        var modName=cursor.getString(cursor.getColumnIndex("Name"))
                        if (modName.indexOf("-")!=-1) modName = modName.split("-")[0]

                        var attr = cursor.getString(cursor.getColumnIndex("Type2"))
                        var type =cursor.getString(cursor.getColumnIndex("Type"))
                        if(type==null) type=""

                        if (attr.equals("超市")) attr = "超商"

                        stuList.add(Toilet(
                                cursor.getString(cursor.getColumnIndex("Number")), modName ,
                                cursor.getDouble(cursor.getColumnIndex("Latitude")) ,
                                cursor.getDouble(cursor.getColumnIndex("Longitude")),
                                cursor.getString(cursor.getColumnIndex("Grade")),
                                type,
                               attr,
                                cursor.getString(cursor.getColumnIndex("Address")),
                                cursor.getString(cursor.getColumnIndex("Country")),
                                cursor.getString(cursor.getColumnIndex("City")),
                                cursor.getString(cursor.getColumnIndex("Administration"))
                        ))
                        /*
                        stuList.add(Toilet(cursor.getInt(cursor.getColumnIndex("rowid")),cursor.getString(cursor.getColumnIndex("Name")) , cursor.getString(cursor.getColumnIndex("Latitude")) ,
                                cursor.getString(cursor.getColumnIndex("Longitude")),cursor.getString(cursor.getColumnIndex("Grade")),cursor.getString(cursor.getColumnIndex("Type"))
                                ,cursor.getString(cursor.getColumnIndex("Type2")),cursor.getString(cursor.getColumnIndex("Address"))
                        ))*/
                    } while ((cursor.moveToNext()))
                }
            }
        } finally {
            cursor.close()
        }

        return stuList
    }

    fun getParticularStudentData(number: String): Toilet? {
        lateinit var particluarToilet: Toilet
        val db = this.readableDatabase
        val cursor: Cursor = getReadableDatabase().query("toliet", arrayOf("Number","Name","Country","City", "Address" ,
                "Administration" , "Latitude" , "Longitude" , "Grade" , "Type" , "Type2"), "Number=?", arrayOf(number) , null, null, null)
        try {
            if (cursor.getCount() != 0) {
                cursor.moveToFirst()
                if (cursor.getCount() > 0) {
                    do {
                        /*var grade :Int= when(cursor.getString(cursor.getColumnIndex("Grade"))){
                            "特優級" -> 5
                            "優等級" -> 4
                            "普通級" -> 3
                            "未評分" -> 99
                            "不合格" -> 0
                            else -> 0
                        }*/
                        var modName=cursor.getString(cursor.getColumnIndex("Name"))
                        var attr = cursor.getString(cursor.getColumnIndex("Type2"))
                        var type =cursor.getString(cursor.getColumnIndex("Type"))
                        if(type==null) type=""
                        Log.d("Mytag",  DatabaseUtils.dumpCurrentRowToString(cursor));
                        if (attr.equals("超市")) attr = "超商"
                        if (modName.indexOf("-")!=-1) modName = modName.split("-")[0]
                        particluarToilet = Toilet(
                                cursor.getString(cursor.getColumnIndex("Number")), modName ,
                                cursor.getDouble(cursor.getColumnIndex("Latitude")) ,
                                cursor.getDouble(cursor.getColumnIndex("Longitude")),
                                cursor.getString(cursor.getColumnIndex("Grade")),
                                type,attr,
                                cursor.getString(cursor.getColumnIndex("Address")),
                                cursor.getString(cursor.getColumnIndex("Country")),
                                cursor.getString(cursor.getColumnIndex("City")),
                                cursor.getString(cursor.getColumnIndex("Administration"))
                        )
                    } while ((cursor.moveToNext()));
                }
            }
            else return null
        } finally {
            cursor.close()
        }

        return particluarToilet
    }
}

