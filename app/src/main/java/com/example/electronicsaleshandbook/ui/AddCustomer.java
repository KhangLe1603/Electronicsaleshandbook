package com.example.electronicsaleshandbook.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.view.View;
import java.text.ParseException;

public class AddCustomer extends AppCompatActivity {
    private EditText etSurName, etFirstName, etPhone, etEmail, etBirthday, etAddress;
    private Button btnAdd, btnCancel;
    private ImageButton btnBack;
    private CustomerViewModel viewModel;
    private MaterialAutoCompleteTextView etGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        View addCustomerLayout = findViewById(R.id.addCustomer);

        ViewCompat.setOnApplyWindowInsetsListener(addCustomerLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

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

        String[] genderOptions = {"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        etGender.setAdapter(adapter);

        // Định dạng ngày sinh
        etBirthday.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final String separator = "/";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^0-9]", "");
                if (!input.equals(current)) {
                    String formatted = formatDate(input);
                    current = input;
                    etBirthday.removeTextChangedListener(this);
                    etBirthday.setText(formatted);
                    etBirthday.setSelection(formatted.length());
                    etBirthday.addTextChangedListener(this);
                }
            }

            private String formatDate(String input) {
                if (input.length() == 0) return "";
                StringBuilder formatted = new StringBuilder();
                int length = Math.min(input.length(), 8);
                for (int i = 0; i < length; i++) {
                    formatted.append(input.charAt(i));
                    if (i == 1 || i == 3) {
                        formatted.append(separator);
                    }
                }
                return formatted.toString();
            }
        });

        // Xử lý nút Thêm
        btnAdd.setOnClickListener(v -> {
            String surname = etSurName.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String gender = etGender.getText().toString().trim();

            // Validation
            if (!validateInputs(surname, firstName, phone, email, birthday, address)) {
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

    private boolean validateInputs(String surname, String firstName, String phone, String email, String birthday, String address) {
        // Validate surname and first name (only letters, spaces, and Vietnamese characters)
        String namePattern = "^[a-zA-ZÀ-ỹ\\s]+$";
        if (surname.isEmpty() || !surname.matches(namePattern)) {
            Toast.makeText(this, "Họ và đệm không hợp lệ (chỉ chứa chữ cái và khoảng trắng)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (firstName.isEmpty() || !firstName.matches(namePattern)) {
            Toast.makeText(this, "Tên không hợp lệ (chỉ chứa chữ cái và khoảng trắng)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate phone (Vietnamese mobile numbers: 10-11 digits, starting with 03, 05, 07, 08, 09)
        String phonePattern = "^(03|05|07|08|09)\\d{8,9}$";
        if (phone.isEmpty() || !phone.matches(phonePattern)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (phải bắt đầu bằng 03, 05, 07, 08, 09 và có 10-11 chữ số)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate email (optional, but if provided, must be valid)
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.isEmpty() && !email.matches(emailPattern)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate birthday (must be in dd/MM/yyyy format and a valid date)
        if (!birthday.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            try {
                java.util.Date date = sdf.parse(birthday);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar today = Calendar.getInstance();
                if (calendar.after(today)) {
                    Toast.makeText(this, "Ngày sinh không được ở tương lai", Toast.LENGTH_SHORT).show();
                    return false;
                }
                int year = calendar.get(Calendar.YEAR);
                if (year < 1900) {
                    Toast.makeText(this, "Năm sinh không hợp lệ (phải từ 1900 trở lên)", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Ngày sinh không hợp lệ (định dạng phải là dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Validate address
        if (address.isEmpty()) {
            Toast.makeText(this, "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}