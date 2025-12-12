package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "botigues_visitades_table")
data class BotigaVisitada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_espectador")
    val idEspectador: Int, // L'ID de l'usuari que està mirant l'app

    @ColumnInfo(name = "id_streamer")
    val idStreamer: Int,   // L'ID del creador de la botiga visitada

    @ColumnInfo(name = "data_visita")
    val dataVisita: Long = System.currentTimeMillis() // Opcional: per ordenar per "més recents"
)