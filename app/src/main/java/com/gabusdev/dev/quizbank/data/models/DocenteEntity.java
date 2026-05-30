package com.gabusdev.dev.quizbank.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "docentes")
public class DocenteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nombre;
    public String correo;
    public String institucion;
    public long fechaRegistro;

    public DocenteEntity() {}

    @androidx.room.Ignore
    public DocenteEntity(String nombre, String correo, String institucion, long fechaRegistro) {
        this.nombre = nombre;
        this.correo = correo;
        this.institucion = institucion;
        this.fechaRegistro = fechaRegistro;
    }
}
