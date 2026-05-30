package com.gabusdev.dev.quizbank.data.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import java.util.List;

@Dao
public interface PreguntaDao {
    @Insert
    long insertPregunta(PreguntaEntity pregunta);

    @Update
    void updatePregunta(PreguntaEntity pregunta);

    @Delete
    void deletePregunta(PreguntaEntity pregunta);

    @Query("SELECT * FROM preguntas ORDER BY fechaCreacion DESC")
    List<PreguntaEntity> getAllPreguntas();

    @Query("SELECT * FROM preguntas WHERE id = :id")
    PreguntaEntity getPreguntaById(int id);
}
