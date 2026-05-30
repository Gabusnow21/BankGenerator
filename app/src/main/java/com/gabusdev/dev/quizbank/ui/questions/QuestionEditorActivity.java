package com.gabusdev.dev.quizbank.ui.questions;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabusdev.dev.quizbank.data.database.AppDatabase;
import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import com.gabusdev.dev.quizbank.databinding.ActivityQuestionEditorBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestionEditorActivity extends AppCompatActivity {
    private ActivityQuestionEditorBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSaveQuestion.setOnClickListener(v -> saveQuestion());
        
        setupPreview();
    }

    private void setupPreview() {
        // En una fase posterior se inyectará KaTeX aquí
        binding.wvPreview.getSettings().setJavaScriptEnabled(true);
        binding.wvPreview.loadData("<html><body><i>La previsualización matemática aparecerá aquí...</i></body></html>", "text/html", "UTF-8");
    }

    private void saveQuestion() {
        String nivel = binding.etNivel.getText().toString().trim();
        String enunciado = binding.etEnunciado.getText().toString().trim();
        String respuesta = binding.etRespuestaCorrecta.getText().toString().trim();
        String justificacion = binding.etJustificacion.getText().toString().trim();

        if (enunciado.isEmpty() || respuesta.isEmpty()) {
            Toast.makeText(this, "Por favor llena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            PreguntaEntity pregunta = new PreguntaEntity(
                    enunciado,
                    "[]", // Opciones JSON vacío por ahora
                    respuesta,
                    justificacion,
                    nivel
            );
            AppDatabase.getInstance(this).preguntaDao().insertPregunta(pregunta);
            runOnUiThread(() -> {
                Toast.makeText(this, "Pregunta guardada", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
