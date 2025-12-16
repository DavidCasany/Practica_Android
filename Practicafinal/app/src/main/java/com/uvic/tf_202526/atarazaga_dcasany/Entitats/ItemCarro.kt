package com.uvic.tf_202526.atarazaga_dcasany.Entitats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carro_table")
data class ItemCarro(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "id_usuari")
    val idUsuari: Int,

    @ColumnInfo(name = "id_producte")
    val idProducte: Int,

    @ColumnInfo(name = "quantitat")
    var quantitat: Int = 1
)