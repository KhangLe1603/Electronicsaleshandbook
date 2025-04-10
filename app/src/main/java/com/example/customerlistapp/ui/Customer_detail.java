package com.example.customerlistapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.customerlistapp.models.Customer;
import com.example.customerlistapp.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.R;

public class Customer_detail extends AppCompatActivity {
    private EditText etPhone, etEmail, etBirthday, etAddress, etGender;
    private Button btnSua, btnLuu, btnXoa;
    private CustomerViewModel viewModel;
    private Customer customer;
    private TextView tvCustomerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_detail);

        ImageButton btnBack = findViewById(R.id.imageButton);
        btnBack.setOnClickListener(v -> finish());

        tvCustomerName = findViewById(R.id.tvCustomerName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etBirthday = findViewById(R.id.etBirthday);
        etAddress = findViewById(R.id.etAddress);
        etGender = findViewById(R.id.etGender);
        btnSua = findViewById(R.id.btnSua);
        btnLuu = findViewById(R.id.btnLuu);
        btnXoa = findViewById(R.id.btnXoa);

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new CustomerViewModel(Customer_detail.this);
                    }
                }).get(CustomerViewModel.class);

        // Nhận Customer từ Intent
        customer = (Customer) getIntent().getSerializableExtra("CUSTOMER");
        if (customer != null) {
            displayCustomerDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnLuu.setEnabled(false);
        btnLuu.setAlpha(0.5f);
        btnSua.setEnabled(true);
        btnSua.setAlpha(1.0f);
        btnXoa.setEnabled(true);
        btnXoa.setAlpha(1.0f);
        setEditMode(false);

        btnSua.setOnClickListener(v -> {
            setEditMode(true);
            btnLuu.setEnabled(true);
            btnLuu.setAlpha(1.0f);
            btnSua.setEnabled(false);
            btnSua.setAlpha(0.5f);
            btnXoa.setEnabled(false);
            btnXoa.setAlpha(0.5f);
        });

        btnLuu.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String gender = etGender.getText().toString().trim();

            if (phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.updateCustomer(customer.getSheetRowIndex(), customer.getSurname(), customer.getFirstName(),
                    address, phone, email, birthday, gender);
            Toast.makeText(this, "Đang cập nhật khách hàng...", Toast.LENGTH_SHORT).show();
            setEditMode(false);
            btnLuu.setEnabled(false);
            btnLuu.setAlpha(0.5f);
            btnSua.setEnabled(true);
            btnSua.setAlpha(1.0f);
            btnXoa.setEnabled(true);
            btnXoa.setAlpha(1.0f);

            Intent intent = new Intent();
            intent.putExtra("REFRESH", true);
            setResult(RESULT_OK, intent);
            finish();
        });

        btnXoa.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa khách hàng này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        viewModel.deleteCustomer(customer.getSheetRowIndex());
                        Toast.makeText(this, "Đã xóa khách hàng", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("REFRESH", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        });
    }

    private void displayCustomerDetails() {
        tvCustomerName.setText(customer.getFullName());
        etPhone.setText(customer.getPhone());
        etEmail.setText(customer.getEmail());
        etBirthday.setText(customer.getBirthday());
        etAddress.setText(customer.getAddress());
        etGender.setText(customer.getGender());
    }

    private void setEditMode(boolean isEditable) {
        etPhone.setFocusable(isEditable);
        etPhone.setFocusableInTouchMode(isEditable);
        etPhone.setClickable(isEditable);

        etEmail.setFocusable(isEditable);
        etEmail.setFocusableInTouchMode(isEditable);
        etEmail.setClickable(isEditable);

        etBirthday.setFocusable(isEditable);
        etBirthday.setFocusableInTouchMode(isEditable);
        etBirthday.setClickable(isEditable);

        etAddress.setFocusable(isEditable);
        etAddress.setFocusableInTouchMode(isEditable);
        etAddress.setClickable(isEditable);

        etGender.setFocusable(isEditable);
        etGender.setFocusableInTouchMode(isEditable);
        etGender.setClickable(isEditable);
    }
}