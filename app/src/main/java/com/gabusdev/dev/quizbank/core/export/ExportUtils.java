package com.gabusdev.dev.quizbank.core.export;

import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import org.json.JSONException;
import java.util.List;

public class ExportUtils {

    public static String exportToJson(List<PreguntaEntity> preguntas) throws JSONException {
        return JsonExporter.export(preguntas);
    }

    public static String exportToMarkdown(List<PreguntaEntity> preguntas) {
        return MarkdownExporter.export(preguntas);
    }

    public static String exportToLatex(List<PreguntaEntity> preguntas) {
        return LatexExporter.export(preguntas);
    }

    /**
     * Genera un HTML completo con MathJax para ser renderizado como PDF.
     */
    public static String exportToHtml(List<PreguntaEntity> preguntas) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css'>");
        sb.append("<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js'></script>");
        sb.append("<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js'></script>");
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 40px; color: #333; line-height: 1.6; }");
        sb.append("h1 { color: #1A237E; text-align: center; border-bottom: 2px solid #1A237E; padding-bottom: 10px; }");
        sb.append(".question { margin-bottom: 30px; page-break-inside: avoid; }");
        sb.append(".header { font-weight: bold; color: #1A237E; margin-bottom: 10px; }");
        sb.append(".meta { font-size: 0.8em; color: #666; margin-bottom: 5px; }");
        sb.append(".footer { margin-top: 50px; text-align: center; font-size: 0.8em; color: #999; }");
        sb.append("</style></head><body>");
        
        sb.append("<h1>Cuestionario Matemático</h1>");
        
        for (int i = 0; i < preguntas.size(); i++) {
            PreguntaEntity p = preguntas.get(i);
            sb.append("<div class='question'>");
            sb.append("<div class='meta'>Nivel: ").append(p.nivel).append("</div>");
            sb.append("<div class='header'>Pregunta ").append(i + 1).append("</div>");
            sb.append("<div class='enunciado'>").append(p.enunciado.replace("\n", "<br>")).append("</div>");
            sb.append("</div>");
        }
        
        sb.append("<div class='footer'>Generado por QuizBank - Herramienta para Docentes</div>");
        
        sb.append("<script>");
        sb.append("document.addEventListener('DOMContentLoaded', function() {");
        sb.append("  renderMathInElement(document.body, {");
        sb.append("    delimiters: [");
        sb.append("      {left: '$$', right: '$$', display: true},");
        sb.append("      {left: '$', right: '$', display: false}");
        sb.append("    ]");
        sb.append("  });");
        sb.append("});");
        sb.append("</script></body></html>");
        
        return sb.toString();
    }
}
