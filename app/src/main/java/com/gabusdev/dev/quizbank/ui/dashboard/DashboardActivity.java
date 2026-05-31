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
        
        adapter = new PreguntaAdapter(this);
        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvQuestions.setAdapter(adapter);

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
        int id = item.getItemId();
        if (id == R.id.action_export) {
            showExportDialog();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.logout_btn_confirm, (dialog, which) -> logout())
                .setNegativeButton(R.string.logout_btn_cancel, null)
                .show();
    }

    private void logout() {
        executorService.execute(() -> {
            AppDatabase.getInstance(this).docenteDao().deleteDocente();
            runOnUiThread(() -> {
                Intent intent = new Intent(this, com.gabusdev.dev.quizbank.ui.auth.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    public void onSelectionChanged(int count) {
        if (adapter != null && count > 0) {
            binding.fabAddQuestion.setText("Exportar (" + count + ")");
            binding.fabAddQuestion.setIconResource(android.R.drawable.ic_menu_share);
        } else {
            binding.fabAddQuestion.setText(R.string.dashboard_new_question);
            binding.fabAddQuestion.setIconResource(android.R.drawable.ic_input_add);
        }
    }

    private void setupFilters(List<PreguntaEntity> preguntas) {
        binding.chipGroupFilters.removeAllViews();
        
        com.google.android.material.chip.Chip chipAll = new com.google.android.material.chip.Chip(this);
        chipAll.setText("Todas");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setOnClickListener(v -> adapter.filter(null));
        binding.chipGroupFilters.addView(chipAll);

        java.util.Set<String> niveles = new java.util.HashSet<>();
        for (PreguntaEntity p : preguntas) {
            if (p.nivel != null && !p.nivel.isEmpty()) niveles.add(p.nivel);
        }

        for (String nivel : niveles) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(nivel);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> adapter.filter(nivel));
            binding.chipGroupFilters.addView(chip);
        }
    }

    private void showExportDialog() {
        String[] options = {"Banco Completo", "Por Nivel/Grado", "Selección Manual"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¿Qué deseas exportar?")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showFormatDialog(null); break;
                        case 1: showLevelSelector(); break;
                        case 2: enterSelectionMode(); break;
                    }
                })
                .show();
    }

    private void enterSelectionMode() {
        adapter.setSelectionMode(true);
        Toast.makeText(this, "Toca las preguntas que deseas incluir", Toast.LENGTH_LONG).show();
        binding.fabAddQuestion.setOnClickListener(v -> {
            List<PreguntaEntity> seleccion = adapter.getSelectedQuestions();
            if (seleccion.isEmpty()) {
                exitSelectionMode();
            } else {
                showFormatDialog(seleccion);
            }
        });
    }

    private void exitSelectionMode() {
        adapter.setSelectionMode(false);
        binding.fabAddQuestion.setText(R.string.dashboard_new_question);
        binding.fabAddQuestion.setIconResource(android.R.drawable.ic_input_add);
        binding.fabAddQuestion.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuestionEditorActivity.class);
            startActivity(intent);
        });
    }

    private void showLevelSelector() {
        executorService.execute(() -> {
            List<PreguntaEntity> all = AppDatabase.getInstance(this).preguntaDao().getAllPreguntas();
            java.util.Set<String> nivelesSet = new java.util.HashSet<>();
            for (PreguntaEntity p : all) nivelesSet.add(p.nivel);
            String[] niveles = nivelesSet.toArray(new String[0]);

            runOnUiThread(() -> {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Seleccionar Nivel")
                        .setItems(niveles, (dialog, which) -> {
                            String selectedLevel = niveles[which];
                            filterAndExport(selectedLevel);
                        })
                        .show();
            });
        });
    }

    private void filterAndExport(String level) {
        executorService.execute(() -> {
            List<PreguntaEntity> all = AppDatabase.getInstance(this).preguntaDao().getAllPreguntas();
            List<PreguntaEntity> filtered = new java.util.ArrayList<>();
            for (PreguntaEntity p : all) {
                if (p.nivel.equals(level)) filtered.add(p);
            }
            runOnUiThread(() -> showFormatDialog(filtered));
        });
    }

    private void showFormatDialog(List<PreguntaEntity> specificList) {
        String[] formats = {"JSON", "Markdown (.md)", "LaTeX (.tex)", "PDF (.pdf)"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dashboard_export_title)
                .setItems(formats, (dialog, which) -> {
                    if (specificList != null) {
                        processExport(which, specificList);
                    } else {
                        exportQuestions(which);
                    }
                    if (adapter != null) exitSelectionMode();
                })
                .show();
    }

    private void processExport(int type, List<PreguntaEntity> preguntas) {
        if (type == 3) { // PDF
            createWebPrintJob(preguntas);
            return;
        }

        executorService.execute(() -> {
            try {
                String content = "";
                String fileName = "";
                String mimeType = "text/plain";

                switch (type) {
                    case 0: content = ExportUtils.exportToJson(preguntas); fileName = "seleccion.json"; mimeType = "application/json"; break;
                    case 1: content = ExportUtils.exportToMarkdown(preguntas); fileName = "seleccion.md"; break;
                    case 2: content = ExportUtils.exportToLatex(preguntas); fileName = "seleccion.tex"; break;
                }

                Uri fileUri = FileHelper.saveAndGetUri(this, content, fileName);
                String finalMimeType = mimeType;
                runOnUiThread(() -> shareFile(fileUri, finalMimeType));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void exportQuestions(int type) {
        executorService.execute(() -> {
            List<PreguntaEntity> preguntas =
                    AppDatabase.getInstance(this).preguntaDao().getAllPreguntas();
            
            if (preguntas.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, R.string.dashboard_no_questions_export, Toast.LENGTH_SHORT).show());
                return;
            }

            if (type == 3) { // PDF
                runOnUiThread(() -> createWebPrintJob(preguntas));
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
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.dashboard_export_error, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private android.webkit.WebView mWebView;
    private androidx.appcompat.app.AlertDialog mActiveDialog;

    private void createWebPrintJob(List<PreguntaEntity> preguntas) {
        // Mostrar diálogo de carga
        mActiveDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Generando PDF")
                .setMessage("Procesando fórmulas matemáticas... (Esto puede tardar unos segundos)")
                .setCancelable(false)
                .show();

        // Timeout de seguridad de 15 segundos
        final Runnable timeoutRunnable = () -> {
            if (mActiveDialog != null && mActiveDialog.isShowing()) {
                mActiveDialog.dismiss();
                Toast.makeText(this, "El proceso tardó demasiado. Revisa tu conexión a internet.", Toast.LENGTH_LONG).show();
                if (mWebView != null) {
                    binding.getRoot().removeView(mWebView);
                    mWebView = null;
                }
            }
        };
        binding.getRoot().postDelayed(timeoutRunnable, 15000);

        executorService.execute(() -> {
            try {
                DocenteEntity docente = AppDatabase.getInstance(this).docenteDao().getDocente();
                String htmlContent = ExportUtils.exportToHtml(preguntas, docente);
                
                runOnUiThread(() -> {
                    mWebView = new android.webkit.WebView(this);
                    // IMPORTANTE: Algunos dispositivos requieren que el WebView esté en la jerarquía para imprimir
                    mWebView.setVisibility(android.view.View.GONE);
                    binding.getRoot().addView(mWebView);
                    
                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.getSettings().setDomStorageEnabled(true);
                    
                    mWebView.setWebViewClient(new android.webkit.WebViewClient() {
                        @Override
                        public void onPageFinished(android.webkit.WebView view, String url) {
                            android.util.Log.d("DashboardActivity", "Página cargada. Renderizando...");
                            // Dar tiempo para KaTeX
                            view.postDelayed(() -> {
                                if (mActiveDialog != null && mActiveDialog.isShowing()) {
                                    binding.getRoot().removeCallbacks(timeoutRunnable);
                                    mActiveDialog.dismiss();
                                    printWebView(view);
                                    // No removemos el view inmediatamente para dejar que el PrintManager lo use
                                    view.postDelayed(() -> {
                                        binding.getRoot().removeView(view);
                                        mWebView = null;
                                    }, 5000);
                                }
                            }, 3000); // 3 segundos para asegurar renderizado completo
                        }

                        @Override
                        public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                            binding.getRoot().removeCallbacks(timeoutRunnable);
                            if (mActiveDialog != null) mActiveDialog.dismiss();
                            Toast.makeText(DashboardActivity.this, "Error de red: " + description, Toast.LENGTH_LONG).show();
                            binding.getRoot().removeView(view);
                            mWebView = null;
                        }
                    });

                    mWebView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlContent, "text/html", "UTF-8", null);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.getRoot().removeCallbacks(timeoutRunnable);
                    if (mActiveDialog != null) mActiveDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void printWebView(android.webkit.WebView webView) {
        android.print.PrintManager printManager = (android.print.PrintManager) getSystemService(android.content.Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " Document";
        
        // Usar el adaptador moderno
        android.print.PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        
        if (printManager != null) {
            printManager.print(jobName, printAdapter, new android.print.PrintAttributes.Builder().build());
        }
    }

    private void shareFile(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getString(R.string.dashboard_share_chooser)));
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
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.item_btn_delete, (dialog, which) -> {
                    executorService.execute(() -> {
                        AppDatabase.getInstance(this).preguntaDao().deletePregunta(pregunta);
                        loadTeacherInfo();
                    });
                })
                .setNegativeButton(R.string.logout_btn_cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherInfo();
    }

    private void loadTeacherInfo() {
        executorService.execute(() -> {
            DocenteEntity docente = AppDatabase.getInstance(this).docenteDao().getDocente();
            List<PreguntaEntity> preguntas = 
                    AppDatabase.getInstance(this).preguntaDao().getAllPreguntas();
            
            runOnUiThread(() -> {
                if (docente != null) {
                    binding.tvWelcome.setText(getString(R.string.dashboard_welcome, docente.nombre));
                }
                binding.tvStats.setText(getString(R.string.dashboard_stats, preguntas.size()));
                if (adapter != null) {
                    adapter.setQuestions(preguntas);
                    setupFilters(preguntas);
                }
            });
        });
    }
}
