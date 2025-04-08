package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;

public class AddProductActivity extends AppCompatActivity {
    private EditText edtTenSanPham, edtDonGia, edtGiaBan, edtDonViTinh, edtMoTa;
    private ProductViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Xử lý nút Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo các EditText
        edtTenSanPham = findViewById(R.id.edtTenSanPham);
        edtDonGia = findViewById(R.id.edtDonGia);
        edtGiaBan = findViewById(R.id.edtGiaBan);
        edtDonViTinh = findViewById(R.id.edtDonViTinh);
        edtMoTa = findViewById(R.id.edtMoTa);

        // Khởi tạo nút
        Button btnThem = findViewById(R.id.btnThem);
        Button btnHuy = findViewById(R.id.btnHuy);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ProductViewModel(AddProductActivity.this);
                    }
                }).get(ProductViewModel.class);

        // Xử lý nút Thêm
        btnThem.setOnClickListener(v -> {
            String name = edtTenSanPham.getText().toString().trim();
            String unitPriceRaw = edtDonGia.getText().toString().trim();
            String sellingPriceRaw = edtGiaBan.getText().toString().trim();
            String unit = edtDonViTinh.getText().toString().trim();
            String description = edtMoTa.getText().toString().trim();

            if (name.isEmpty() || unitPriceRaw.isEmpty() || sellingPriceRaw.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            } else {
                String unitPrice = unitPriceRaw.replaceAll("[^0-9]", "");
                String sellingPrice = sellingPriceRaw.replaceAll("[^0-9]", "");

                viewModel.addProduct(name, description, unitPrice, sellingPrice, unit);
                Toast.makeText(this, "Đang thêm sản phẩm...", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        // Xử lý nút Huỷ
        btnHuy.setOnClickListener(v -> finish());
    }

}