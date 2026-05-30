package com.gabusdev.dev.quizbank.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

        checkExistingSession();
    }

    private void checkExistingSession() {
        executorService.execute(() -> {
            DocenteEntity docente = AppDatabase.getInstance(this).docenteDao().getDocente();
            if (docente != null) {
                navigateToDashboard();
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
            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDocenteLocally(GoogleSignInAccount account) {
        executorService.execute(() -> {
            DocenteEntity docente = new DocenteEntity(
                    account.getDisplayName(),
                    account.getEmail(),
                    "Institución no especificada",
                    System.currentTimeMillis()
            );
            AppDatabase.getInstance(this).docenteDao().insertDocente(docente);
            runOnUiThread(this::navigateToDashboard);
        });
    }

    private void navigateToDashboard() {
        // TODO: Intent to DashboardActivity
        Toast.makeText(this, "Bienvenido, " + (GoogleSignIn.getLastSignedInAccount(this) != null ? GoogleSignIn.getLastSignedInAccount(this).getDisplayName() : ""), Toast.LENGTH_SHORT).show();
    }
}
