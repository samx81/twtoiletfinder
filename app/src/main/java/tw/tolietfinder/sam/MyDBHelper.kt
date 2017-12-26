package tw.tolietfinder.sam

import android.content.Context
import android.database.sqlite.SQLiteDatabase

import org.jetbrains.anko.db.*

class MyDBHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
    companion object {
        private var instance: MyDBHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDBHelper {
            if (instance == null) {
                instance = MyDBHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable("Toliet", true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE ,
                "name" to TEXT,
                "address" to TEXT,
                "type" to TEXT,
                "location" to TEXT,
                "lat" to REAL,
                "leg" to REAL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable("User", true)
    }

}

// Access property for Context
val Context.database: MyDBHelper
    get() = MyDBHelper.getInstance(getApplicationContext())