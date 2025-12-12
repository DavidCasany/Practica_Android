package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Usuari

@Dao
interface UsuariDao {

    // 1. REGISTRE: Insereix un nou usuari.
    // Retorna un Long (la nova ID generada).
    // Si ja existeix (conflicte), l'ignora.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUsuari(usuari: Usuari): Long

    // 2. LOGIN: Busca un usuari que coincideixi amb nom i password.
    // Retorna l'Usuari sencer o null si no el troba.
    @Query("SELECT * FROM usuaris_table WHERE nom_usuari = :nom AND password = :pass")
    suspend fun getLogin(nom: String, pass: String): Usuari?

    // 3. VALIDACIÓ: Busca si ja existeix un usuari amb aquest nom (per no repetir).
    @Query("SELECT * FROM usuaris_table WHERE nom_usuari = :nom")
    suspend fun getUsuariByNom(nom: String): Usuari?
}