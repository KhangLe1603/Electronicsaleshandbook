package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private TextView goToLoginText;
    private ImageView googleSignUpButton;
    private AuthViewModel viewModel;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.emailRegister);
        passwordEditText = findViewById(R.id.passwordRegister);
        registerButton = findViewById(R.id.btnRegister);
        goToLoginText = findViewById(R.id.txtGoToLogin);
        googleSignUpButton = findViewById(R.id.btnGoogleSignUp);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new AuthViewModel(RegisterActivity.this);
                    }
                }).get(AuthViewModel.class);

        // Observe authentication result
        viewModel.getAuthResult().observe(this, authResult -> {
            Toast.makeText(RegisterActivity.this, authResult.getMessage(), Toast.LENGTH_SHORT).show();
            if (authResult.isSuccess()) {
                Intent intent = new Intent(RegisterActivity.this, ProductsView.class);
                startActivity(intent);
                finish();
            }
        });

        // Observe current user
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("RegisterActivity", "User already logged in: " + user.getEmail());
                Intent intent = new Intent(RegisterActivity.this, ProductsView.class);
                startActivity(intent);
                finish();
            }
        });

        // Email registration
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.registerWithEmail(email, password);
        });

        // Navigate to Login
        goToLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Google Sign-In
        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);
                    viewModel.signInWithGoogle(account);
                } catch (ApiException e) {
                    Log.e("RegisterActivity", "Google sign-in failed: " + e.getStatusCode(), e);
                    Toast.makeText(this, "Đăng ký Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w("RegisterActivity", "Google sign-in cancelled");
            }
        });

        googleSignUpButton.setOnClickListener(v -> {
            Intent signInIntent = viewModel.getGoogleSignInClient().getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }
}