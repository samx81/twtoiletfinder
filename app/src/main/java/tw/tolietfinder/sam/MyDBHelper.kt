package tw.tolietfinder.sam

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


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

    fun insertStudentData(name: String, age: Int): Long {
        val values = ContentValues()
        values.put(NAME, name)
        values.put(AGE, age)
        return getWritableDatabase().insert(TABLE_STUDENT, null, values)
    }

    fun getAllStudentData(): MutableList<Toliet> {
        val stuList: MutableList<Toliet> = mutableListOf<Toliet>()
        val cursor: Cursor = getReadableDatabase().query("toliet", arrayOf("rowid","Name", "Address" ,
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
                        if (attr.equals("超市")) attr = "超商"

                        stuList.add(Toliet(
                                cursor.getInt(rowId), modName ,
                                cursor.getDouble(cursor.getColumnIndex("Latitude")) ,
                                cursor.getDouble(cursor.getColumnIndex("Longitude")),
                                cursor.getString(cursor.getColumnIndex("Grade")),
                                cursor.getString(cursor.getColumnIndex("Type")),
                               attr,
                                cursor.getString(cursor.getColumnIndex("Address"))
                        ))
                        /*
                        stuList.add(Toliet(cursor.getInt(cursor.getColumnIndex("rowid")),cursor.getString(cursor.getColumnIndex("Name")) , cursor.getString(cursor.getColumnIndex("Latitude")) ,
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

    fun getParticularStudentData(id:Int): Toliet {
        lateinit var particluarToliet :Toliet
        val db = this.readableDatabase
        val cursor: Cursor = getReadableDatabase().query("toliet", arrayOf("rowid","Name", "Address" ,
                "Administration" , "Latitude" , "Longitude" , "Grade" , "Type" , "Type2"), "rowid=?", arrayOf(id.toString()) , null, null, null)
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
                        val rowId=cursor.getColumnIndex("rowid")
                        var modName=cursor.getString(cursor.getColumnIndex("Name"))
                        if (modName.indexOf("-")!=-1) modName = modName.split("-")[0]
                        particluarToliet = Toliet(
                                cursor.getInt(rowId), modName ,
                                cursor.getDouble(cursor.getColumnIndex("Latitude")) ,
                                cursor.getDouble(cursor.getColumnIndex("Longitude")),
                                cursor.getString(cursor.getColumnIndex("Grade")),
                                cursor.getString(cursor.getColumnIndex("Type")),
                                cursor.getString(cursor.getColumnIndex("Type2")),
                                cursor.getString(cursor.getColumnIndex("Address"))
                        )
                    } while ((cursor.moveToNext()));
                }
            }
        } finally {
            cursor.close()
        }

        return particluarToliet
    }
}

