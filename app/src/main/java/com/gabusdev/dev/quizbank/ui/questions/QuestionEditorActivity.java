package com.gabusdev.dev.quizbank.ui.questions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.gabusdev.dev.quizbank.R;
import com.gabusdev.dev.quizbank.data.database.AppDatabase;
import com.gabusdev.dev.quizbank.data.database.daos.PreguntaDao;
import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import com.gabusdev.dev.quizbank.databinding.ActivityQuestionEditorBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.text.Editable;
import android.text.TextWatcher;
import android.content.SharedPreferences;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.button.MaterialButton;

public class QuestionEditorActivity extends AppCompatActivity {
    public static final String EXTRA_QUESTION_ID = "extra_question_id";
    private static final String PREFS_NAME = "quizbank_prefs";
    private static final String PREF_SHOW_MACROS = "show_macros";
    private static final String DEFAULT_OPTIONS_JSON = "[]";

    private ActivityQuestionEditorBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isWebViewLoaded = false;
    private boolean isWebView2Loaded = false;
    private int questionId = -1;
    private int currentStep = 1;

    private final ActivityResultLauncher<Intent> ocrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String extractedText = result.getData().getStringExtra(OcrCaptureActivity.EXTRA_EXTRACTED_TEXT);
                    if (extractedText != null) {
                        insertTextAtCursor(extractedText);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        questionId = getIntent().getIntExtra(EXTRA_QUESTION_ID, -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> handleBackNavigation());

        binding.btnNext.setOnClickListener(v -> goToStep2());
        binding.btnSaveQuestion.setOnClickListener(v -> saveQuestion());
        
        setupPreview(binding.wvPreview, true);
        setupPreview(binding.wvPreview2, false);
        setupTextWatcher();
        setupMacrosAndOcr();
        
        if (questionId != -1) {
            loadQuestionData();
        }
    }

    private void setupMacrosAndOcr() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean showMacros = prefs.getBoolean(PREF_SHOW_MACROS, true);
        
        binding.switchMacros.setChecked(showMacros);
        binding.layoutMacros.getRoot().setVisibility(showMacros ? View.VISIBLE : View.GONE);

