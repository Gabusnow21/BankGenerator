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

public class DashboardActivity extends AppCompatActivity {
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
        adapter = new PreguntaAdapter();
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvQuestions.setAdapter(adapter);
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
