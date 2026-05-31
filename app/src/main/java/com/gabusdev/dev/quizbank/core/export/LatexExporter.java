package com.gabusdev.dev.quizbank.core.export;

import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import java.util.List;

public class LatexExporter {
    /**
     * Exporta la lista de preguntas a un String en formato LaTeX (Artículo científico).
     */
    public static String export(List<PreguntaEntity> preguntas) {
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
        }
        sb.append("\\end{enumerate}\n\n");
        sb.append("\\end{document}");
        return sb.toString();
    }
}
