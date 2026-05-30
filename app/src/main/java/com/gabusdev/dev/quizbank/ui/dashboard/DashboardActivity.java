package com.gabusdev.dev.quizbank.ui.dashboard;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabusdev.dev.quizbank.data.database.AppDatabase;
import com.gabusdev.dev.quizbank.data.models.DocenteEntity;
import com.gabusdev.dev.quizbank.databinding.ActivityDashboardBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadTeacherInfo();
        
        binding.fabAddQuestion.setOnClickListener(v -> {
            // TODO: Open Question Editor
            Toast.makeText(this, "Añadir pregunta próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadTeacherInfo() {
        executorService.execute(() -> {
            DocenteEntity docente = AppDatabase.getInstance(this).docenteDao().getDocente();
            int count = AppDatabase.getInstance(this).preguntaDao().getAllPreguntas().size();
            
            runOnUiThread(() -> {
                if (docente != null) {
                    binding.tvWelcome.setText("Bienvenido, " + docente.nombre);
                }
                binding.tvStats.setText("Tienes " + count + " preguntas en tu banco");
            });
        });
    }
}
