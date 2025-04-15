package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.view.View;

public class AccountActivity extends AppCompatActivity {
    private ImageView backIcon, profileImage;
    private TextView nameText, emailText;
    private Button logoutButton;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        backIcon = findViewById(R.id.backIcon);
        profileImage = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        logoutButton = findViewById(R.id.logoutButton);

        View accountLayout = findViewById(R.id.account);

        ViewCompat.setOnApplyWindowInsetsListener(accountLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                        return (T) new AuthViewModel(AccountActivity.this);
                    }
                }).get(AuthViewModel.class);

        // Quan sát thông tin người dùng
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Hiển thị thông tin
                nameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
                emailText.setText(user.getEmail());
                if (user.getPhotoUrl() != null) {
                    Picasso.get().load(user.getPhotoUrl()).into(profileImage);
                }
            } else {
                // Không có người dùng, quay về LoginActivity
                Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Nút quay lại
        backIcon.setOnClickListener(v -> finish());

        // Nút đăng xuất
        logoutButton.setOnClickListener(v -> {
            viewModel.signOut();
            Toast.makeText(AccountActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}