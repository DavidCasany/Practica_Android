package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "botigues_visitades_table")
data class BotigaVisitada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_espectador")
    val idEspectador: Int,

    @ColumnInfo(name = "id_streamer")
    val idStreamer: Int,

    @ColumnInfo(name = "data_visita")
    val dataVisita: Long = System.currentTimeMillis()
)