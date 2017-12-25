package tw.tolietfinder.sam

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by sam on 2017/12/26.
 */
class MyDBHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("CREATE TABLE localToliet" +
                "(_id INTEGER PRIMARY KEY NOT NULL ," +
                "type TEXT," +
                "address text," +
                "lat DECIMAL(9,6)," +
                "leg DECIMAL(9,6) )")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}