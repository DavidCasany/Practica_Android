package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uvic.tf_202526.atarazaga_dcasany.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Opcional: Amagar la barra superior per veure millor el disseny
        supportActionBar?.hide()
    }
}