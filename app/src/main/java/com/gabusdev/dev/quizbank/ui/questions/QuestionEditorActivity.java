package com.gabusdev.dev.quizbank.ui.questions;

import android.os.Bundle;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class QuestionEditorActivity extends AppCompatActivity {
    public static final String EXTRA_QUESTION_ID = "extra_question_id";
    private ActivityQuestionEditorBinding binding;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isWebViewLoaded = false;
    private int questionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        questionId = getIntent().getIntExtra(EXTRA_QUESTION_ID, -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (questionId != -1) {
                getSupportActionBar().setTitle(R.string.editor_title_edit);
                binding.btnSaveQuestion.setText(R.string.editor_btn_update);
            } else {
                getSupportActionBar().setTitle(R.string.editor_title_new);
                binding.btnSaveQuestion.setText(R.string.editor_btn_save);
            }
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSaveQuestion.setOnClickListener(v -> saveQuestion());
        
        setupPreview();
        setupTextWatcher();
        
        if (questionId != -1) {
            loadQuestionData();
        }
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

    private void setupPreview() {
        WebSettings settings = binding.wvPreview.getSettings();
        settings.setJavaScriptEnabled(true);
        
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        
        String backgroundColor = isDarkMode ? "#1C1B1F" : "#FFFFFF";
        String textColor = isDarkMode ? "#E6E1E5" : "#1A237E";
        String placeholder = getString(R.string.editor_preview_placeholder);

        String htmlTemplate = "<!DOCTYPE html><html><head>" +
                "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css'>" +
                "<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js'></script>" +
                "<script src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js'></script>" +
                "<style>body { font-size: 16px; color: " + textColor + "; background-color: " + backgroundColor + "; padding: 5px; font-family: sans-serif; }</style>" +
                "</head><body><div id='mathContent'>" + placeholder + "</div>" +
                "<script>" +
                "function renderMath(text) {" +
                "  var element = document.getElementById('mathContent');" +
                "  element.innerHTML = text;" +
                "  renderMathInElement(document.body, {" +
                "    delimiters: [" +
                "      {left: '$$', right: '$$', display: true}," +
                "      {left: '$', right: '$', display: false}" +
                "    ]" +
                "  });" +
                "}" +
                "</script></body></html>";

        binding.wvPreview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                isWebViewLoaded = true;
                updateMathPreview(binding.etEnunciado.getText().toString());
            }
        });

        binding.wvPreview.loadDataWithBaseURL("https://cdn.jsdelivr.net/", htmlTemplate, "text/html", "UTF-8", null);
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
        if (isWebViewLoaded) {
            String escapedText = text.replace("'", "\\'").replace("\n", "<br>");
            binding.wvPreview.evaluateJavascript("renderMath('" + escapedText + "')", null);
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
