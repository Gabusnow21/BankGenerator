package com.gabusdev.dev.quizbank.core.export;

import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class ExportUtils {

    /**
     * Exporta la lista de preguntas a un String en formato JSON.
     */
    public static String exportToJson(List<PreguntaEntity> preguntas) throws JSONException {
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
        return jsonArray.toString(4); // Indentación de 4 espacios
    }

    /**
     * Exporta la lista de preguntas a un String en formato Markdown optimizado para NotebookLM.
     */
    public static String exportToMarkdown(List<PreguntaEntity> preguntas) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Banco de Preguntas - QuizBank\n\n");
        for (int i = 0; i < preguntas.size(); i++) {
            PreguntaEntity p = preguntas.get(i);
            sb.append("## Pregunta ").append(i + 1).append(" (").append(p.nivel).append(")\n\n");
            sb.append("**Enunciado:**\n").append(p.enunciado).append("\n\n");
            sb.append("**Respuesta Correcta:** ").append(p.respuestaCorrecta).append("\n\n");
            if (p.justificacion != null && !p.justificacion.isEmpty()) {
                sb.append("> **Justificación para NotebookLM:**\n> ").append(p.justificacion).append("\n\n");
            }
            sb.append("---\n\n");
        }
        return sb.toString();
    }

    /**
     * Exporta la lista de preguntas a un String en formato LaTeX (Artículo científico).
     */
    public static String exportToLatex(List<PreguntaEntity> preguntas) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\documentclass{article}\n");
        sb.append("\\usepackage[utf8]{inputenc}\n");
        sb.append("\\usepackage{amsmath}\n\n");
        sb.append("\\title{Cuestionario Matemático - QuizBank}\n");
        sb.append("\\author{Generado por QuizBank App}\n");
        sb.append("\\date{\\today}\n\n");
        sb.append("\\begin{document}\n\n");
        sb.append("\\maketitle\n\n");
        sb.append("\\begin{enumerate}\n");
        for (PreguntaEntity p : preguntas) {
            sb.append("  \\item ").append(p.enunciado).append("\n");
            // Nota: En LaTeX real, se requiere escapar ciertos caracteres o asegurar que el LaTeX del docente sea compatible.
        }
        sb.append("\\end{enumerate}\n\n");
        sb.append("\\end{document}");
        return sb.toString();
    }
}
