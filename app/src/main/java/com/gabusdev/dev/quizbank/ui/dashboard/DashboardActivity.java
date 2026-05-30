package com.gabusdev.dev.quizbank.ui.dashboard;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.gabusdev.dev.quizbank.R;
import com.gabusdev.dev.quizbank.data.database.AppDatabase;
import com.gabusdev.dev.quizbank.data.models.DocenteEntity;
import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import com.gabusdev.dev.quizbank.databinding.ActivityDashboardBinding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabusdev.dev.quizbank.ui.questions.PreguntaAdapter;
import com.gabusdev.dev.quizbank.ui.questions.QuestionEditorActivity;
import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;
import com.gabusdev.dev.quizbank.core.export.ExportUtils;
import com.gabusdev.dev.quizbank.core.utils.FileHelper;
import android.net.Uri;

public class DashboardActivity extends AppCompatActivity implements PreguntaAdapter.OnQuestionActionListener {
    private ActivityDashboardBinding binding;
    private PreguntaAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getLifecycle();
        loadTeacherInfo();
        
        binding.fabAddQuestion.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuestionEditorActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_export) {
            showExportDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExportDialog() {
        String[] options = {"JSON", "Markdown (.md)", "LaTeX (.tex)"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Seleccionar Formato de Exportación")
                .setItems(options, (dialog, which) -> {
                    exportQuestions(which);
                })
                .show();
    }

    private void exportQuestions(int type) {
        executorService.execute(() -> {
            List<PreguntaEntity> preguntas =
                    AppDatabase.getInstance(this).preguntaDao().getAllPreguntas();
            
            if (preguntas.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No hay preguntas para exportar", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                String content = "";
                String fileName = "";
                String mimeType = "text/plain";

                switch (type) {
                    case 0: // JSON
                        content = ExportUtils.exportToJson(preguntas);
                        fileName = "banco_preguntas.json";
                        mimeType = "application/json";
                        break;
                    case 1: // MD
                        content = ExportUtils.exportToMarkdown(preguntas);
                        fileName = "banco_preguntas.md";
                        break;
                    case 2: // LaTeX
                        content = ExportUtils.exportToLatex(preguntas);
                        fileName = "banco_preguntas.tex";
                        break;
                }

                Uri fileUri = FileHelper.saveAndGetUri(this, content, fileName);
                shareFile(fileUri, mimeType);

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al exportar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void shareFile(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir Banco de Preguntas"));
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
                if (adapter != null) {
                    adapter.setQuestions(preguntas);
                }
            });
        });
    }
}

