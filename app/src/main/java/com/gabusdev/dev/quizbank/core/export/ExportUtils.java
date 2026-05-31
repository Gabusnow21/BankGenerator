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
     * Genera un HTML completo con KaTeX para ser renderizado como PDF profesional.
     */
    public static String exportToHtml(List<PreguntaEntity> preguntas, com.gabusdev.dev.quizbank.data.models.DocenteEntity docente) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css'>");
        sb.append("<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js'></script>");
        sb.append("<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js'></script>");
        sb.append("<style>");
        sb.append("@page { size: A4; margin: 2cm; }");
        sb.append("body { font-family: 'Times New Roman', serif; padding: 0; color: #000; line-height: 1.5; font-size: 12pt; }");
        sb.append(".header-box { text-align: center; border: 2px solid #000; padding: 15px; margin-bottom: 30px; }");
        sb.append(".header-box h1 { margin: 0; font-size: 18pt; text-transform: uppercase; }");
        sb.append(".header-box p { margin: 5px 0; font-style: italic; }");
        sb.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 20px; border-bottom: 1px solid #000; padding-bottom: 5px; }");
        sb.append(".question { margin-bottom: 25px; page-break-inside: avoid; }");
        sb.append(".q-num { font-weight: bold; margin-right: 10px; float: left; }");
        sb.append(".q-content { overflow: hidden; }");
        sb.append(".q-level { font-size: 0.9em; font-weight: normal; color: #444; margin-bottom: 5px; display: block; }");
        sb.append(".footer { position: fixed; bottom: 0; width: 100%; text-align: right; font-size: 8pt; color: #777; border-top: 1px solid #ccc; padding-top: 5px; }");
        sb.append("</style></head><body>");
        
        // Encabezado del Examen
        sb.append("<div class='header-box'>");
        sb.append("<h1>").append(docente != null ? docente.institucion : "Examen de Matemáticas").append("</h1>");
        sb.append("<p>Cuestionario generado por QuizBank</p>");
        sb.append("</div>");

        sb.append("<div class='info-row'>");
        sb.append("<span><strong>Docente:</strong> ").append(docente != null ? docente.nombre : "____________________").append("</span>");
        sb.append("<span><strong>Fecha:</strong> ____________________</span>");
        sb.append("</div>");

        sb.append("<div class='info-row'>");
        sb.append("<span><strong>Estudiante:</strong> ________________________________________</span>");
        sb.append("</div>");
        
        // Lista de Preguntas
        for (int i = 0; i < preguntas.size(); i++) {
            PreguntaEntity p = preguntas.get(i);
            sb.append("<div class='question'>");
            sb.append("<span class='q-level'>[Nivel: ").append(p.nivel).append("]</span>");
            sb.append("<div class='q-num'>").append(i + 1).append(")</div>");
            sb.append("<div class='q-content'>").append(p.enunciado.replace("\n", "<br>")).append("</div>");
            sb.append("</div>");
        }
        
        sb.append("<div class='footer'>Hoja generada automáticamente por QuizBank App</div>");
        
        sb.append("<script>");
        sb.append("document.addEventListener('DOMContentLoaded', function() {");
        sb.append("  renderMathInElement(document.body, {");
        sb.append("    delimiters: [");
        sb.append("      {left: '$$', right: '$$', display: true},");
        sb.append("      {left: '$', right: '$', display: false}");
        sb.append("    ],");
        sb.append("    throwOnError: false");
        sb.append("  });");
        sb.append("});");
        sb.append("</script></body></html>");
        
        return sb.toString();
    }
}