        binding.switchMacros.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.layoutMacros.getRoot().setVisibility(isChecked ? View.VISIBLE : View.GONE);
            prefs.edit().putBoolean(PREF_SHOW_MACROS, isChecked).apply();
        });

        binding.btnScanOcr.setOnClickListener(v -> {
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            ocrLauncher.launch(intent);
        });

        setupMacroButtons();
    }

    private void setupMacroButtons() {
        // Find buttons in the included layout
        View macroBar = binding.layoutMacros.getRoot();
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_frac));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_sqrt));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_pow));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_sub));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_pm));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_times));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_alpha));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_beta));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_sum));
        setupMacroButton(macroBar.findViewById(R.id.btn_macro_int));
    }

    private void setupMacroButton(View view) {
        if (view instanceof MaterialButton) {
            MaterialButton button = (MaterialButton) view;
            String macro = button.getText().toString();
            button.setOnClickListener(v -> insertTextAtCursor(macro));
        }
    }

    private void insertTextAtCursor(String text) {
        int start = Math.max(binding.etEnunciado.getSelectionStart(), 0);
        int end = Math.max(binding.etEnunciado.getSelectionEnd(), 0);
        binding.etEnunciado.getText().replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length());
        
        // Position cursor inside brackets if present
        if (text.contains("{}")) {
            binding.etEnunciado.setSelection(start + text.indexOf("{") + 1);
        }
    }

    private void handleBackNavigation() {
        if (currentStep == 2) {
            goToStep1();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep == 2) {
            goToStep1();
        } else {
            super.onBackPressed();
        }
    }

    private void goToStep1() {
        currentStep = 1;
        binding.layoutStep1.setVisibility(View.VISIBLE);
        binding.layoutStep2.setVisibility(View.GONE);
        binding.toolbar.setTitle(R.string.editor_step_1_title);
        binding.tvStepIndicator.setText(R.string.editor_step_1_of_2);
        binding.progressIndicator.setProgress(50);
    }

    private void goToStep2() {
        if (binding.etEnunciado.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.editor_error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        currentStep = 2;
        binding.layoutStep1.setVisibility(View.GONE);
        binding.layoutStep2.setVisibility(View.VISIBLE);
        binding.toolbar.setTitle(R.string.editor_step_2_title);
        binding.tvStepIndicator.setText(R.string.editor_step_2_of_2);
        binding.progressIndicator.setProgress(100);
        updateMathPreview(binding.etEnunciado.getText().toString());
    }

    private void loadQuestionData() {
        executorService.execute(() -> {
            PreguntaEntity pregunta = AppDatabase.getInstance(this).preguntaDao().getPreguntaById(questionId);
            if (pregunta != null) {
                runOnUiThread(() -> {
                    binding.etNivel.setText(pregunta.nivel);
                    binding.etEnunciado.setText(pregunta.enunciado);
                    binding.etRespuestaCorrecta.setText(pregunta.respuestaCorrecta);
                    binding.etJustificacion.setText(pregunta.justificacion);
                });
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupPreview(WebView webView, boolean isFirst) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        
        webView.setBackgroundColor(0); // Transparent

        String htmlTemplate = "<!DOCTYPE html><html><head>" +
                "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css'>" +
                "<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js'></script>" +
                "<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js'></script>" +
                "<style>body { font-size: 18px; color: white; background-color: transparent; padding: 10px; margin: 0; display: flex; align-items: center; justify-content: center; height: 100vh; font-family: sans-serif; }</style>" +
                "</head><body><div id='mathContent'></div>" +
                "<script>" +
                "function renderMath(text) {" +
                "  var element = document.getElementById('mathContent');" +
                "  element.innerHTML = text || '';" +
                "  renderMathInElement(document.body, {" +
                "    delimiters: [" +
                "      {left: '$$', right: '$$', display: true}," +
                "      {left: '$', right: '$', display: false}" +
                "    ]" +
                "  });" +
                "}" +
                "</script></body></html>";

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isFirst) {
                    isWebViewLoaded = true;
                } else {
                    isWebView2Loaded = true;
                }
                updateMathPreview(binding.etEnunciado.getText().toString());
            }
        });

        webView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlTemplate, "text/html", "UTF-8", null);
    }

    private void setupTextWatcher() {
        binding.etEnunciado.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateMathPreview(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateMathPreview(String text) {
        String escapedText = text.replace("'", "\\'").replace("\n", "<br>");
        if (isWebViewLoaded && binding.layoutStep1.getVisibility() == View.VISIBLE) {
            binding.wvPreview.evaluateJavascript("renderMath('" + escapedText + "')", null);
        }
        if (isWebView2Loaded && binding.layoutStep2.getVisibility() == View.VISIBLE) {
            binding.wvPreview2.evaluateJavascript("renderMath('" + escapedText + "')", null);
        }
    }

    private void saveQuestion() {
        String nivel = binding.etNivel.getText().toString().trim();
        String enunciado = binding.etEnunciado.getText().toString().trim();
        String respuesta = binding.etRespuestaCorrecta.getText().toString().trim();
        String justificacion = binding.etJustificacion.getText().toString().trim();

        if (enunciado.isEmpty() || respuesta.isEmpty()) {
            Toast.makeText(this, R.string.editor_error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            PreguntaDao dao = AppDatabase.getInstance(this).preguntaDao();
            if (questionId != -1) {
                PreguntaEntity pregunta = dao.getPreguntaById(questionId);
                if (pregunta != null) {
                    pregunta.nivel = nivel;
                    pregunta.enunciado = enunciado;
                    pregunta.respuestaCorrecta = respuesta;
                    pregunta.justificacion = justificacion;
                    dao.updatePregunta(pregunta);
                }
            } else {
                PreguntaEntity pregunta = new PreguntaEntity(
                        enunciado,
                        DEFAULT_OPTIONS_JSON,
                        respuesta,
                        justificacion,
                        nivel
                );
                dao.insertPregunta(pregunta);
            }
            
            runOnUiThread(() -> {
                Toast.makeText(this, questionId != -1 ? R.string.editor_success_updated : R.string.editor_success_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
