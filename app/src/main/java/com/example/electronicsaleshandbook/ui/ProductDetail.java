package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;

public class ProductDetail extends AppCompatActivity {
    private EditText edtTenSanPham, edtDonGia, edtGiaBan, edtDonViTinh, edtMoTa;
    private Button btnSua, btnLuu, btnXoa;
    private ProductViewModel viewModel;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        edtTenSanPham = findViewById(R.id.edtTenSanPham);
        edtDonGia = findViewById(R.id.edtDonGia);
        edtGiaBan = findViewById(R.id.edtGiaBan);
        edtDonViTinh = findViewById(R.id.edtDonViTinh);
        edtMoTa = findViewById(R.id.edtMoTa);
        btnSua = findViewById(R.id.btnSua);
        btnLuu = findViewById(R.id.btnLuu);
        btnXoa = findViewById(R.id.btnXoa);

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ProductViewModel(ProductDetail.this);
                    }
                }).get(ProductViewModel.class);

        product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product != null) {
            displayProductDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSua.setOnClickListener(v -> {
            setEditMode(true);
            btnLuu.setEnabled(true);
            btnSua.setEnabled(false);
            btnXoa.setEnabled(false);
        });

        btnLuu.setOnClickListener(v -> {
            String name = edtTenSanPham.getText().toString().trim();
            String unitPrice = edtDonGia.getText().toString().trim().replaceAll("[^0-9]", "");
            String sellingPrice = edtGiaBan.getText().toString().trim().replaceAll("[^0-9]", "");
            String unit = edtDonViTinh.getText().toString().trim();
            String description = edtMoTa.getText().toString().trim();

            if (name.isEmpty() || unitPrice.isEmpty() || sellingPrice.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.updateProduct(product.getSheetRowIndex(), name, description, unitPrice, sellingPrice, unit);
            Toast.makeText(this, "Đang cập nhật sản phẩm...", Toast.LENGTH_SHORT).show();
            setEditMode(false);
            btnLuu.setEnabled(false);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
            setResult(RESULT_OK);
            finish();
        });

        btnXoa.setOnClickListener(v -> {
            viewModel.deleteProduct(product.getSheetRowIndex());
            Toast.makeText(this, "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void displayProductDetails() {
        edtTenSanPham.setText(product.getName());
        edtDonGia.setText(product.getUnitPrice());
        edtGiaBan.setText(product.getPrice());
        edtDonViTinh.setText(product.getUnit());
        edtMoTa.setText(product.getDescription());
    }

    private void setEditMode(boolean isEditable) {
        edtTenSanPham.setFocusable(isEditable);
        edtTenSanPham.setFocusableInTouchMode(isEditable);
        edtTenSanPham.setClickable(isEditable);

        edtDonGia.setFocusable(isEditable);
        edtDonGia.setFocusableInTouchMode(isEditable);
        edtDonGia.setClickable(isEditable);

        edtGiaBan.setFocusable(isEditable);
        edtGiaBan.setFocusableInTouchMode(isEditable);
        edtGiaBan.setClickable(isEditable);

        edtDonViTinh.setFocusable(isEditable);
        edtDonViTinh.setFocusableInTouchMode(isEditable);
        edtDonViTinh.setClickable(isEditable);

        edtMoTa.setFocusable(isEditable);
        edtMoTa.setFocusableInTouchMode(isEditable);
        edtMoTa.setClickable(isEditable);
    }
}