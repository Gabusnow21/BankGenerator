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

    private void showExportDialog() {
        String[] options = {"JSON", "Markdown (.md)", "LaTeX (.tex)", "PDF (.pdf)"};
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dashboard_export_title)
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
                }
            });
        });
    }
}
