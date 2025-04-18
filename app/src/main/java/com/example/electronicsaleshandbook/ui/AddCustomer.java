package com.example.electronicsaleshandbook.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

        etBirthday.setOnClickListener(v -> showDatePickerDialog());

        String[] genderOptions = {"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        etGender.setAdapter(adapter);

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

    private void showDatePickerDialog() {
        // Lấy ngày hiện tại làm mặc định
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Nếu etBirthday đã có giá trị, parse để đặt ngày mặc định
        String birthdayText = etBirthday.getText().toString().trim();
        if (!birthdayText.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdf.parse(birthdayText));
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            } catch (Exception e) {
                Log.e("Customer_detail", "Invalid birthday format: " + birthdayText, e);
            }
        }

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Định dạng ngày thành dd/MM/yyyy
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    etBirthday.setText(formattedDate);
                },
                year, month, day);

        // Giới hạn ngày tối đa là hôm nay
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}