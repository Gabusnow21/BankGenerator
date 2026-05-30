package com.gabusdev.dev.quizbank.data.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.gabusdev.dev.quizbank.data.models.DocenteEntity;

@Dao
public interface DocenteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDocente(DocenteEntity docente);

    @Query("SELECT * FROM docentes LIMIT 1")
    DocenteEntity getDocente();

    @Query("DELETE FROM docentes")
    void deleteDocente();
}
