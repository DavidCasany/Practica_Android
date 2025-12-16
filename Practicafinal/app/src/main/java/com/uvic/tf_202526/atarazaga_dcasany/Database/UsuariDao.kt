package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Usuari

@Dao
interface UsuariDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUsuari(usuari: Usuari): Long


    @Query("SELECT * FROM usuaris_table WHERE nom_usuari = :nom AND password = :pass")
    suspend fun getLogin(nom: String, pass: String): Usuari?


    @Query("SELECT * FROM usuaris_table WHERE nom_usuari = :nom")
    suspend fun getUsuariByNom(nom: String): Usuari?

    @Query("SELECT * FROM usuaris_table WHERE uid = :id")
    suspend fun getUsuariById(id: Int): Usuari?

    @Query("UPDATE usuaris_table SET banner_uri = :uri WHERE uid = :id")
    suspend fun updateBanner(id: Int, uri: String)
}