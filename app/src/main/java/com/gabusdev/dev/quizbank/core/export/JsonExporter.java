package com.gabusdev.dev.quizbank.core.export;

import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class JsonExporter {
    /**
     * Exporta la lista de preguntas a un String en formato JSON.
     */
    public static String export(List<PreguntaEntity> preguntas) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (PreguntaEntity p : preguntas) {
            JSONObject obj = new JSONObject();
            obj.put("id", p.id);
            obj.put("nivel", p.nivel);
            obj.put("enunciado", p.enunciado);
            obj.put("respuesta", p.respuestaCorrecta);
            obj.put("justificacion", p.justificacion);
            obj.put("fecha", p.fechaCreacion);
            jsonArray.put(obj);
        }
        return jsonArray.toString(4);
    }
}
