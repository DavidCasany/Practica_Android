package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productes_table")
data class Producte(
    @PrimaryKey(autoGenerate = true)
    val pid: Int = 0,

    @ColumnInfo(name = "nom")
    var nom: String,

    @ColumnInfo(name = "descripcio")
    var descripcio: String,

    @ColumnInfo(name = "preu")
    var preu: Double,

    @ColumnInfo(name = "imatge_uri")
    var imatgeUri: String?,


    @ColumnInfo(name = "es_oferta")
    var esOferta: Boolean = false,

    @ColumnInfo(name = "preu_oferta")
    var preuOferta: Double = 0.0,


    @ColumnInfo(name = "id_creador")
    val idCreador: Int
) {

    val preuFinal: Double
        get() = if (esOferta && preuOferta > 0.0) preuOferta else preu
}