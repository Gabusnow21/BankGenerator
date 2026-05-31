package com.gabusdev.dev.quizbank.core.export;

import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import java.util.List;

public class MarkdownExporter {
    /**
     * Exporta la lista de preguntas a un String en formato Markdown optimizado para NotebookLM.
     */
    public static String export(List<PreguntaEntity> preguntas) {
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
}
