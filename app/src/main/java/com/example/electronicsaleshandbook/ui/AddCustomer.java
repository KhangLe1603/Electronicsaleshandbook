package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.R;

public class AddCustomer extends AppCompatActivity {
    private EditText etSurName, etFirstName, etPhone, etEmail, etBirthday, etAddress, etGender;
    private Button btnAdd, btnCancel;
    private ImageButton btnBack;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        // Khởi tạo các view
        etSurName = findViewById(R.id.etSurName);
        etFirstName = findViewById(R.id.etFirstName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etBirthday = findViewById(R.id.etBirthday);
        etAddress = findViewById(R.id.etAddress);
        etGender = findViewById(R.id.etGender);
        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new CustomerViewModel(AddCustomer.this);
                    }
                }).get(CustomerViewModel.class);

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút Thêm
        btnAdd.setOnClickListener(v -> {
            String surname = etSurName.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String gender = etGender.getText().toString().trim();

            // Kiểm tra thông tin bắt buộc
            if (surname.isEmpty() || firstName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ họ, tên, số điện thoại và địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi ViewModel để thêm khách hàng
            viewModel.addCustomer(surname, firstName, address, phone, email, birthday, gender);
            Toast.makeText(this, "Đang thêm khách hàng...", Toast.LENGTH_SHORT).show();

            // Trả về kết quả để làm mới danh sách
            Intent intent = new Intent();
            intent.putExtra("REFRESH", true);
            setResult(RESULT_OK, intent);
            finish();
        });

        // Xử lý nút Huỷ
        btnCancel.setOnClickListener(v -> finish());
    }
}