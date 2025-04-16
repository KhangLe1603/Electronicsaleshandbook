package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.repository.SheetRepository;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.view.View;

public class AddProductActivity extends AppCompatActivity {
    private EditText edtTenSanPham, edtDonGia, edtGiaBan, edtDonViTinh, edtMoTa;
    private ProductViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        View addProductLayout = findViewById(R.id.addProduct);

        ViewCompat.setOnApplyWindowInsetsListener(addProductLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

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

        setupCurrencyFormatter(edtDonGia);
        setupCurrencyFormatter(edtGiaBan);

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
                Intent intent = new Intent();
                intent.putExtra("REFRESH", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // Xử lý nút Huỷ
        btnHuy.setOnClickListener(v -> finish());


    }

    private String formatCurrency(String input) {
        if (input.isEmpty()) return "";

        try {
            long value = Long.parseLong(input.replaceAll("\\D", ""));
            return String.format("%,d", value).replace(",", ".");
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private void setupCurrencyFormatter(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("\\D", "");
                    String formatted = formatCurrency(cleanString);
                    current = formatted;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

}