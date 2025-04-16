package com.example.electronicsaleshandbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.util.NetworkUtil;
import com.example.electronicsaleshandbook.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView goToRegisterText;
    private ImageView googleSignInButton;
    private AuthViewModel viewModel;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private BroadcastReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailLogin);
        passwordEditText = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.btnLogin);
        goToRegisterText = findViewById(R.id.txtGoToRegister);
        googleSignInButton = findViewById(R.id.btnGoogleSignIn);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new AuthViewModel(LoginActivity.this);
                    }
                }).get(AuthViewModel.class);

        // Kiểm tra kết nối mạng
        updateUIForNetworkStatus();

        // Thiết lập BroadcastReceiver để theo dõi mạng
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUIForNetworkStatus();
            }
        };
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);

        // Observe authentication result
        viewModel.getAuthResult().observe(this, authResult -> {
            Toast.makeText(LoginActivity.this, authResult.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Observe current user
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("LoginActivity", "User already logged in: " + user.getEmail());
                Intent intent = new Intent(LoginActivity.this, ProductsView.class);
                startActivity(intent);
                finish();
            }
        });

        // Email login
        loginButton.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(this)) {
                showNetworkErrorDialog();
                return;
            }
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.loginWithEmail(email, password);
        });

        // Navigate to Register
        goToRegisterText.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(this)) {
                showNetworkErrorDialog();
                return;
            }
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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
                    Log.e("LoginActivity", "Google sign-in failed: " + e.getStatusCode(), e);
                    Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w("LoginActivity", "Google sign-in cancelled");
            }
        });

        googleSignInButton.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(this)) {
                showNetworkErrorDialog();
                return;
            }
            Intent signInIntent = viewModel.getGoogleSignInClient().getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    private void updateUIForNetworkStatus() {
        boolean isNetworkAvailable = NetworkUtil.isNetworkAvailable(this);
        Log.d("LoginActivity", "Network status: " + (isNetworkAvailable ? "Connected" : "Disconnected"));

        loginButton.setEnabled(isNetworkAvailable);
        googleSignInButton.setEnabled(isNetworkAvailable);
        goToRegisterText.setEnabled(isNetworkAvailable);

        if (!isNetworkAvailable) {
            showNetworkErrorDialog();
        }
    }

    private void showNetworkErrorDialog() {
        Log.w("LoginActivity", "No network connection detected");
        new AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối")
                .setMessage("Không có kết nối mạng. Vui lòng bật Wi-Fi hoặc dữ liệu di động và thử lại!")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}