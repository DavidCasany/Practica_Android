package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update // Caldria si s'actualitza l'entitat sencera
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.CartItemDisplay
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro

@Dao
interface CarroDao {

    // 1. AFEGIR/INSERTAR ÍTEM
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemCarro)

    // 2. BUSCAR ÍTEM ESPECÍFIC (Usat per ProductDetailActivity per augmentar quantitat)
    @Query("SELECT * FROM items_carro_table WHERE id_usuari = :userId AND id_producte = :prodId LIMIT 1")
    suspend fun getItemSpecific(userId: Int, prodId: Int): ItemCarro?

    // 3. ACTUALITZAR QUANTITAT (usarem l'Update directe amb l'entitat, o la query)
    // Utilitzem aquesta Query per l'increment/decrement
    @Query("UPDATE items_carro_table SET quantitat = :novaQuantitat WHERE id = :itemId")
    suspend fun updateQuantitat(itemId: Int, novaQuantitat: Int) // Ja no dona error

    // 4. LLISTAR EL CARRO COMPLET (CORRECCIÓ CLAU DEL MAPEIG)
    @Query("SELECT " +
            "P.nom AS nomProducte, " +
            "P.preu AS preuOriginal, " +
            "C.quantitat, " +               // El nom de la columna "quantitat" ja coincideix
            "C.id AS idItemCarro, " +       // Havia de ser 'id' AS 'idItemCarro'
            "P.es_oferta AS esOferta, " +
            "P.preu_oferta AS preuOferta " +
            "FROM items_carro_table C " +
            "INNER JOIN productes_table P ON C.id_producte = P.pid " +
            "WHERE C.id_usuari = :userId")
    suspend fun getCartItemsComplets(userId: Int): List<CartItemDisplay>

    // 5. BUIDAR/ELIMINAR
    @Query("DELETE FROM items_carro_table WHERE id_usuari = :userId")
    suspend fun buidarCarro(userId: Int)

    @Query("DELETE FROM items_carro_table WHERE id = :idItemCarro")
    suspend fun deleteItemById(idItemCarro: Int)

    @Query("UPDATE items_carro_table SET quantitat = quantitat - 1 WHERE id = :idItemCarro")
    suspend fun decrementQuantitat(idItemCarro: Int)
}