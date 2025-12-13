package com.uvic.tf_202526.atarazaga_dcasany.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.BotigaDisplay
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.BotigaVisitada

@Dao
interface BotigaVisitadaDao {

    // GUARDAR VISITA: Quan escanegem el QR
    // OnConflict IGNORE: Si ja l'havíem visitat, no fem res (no dupliquem)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addVisita(visita: BotigaVisitada)

    // Afegeix aquesta query
    @Query("SELECT U.uid as idStreamer, U.nom_usuari as nomStreamer, U.banner_uri as bannerUri " +
            "FROM botigues_visitades_table B " +
            "INNER JOIN usuaris_table U ON B.id_streamer = U.uid " +
            "WHERE B.id_espectador = :userId")
    suspend fun getBotiguesAmbDetall(userId: Int): List<BotigaDisplay>

    // LLISTAR: Per al RecyclerView de l'Espectador (Historial)
    @Query("SELECT * FROM botigues_visitades_table WHERE id_espectador = :idEspectador")
    suspend fun getVisitesByEspectador(idEspectador: Int): List<BotigaVisitada>

    @Query("SELECT EXISTS(SELECT * FROM botigues_visitades_table WHERE id_espectador = :userId AND id_streamer = :streamerId)")
    suspend fun jaLaTinc(userId: Int, streamerId: Int): Boolean
}