package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte

@Dao
interface ProducteDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProducte(producte: Producte)


    @Query("SELECT * FROM productes_table")
    suspend fun getAllProductes(): List<Producte>



    @Query("SELECT * FROM productes_table WHERE id_creador = :idCreador")
    suspend fun getProductesByStreamer(idCreador: Int): List<Producte>


    @Update
    suspend fun updateProducte(producte: Producte)


    @Delete
    suspend fun deleteProducte(producte: Producte)

    @Query("SELECT * FROM productes_table WHERE pid = :id LIMIT 1")
    suspend fun getProducteById(id: Int): Producte?
}