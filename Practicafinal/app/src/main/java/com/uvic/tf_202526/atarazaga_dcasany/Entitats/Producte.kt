package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productes_table")
data class Producte(
    @PrimaryKey(autoGenerate = true)
    val pid: Int = 0,

    @ColumnInfo(name = "nom")
    val nom: String,

    @ColumnInfo(name = "descripcio")
    val descripcio: String,

    @ColumnInfo(name = "preu")
    val preu: Double,

    // Guardarem la ruta de la imatge (URI) com a text.
    // Posa '?' perquè pot ser null si encara no n'ha triat cap.
    @ColumnInfo(name = "imatge_uri")
    val imatgeUri: String?,

    @ColumnInfo(name = "es_oferta")
    val esOferta: Boolean = false,

    // IMPORTANT: Això vincula el producte amb el Streamer que l'ha creat
    @ColumnInfo(name = "id_creador")
    val idCreador: Int
)