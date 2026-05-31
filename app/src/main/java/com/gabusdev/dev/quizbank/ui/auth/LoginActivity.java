package com.gabusdev.dev.quizbank.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.gabusdev.dev.quizbank.R;
import com.gabusdev.dev.quizbank.data.database.AppDatabase;
import com.gabusdev.dev.quizbank.data.models.DocenteEntity;
import com.gabusdev.dev.quizbank.data.repositories.AuthRepository;
import com.gabusdev.dev.quizbank.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private AuthRepository authRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository(this);

        binding.btnGoogleSignin.setOnClickListener(v -> {
            Intent signInIntent = authRepository.getGoogleSignInClient().getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.btnLocalLogin.setOnClickListener(v -> handleLocalLogin());

        checkExistingSession();
    }

    private void handleLocalLogin() {
        String nombre = binding.etNombre.getText().toString().trim();
        String institucion = binding.etInstitucion.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, R.string.login_error_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            DocenteEntity docente = new DocenteEntity(
                    nombre,
                    "local_user@quizbank.app",
                    institucion.isEmpty() ? getString(R.string.login_institucion_default) : institucion,
                    System.currentTimeMillis()
            );
            AppDatabase.getInstance(this).docenteDao().insertDocente(docente);
            runOnUiThread(this::navigateToDashboard);
        });
    }

    private void checkExistingSession() {
        executorService.execute(() -> {
            DocenteEntity docente = AppDatabase.getInstance(this).docenteDao().getDocente();
            if (docente != null) {
                runOnUiThread(this::navigateToDashboard);
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                saveDocenteLocally(account);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, R.string.login_error_google, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDocenteLocally(GoogleSignInAccount account) {
        executorService.execute(() -> {
            DocenteEntity docente = new DocenteEntity(
                    account.getDisplayName(),
                    account.getEmail(),
                    getString(R.string.login_institucion_google),
                    System.currentTimeMillis()
            );
            AppDatabase.getInstance(this).docenteDao().insertDocente(docente);
            runOnUiThread(this::navigateToDashboard);
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, com.gabusdev.dev.quizbank.ui.dashboard.DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
