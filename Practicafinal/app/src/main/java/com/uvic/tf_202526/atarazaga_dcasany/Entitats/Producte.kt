package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productes_table")
data class Producte(
    @PrimaryKey(autoGenerate = true)
    val pid: Int = 0,

    @ColumnInfo(name = "nom")
    var nom: String, // 'var' per si voleu editar-lo després

    @ColumnInfo(name = "descripcio")
    var descripcio: String,

    @ColumnInfo(name = "preu")
    var preu: Double,

    @ColumnInfo(name = "imatge_uri")
    var imatgeUri: String?,

    // --- NOUS CAMPS PER A OFERTES (Dev B) ---
    @ColumnInfo(name = "es_oferta")
    var esOferta: Boolean = false,

    @ColumnInfo(name = "preu_oferta")
    var preuOferta: Double = 0.0,

    // --- CAMP PER VINCULAR AMB STREAMER ---
    @ColumnInfo(name = "id_creador")
    val idCreador: Int
) {
    // Propietat calculada per saber què cobrar (per al Carro)
    val preuFinal: Double
        get() = if (esOferta && preuOferta > 0.0) preuOferta else preu
}