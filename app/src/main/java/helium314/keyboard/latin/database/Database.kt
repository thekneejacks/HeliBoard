// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database private constructor(context: Context, name: String = NAME) : SQLiteOpenHelper(context, name, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        onUpgrade(db, 0, VERSION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 1) {
            //db.execSQL(GestureDataDao.CREATE_TABLE)
        }
        if (oldVersion <= 2) {
            //db.execSQL(ClipboardDao.ADD_FILE_COLUMN)
            //db.execSQL(ClipboardDao.ADD_MIME_TYPE_COLUMN)
        }
        if (oldVersion <= 4) {
            //db.execSQL(ClipboardDao.ADD_FILE_COLUMN)
            db.execSQL("DROP TABLE CLIPBOARD")
        }
    }

    companion object {
        private val TAG = Database::class.java.simpleName
        private const val VERSION = 4
        const val NAME = "heliboard.db"
        private var instance: Database? = null
        fun getInstance(context: Context): Database {
            if (instance == null)
                instance = Database(context)
            return instance!!
        }

    }
}
