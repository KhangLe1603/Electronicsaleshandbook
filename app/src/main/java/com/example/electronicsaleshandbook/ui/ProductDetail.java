package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.electronicsaleshandbook.repository.SheetRepository;
import com.example.electronicsaleshandbook.viewmodel.CustomerProductLinkViewModel;
import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private SwipeRefreshLayout swipeRefreshLayout;
    private SheetRepository sheetRepository;
    private List<Customer> latestCustomers = null;
    private List<CustomerProductLink> latestLinks = null;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        View ProductDetailLayout = findViewById(R.id.productDetai_Layout);
        ViewCompat.setOnApplyWindowInsetsListener(ProductDetailLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        setupCurrencyFormatter(edtDonGia);
        setupCurrencyFormatter(edtGiaBan);

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
            sheetRepository = SheetRepository.getInstance(this);
        } catch (IOException | GeneralSecurityException e) {
            Log.e("ProductDetail", "Failed to initialize repositories", e);
            Toast.makeText(this, "Không thể kết nối đến dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cập nhật MediatorLiveData với đồng bộ hóa dữ liệu
        MediatorLiveData<List<Customer>> productCustomersLiveData = new MediatorLiveData<>();
        LiveData<List<Customer>> customersLiveData = customerRepository.getCustomers();
        LiveData<List<CustomerProductLink>> linksLiveData = linkViewModel.getLinks();

        productCustomersLiveData.addSource(customersLiveData, customers -> {
            Log.d("ProductDetail", "Customers updated, size: " + (customers != null ? customers.size() : 0));
            latestCustomers = customers;
            if (latestCustomers != null && latestLinks != null) {
                combineData(productCustomersLiveData, latestCustomers, latestLinks);
            }
        });
        productCustomersLiveData.addSource(linksLiveData, links -> {
            Log.d("ProductDetail", "Links updated, size: " + (links != null ? links.size() : 0));
            latestLinks = links;
            if (latestCustomers != null && latestLinks != null) {
                combineData(productCustomersLiveData, latestCustomers, latestLinks);
            }
        });

        productCustomersLiveData.observe(this, productCustomers -> {
            Log.d("ProductDetail", "Customers using product " + product.getId() + ": " + (productCustomers != null ? productCustomers.size() : 0));
            customerAdapter.setCustomers(productCustomers != null ? productCustomers : new ArrayList<>());
            swipeRefreshLayout.setRefreshing(false);
            if (productCustomers == null || productCustomers.isEmpty()) {
                Toast.makeText(this, "Sản phẩm này chưa có khách hàng sử dụng", Toast.LENGTH_SHORT).show();
            }
        });

        try {
            sheetRepository = SheetRepository.getInstance(this);
        } catch (IOException | GeneralSecurityException e) {
            Log.e("ProductDetail", "Failed to initialize SheetRepository", e);
            Toast.makeText(this, "Lỗi tải dữ liệu liên kết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Xử lý kéo để làm mới
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("ProductDetail", "Refreshing customer list for product " + product.getId());
            try {
                if (customerRepository != null) {
                    customerRepository.invalidateCache();
                    customerRepository.refreshCustomers();
                } else {
                    Log.w("ProductDetail", "CustomerRepository is null, skipping refresh");
                    Toast.makeText(this, "Không thể làm mới khách hàng", Toast.LENGTH_SHORT).show();
                }
                if (sheetRepository != null) {
                    sheetRepository.invalidateCache();
                    linkViewModel.refreshLinks();
                } else {
                    Log.w("ProductDetail", "SheetRepository is null, skipping refresh");
                    Toast.makeText(this, "Không thể làm mới liên kết", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("ProductDetail", "Refresh failed", e);
                Toast.makeText(this, "Lỗi làm mới dữ liệu", Toast.LENGTH_SHORT).show();
            } finally {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        btnLuu.setEnabled(false);
        btnLuu.setBackgroundColor(getColor(android.R.color.darker_gray));
        btnLuu.setAlpha(1.0f);
        btnSua.setEnabled(true);
        btnSua.setAlpha(1.0f);
        btnXoa.setEnabled(true);
        btnXoa.setAlpha(1.0f);
        setEditMode(false);

        btnSua.setOnClickListener(v -> {
            setEditMode(true);
            btnLuu.setEnabled(true);
            btnLuu.setBackgroundColor(Color.parseColor("#4CAF50"));
            btnSua.setEnabled(false);
            btnSua.setAlpha(0.5f);
            btnXoa.setText("Hủy");
            btnXoa.setEnabled(true);
            btnXoa.setAlpha(1.0f);
            isEditing = true;
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
//            btnLuu.setEnabled(false);
//            btnLuu.setAlpha(0.5f);
//            btnSua.setEnabled(true);
//            btnSua.setAlpha(1.0f);
//            btnXoa.setEnabled(true);
//            btnXoa.setAlpha(1.0f);
            Intent intent = new Intent();
            intent.putExtra("REFRESH", true);
            setResult(RESULT_OK, intent);
            finish();
        });

        btnXoa.setOnClickListener(v -> {
            if (isEditing) {
                setEditMode(false);
                btnLuu.setEnabled(false);
                btnLuu.setBackgroundColor(getColor(android.R.color.darker_gray));
                btnLuu.setAlpha(1.0f);
                btnSua.setEnabled(true);
                btnSua.setAlpha(1.0f);
                btnXoa.setText("Xóa");
                btnXoa.setEnabled(true);
                btnXoa.setAlpha(1.0f);
                isEditing = false;
                displayProductDetails();
                return;
            }

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
        Log.d("ProductDetail", "Combining data: customers=" + (customers != null ? customers.size() : "null") +
                ", links=" + (links != null ? links.size() : "null"));
        List<Customer> productCustomers = new ArrayList<>();
        if (customers == null || links == null) {
            Log.w("ProductDetail", "Customers or links are null, cannot combine data");
            productCustomersLiveData.setValue(productCustomers);
            Toast.makeText(this, "Không thể tải dữ liệu khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        for (CustomerProductLink link : links) {
            Log.d("ProductDetail", "Checking link: customerId=" + link.getCustomerId() + ", productId=" + link.getProductId());
            if (link.getProductId().equals(product.getId())) {
                Customer customer = getCustomerById(link.getCustomerId(), customers);
                if (customer != null) {
                    productCustomers.add(customer);
                    Log.d("ProductDetail", "Added customer: " + customer.getId());
                } else {
                    Log.w("ProductDetail", "Customer not found for customerId=" + link.getCustomerId());
                }
            }
        }
        Log.d("ProductDetail", "Combined customers for product " + product.getId() + ": " + productCustomers.size());
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