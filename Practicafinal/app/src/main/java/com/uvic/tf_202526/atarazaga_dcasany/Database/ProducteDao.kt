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

    // CREAR: Afegeix un producte nou
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProducte(producte: Producte)

    // LLEGIR (Tots): Per debug o llistes generals
    @Query("SELECT * FROM productes_table")
    suspend fun getAllProductes(): List<Producte>

    // LLEGIR (Per Creador): Aquesta és la clau!
    // Ens permet mostrar només els productes de la botiga que estem visitant (o gestionant)
    @Query("SELECT * FROM productes_table WHERE id_creador = :idCreador")
    suspend fun getProductesByStreamer(idCreador: Int): List<Producte>

    // ACTUALITZAR: Modificar preu, nom, etc.
    @Update
    suspend fun updateProducte(producte: Producte)

    // ELIMINAR: Esborrar un producte
    @Delete
    suspend fun deleteProducte(producte: Producte)

    @Query("SELECT * FROM productes_table WHERE pid = :id LIMIT 1")
    suspend fun getProducteById(id: Int): Producte?
}