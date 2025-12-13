package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.CartItemDisplay
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro

@Dao
interface CarroDao {

    // Afegir producte nou
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemCarro)

    // Actualitzar quantitat (si ja existeix)
    @Update
    suspend fun updateItem(item: ItemCarro)

    // Buscar si ja tenim aquest producte al carro d'aquest usuari
    @Query("SELECT * FROM items_carro_table WHERE id_usuari = :userId AND id_producte = :prodId LIMIT 1")
    suspend fun getItemSpecific(userId: Int, prodId: Int): ItemCarro?

    // Obtenir tot el carro d'un usuari (per a la pantalla final, si la fessim)
    @Query("SELECT * FROM items_carro_table WHERE id_usuari = :userId")
    suspend fun getCarroByUser(userId: Int): List<ItemCarro>

    // Aquesta Query màgica uneix les dues taules
    @Query("SELECT " +
            "P.nom as nomProducte, " +
            "P.preu as preuOriginal, " +  // Ara mapegem a 'preuOriginal'
            "C.quantitat as quantitat, " +
            "C.id as idItemCarro, " +
            "P.es_oferta as esOferta, " +   // NOU: Agafem el booleà
            "P.preu_oferta as preuOferta " + // NOU: Agafem el preu rebaixat
            "FROM items_carro_table C " +
            "INNER JOIN productes_table P ON C.id_producte = P.pid " +
            "WHERE C.id_usuari = :userId")
    suspend fun getCartItemsComplets(userId: Int): List<CartItemDisplay>

    // Per buidar el carro al final
    @Query("DELETE FROM items_carro_table WHERE id_usuari = :userId")
    suspend fun buidarCarro(userId: Int)

    // NOU: Esborrar un item concret per la seva ID (la ID de la línia del carro, no del producte)
    @Query("DELETE FROM items_carro_table WHERE id = :idItemCarro")
    suspend fun deleteItemById(idItemCarro: Int)

    // NOU: Restar 1 a la quantitat
    @Query("UPDATE items_carro_table SET quantitat = quantitat - 1 WHERE id = :idItemCarro")
    suspend fun decrementQuantitat(idItemCarro: Int)
}