package com.gabusdev.dev.quizbank.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "preguntas")
public class PreguntaEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String enunciado; // Soporta LaTeX ($...$ o $$...$$)
    public String opcionesJson; // List<String> convertido a JSON
    public String respuestaCorrecta;
    public String justificacion; // Clave para NotebookLM
    public String nivel; // Ej. "8vo Grado"
    public long fechaCreacion;

    public PreguntaEntity() {}

    @androidx.room.Ignore
    public PreguntaEntity(String enunciado, String opcionesJson, String respuestaCorrecta, String justificacion, String nivel) {
        this.enunciado = enunciado;
        this.opcionesJson = opcionesJson;
        this.respuestaCorrecta = respuestaCorrecta;
        this.justificacion = justificacion;
        this.nivel = nivel;
        this.fechaCreacion = System.currentTimeMillis();
    }
}
