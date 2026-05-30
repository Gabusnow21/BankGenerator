package com.gabusdev.dev.quizbank.ui.dashboard;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabusdev.dev.quizbank.data.database.AppDatabase;
import com.gabusdev.dev.quizbank.data.models.DocenteEntity;
import com.gabusdev.dev.quizbank.databinding.ActivityDashboardBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabusdev.dev.quizbank.ui.questions.PreguntaAdapter;
import com.gabusdev.dev.quizbank.ui.questions.QuestionEditorActivity;
import android.content.Intent;

public class DashboardActivity extends AppCompatActivity implements PreguntaAdapter.OnQuestionActionListener {
    private ActivityDashboardBinding binding;
    private PreguntaAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        loadTeacherInfo();
        
        binding.fabAddQuestion.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuestionEditorActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new PreguntaAdapter(this);
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvQuestions.setAdapter(adapter);
    }

    @Override
    public void onEdit(com.gabusdev.dev.quizbank.data.models.PreguntaEntity pregunta) {
        Intent intent = new Intent(this, QuestionEditorActivity.class);
        intent.putExtra(QuestionEditorActivity.EXTRA_QUESTION_ID, pregunta.id);
        startActivity(intent);
    }

    @Override
    public void onDelete(com.gabusdev.dev.quizbank.data.models.PreguntaEntity pregunta) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Pregunta")
                .setMessage("¿Estás seguro de que deseas eliminar esta pregunta?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    executorService.execute(() -> {
                        AppDatabase.getInstance(this).preguntaDao().deletePregunta(pregunta);
                        loadTeacherInfo(); // Recargar lista
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherInfo(); // Actualizar lista al volver
    }

    private void loadTeacherInfo() {
        executorService.execute(() -> {
            DocenteEntity docente = AppDatabase.getInstance(this).docenteDao().getDocente();
            java.util.List<com.gabusdev.dev.quizbank.data.models.PreguntaEntity> preguntas = 
                    AppDatabase.getInstance(this).preguntaDao().getAllPreguntas();
            
            runOnUiThread(() -> {
                if (docente != null) {
                    binding.tvWelcome.setText("Bienvenido, " + docente.nombre);
                }
                binding.tvStats.setText("Tienes " + preguntas.size() + " preguntas en tu banco");
                adapter.setQuestions(preguntas);
            });
        });
    }
}
