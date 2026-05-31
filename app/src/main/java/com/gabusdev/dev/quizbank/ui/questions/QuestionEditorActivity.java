package com.gabusdev.dev.quizbank.ui.questions;

import android.annotation.SuppressLint;
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

public class QuestionEditorActivity extends AppCompatActivity {
    public static final String EXTRA_QUESTION_ID = "extra_question_id";
    private ActivityQuestionEditorBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isWebViewLoaded = false;
    private boolean isWebView2Loaded = false;
    private int questionId = -1;
    private int currentStep = 1;

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
        
        if (questionId != -1) {
            loadQuestionData();
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
                        "[]",
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
