package com.uvic.tf_202526.atarazaga_dcasany.Apps

import android.app.Application

class MerchApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicialitzem la Base de Dades
        AppSingleton.Companion.getInstance().init(this)
    }
}