package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuaris_table")
data class Usuari(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,

    @ColumnInfo(name = "nom_usuari")
    val nom: String,

    @ColumnInfo(name = "password")
    val contrasenya: String,

    @ColumnInfo(name = "es_streamer")
    val esStreamer: Boolean
)