package com.uvic.tf_202526.atarazaga_dcasany.Apps

import android.content.Context
import com.uvic.tf_202526.atarazaga_dcasany.Database.AppDatabase

class AppSingleton private constructor() {

    lateinit var db: AppDatabase
        private set

    companion object {
        @Volatile
        private var instance: AppSingleton? = null

        fun getInstance(): AppSingleton {
            return instance ?: synchronized(this) {
                instance ?: AppSingleton().also { instance = it }
            }
        }
    }

    // Aquest mètode es cridarà només una vegada al principi
    fun init(context: Context) {
        db = AppDatabase.Companion.getDatabase(context)
    }
}