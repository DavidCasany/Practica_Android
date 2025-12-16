package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.CartItemDisplay
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro

@Dao
interface CarroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemCarro)


    @Query("SELECT * FROM carro_table WHERE id_usuari = :userId AND id_producte = :prodId LIMIT 1")
    suspend fun getItemSpecific(userId: Int, prodId: Int): ItemCarro?


    @Query("UPDATE carro_table SET quantitat = :novaQuantitat WHERE id = :itemId")
    suspend fun updateQuantitat(itemId: Int, novaQuantitat: Int)


    @Query("SELECT " +
            "P.nom AS nomProducte, " +
            "P.preu AS preuOriginal, " +
            "C.quantitat, " +
            "C.id AS idItemCarro, " +
            "P.es_oferta AS esOferta, " +
            "P.preu_oferta AS preuOferta " +
            "FROM carro_table C " +
            "INNER JOIN productes_table P ON C.id_producte = P.pid " +
            "WHERE C.id_usuari = :userId")
    suspend fun getCartItemsComplets(userId: Int): List<CartItemDisplay>


    @Query("DELETE FROM carro_table WHERE id_usuari = :userId")
    suspend fun buidarCarro(userId: Int)

    @Query("DELETE FROM carro_table WHERE id = :idItemCarro")
    suspend fun deleteItemById(idItemCarro: Int)

    @Query("UPDATE carro_table SET quantitat = quantitat - 1 WHERE id = :idItemCarro")
    suspend fun decrementQuantitat(idItemCarro: Int)
}