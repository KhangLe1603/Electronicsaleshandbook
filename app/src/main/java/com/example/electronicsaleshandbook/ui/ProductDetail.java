package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;

public class ProductDetail extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Xử lý nút Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Lấy dữ liệu sản phẩm từ Intent
        Product product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product != null) {
            EditText edtTenSanPham = findViewById(R.id.edtTenSanPham);
            EditText edtDonGia = findViewById(R.id.edtDonGia);
            EditText edtGiaBan = findViewById(R.id.edtGiaBan);
            EditText edtDonViTinh = findViewById(R.id.edtDonViTinh);
            EditText edtMoTa = findViewById(R.id.edtMoTa);

            edtTenSanPham.setText(product.getName());
            edtMoTa.setText(product.getDescription());
            edtDonGia.setText(product.getUnitPrice());
            edtGiaBan.setText(product.getPrice());
            edtDonViTinh.setText(product.getUnit());
        }
    }
}