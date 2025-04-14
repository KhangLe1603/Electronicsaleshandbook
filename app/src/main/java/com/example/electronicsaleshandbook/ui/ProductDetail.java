package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.model.Customer;
import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.CustomerRepository;
import com.example.electronicsaleshandbook.viewmodel.CustomerProductLinkViewModel;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ProductDetail extends AppCompatActivity {
    private EditText edtTenSanPham, edtDonGia, edtGiaBan, edtDonViTinh, edtMoTa, editId;
    private Button btnSua, btnLuu, btnXoa;
    private ProductViewModel viewModel;
    private Product product;
    private RecyclerView customersRecyclerView;
    private CustomerDetailAdapter customerAdapter;
    private CustomerProductLinkViewModel linkViewModel;
    private CustomerRepository customerRepository;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        customersRecyclerView = findViewById(R.id.customers_recycler_view);
        editId = findViewById(R.id.edtMaSanPham);
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
        if (product == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayProductDetails();

        customerAdapter = new CustomerDetailAdapter();
        customersRecyclerView.setAdapter(customerAdapter);
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            customerRepository = CustomerRepository.getInstance(this);
            linkViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    try {
                        return (T) new CustomerProductLinkViewModel(ProductDetail.this);
                    } catch (IOException | GeneralSecurityException e) {
                        Log.e("ProductDetail", "Failed to create CustomerProductLinkViewModel", e);
                        Toast.makeText(ProductDetail.this, "Lỗi tải dữ liệu liên kết", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }).get(CustomerProductLinkViewModel.class);
        } catch (IOException | GeneralSecurityException e) {
            Log.e("ProductDetail", "Failed to initialize CustomerRepository", e);
            Toast.makeText(this, "Không thể kết nối đến dữ liệu khách hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MediatorLiveData<List<Customer>> productCustomersLiveData = new MediatorLiveData<>();
        LiveData<List<Customer>> customersLiveData = customerRepository.getCustomers();
        LiveData<List<CustomerProductLink>> linksLiveData = linkViewModel.getLinks();

        productCustomersLiveData.addSource(customersLiveData, customers -> combineData(productCustomersLiveData, customers, linksLiveData.getValue()));
        productCustomersLiveData.addSource(linksLiveData, links -> combineData(productCustomersLiveData, customersLiveData.getValue(), links));

        productCustomersLiveData.observe(this, productCustomers -> {
            Log.d("ProductDetail", "Customers using product " + product.getId() + ": " + productCustomers.size());
            customerAdapter.setCustomers(productCustomers);
            if (productCustomers.isEmpty()) {
                Toast.makeText(this, "Sản phẩm này chưa có khách hàng sử dụng", Toast.LENGTH_SHORT).show();
            }
        });

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
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        viewModel.deleteProduct(product.getSheetRowIndex());
                        Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();

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

    private void displayProductDetails() {
        editId.setText(product.getId());
        editId.setEnabled(false);
        edtTenSanPham.setText(product.getName());
        String unitPriceRaw = product.getUnitPrice() != null ? product.getUnitPrice().trim().replaceAll("[^0-9]", "") : "";
        String sellingPriceRaw = product.getPrice() != null ? product.getPrice().trim().replaceAll("[^0-9]", "") : "";
        try {
            if (!unitPriceRaw.isEmpty()) {
                String formattedUnitPrice = decimalFormat.format(Long.parseLong(unitPriceRaw));
                edtDonGia.setText(formattedUnitPrice);
            } else {
                edtDonGia.setText("0");
            }
            if (!sellingPriceRaw.isEmpty()) {
                String formattedSellingPrice = decimalFormat.format(Long.parseLong(sellingPriceRaw));
                edtGiaBan.setText(formattedSellingPrice);
            } else {
                edtGiaBan.setText("0");
            }
        } catch (NumberFormatException e) {
            Log.e("ProductDetail", "Invalid price format: " + e.getMessage());
            edtDonGia.setText(unitPriceRaw.isEmpty() ? "0" : unitPriceRaw);
            edtGiaBan.setText(sellingPriceRaw.isEmpty() ? "0" : sellingPriceRaw);
        }
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

    private void combineData(MediatorLiveData<List<Customer>> productCustomersLiveData,
                             List<Customer> customers, List<CustomerProductLink> links) {
        if (customers == null || links == null) {
            return;
        }
        List<Customer> productCustomers = new ArrayList<>();
        for (CustomerProductLink link : links) {
            if (link.getProductId().equals(product.getId())) {
                Customer customer = getCustomerById(link.getCustomerId(), customers);
                if (customer != null) {
                    productCustomers.add(customer);
                }
            }
        }
        productCustomersLiveData.setValue(productCustomers);
    }

    private Customer getCustomerById(String customerId, List<Customer> customers) {
        for (Customer customer : customers) {
            if (customer.getId().equals(customerId)) {
                return customer;
            }
        }
        return null;
    }
}