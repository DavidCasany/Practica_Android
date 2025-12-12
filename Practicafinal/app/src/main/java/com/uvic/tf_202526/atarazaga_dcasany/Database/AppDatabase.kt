package com.uvic.tf_202526.atarazaga_dcasany.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.BotigaVisitada
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Producte
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.Usuari
import com.uvic.tf_202526.atarazaga_dcasany.Entitats.ItemCarro

// 1. Definim les taules (Entitats) i la versió de la BD
@Database(
    entities = [Usuari::class, Producte::class, BotigaVisitada::class, ItemCarro::class],
    version = 1, // Nota: Si ja has executat l'app, potser has de desinstal·lar-la perquè Room detecti el canvi de taules
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 2. Exposem els DAOs (perquè puguem fer servir les operacions)
    abstract fun usuariDao(): UsuariDao
    abstract fun producteDao(): ProducteDao
    abstract fun botigaVisitadaDao(): BotigaVisitadaDao

    abstract fun carroDao(): CarroDao

    // 3. Patró Singleton (per obrir la BD una sola vegada)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instància ja existeix, la retornem. Si no, la creem.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "merchstream_database" // Nom del fitxer físic al mòbil
                )
                    .fallbackToDestructiveMigration() // Opcional: Si canvies la BD, esborra l'antiga per evitar errors en dev
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}