package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.adapter.ProductDetailAdapter;
import com.example.electronicsaleshandbook.model.Customer;
import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.SheetRepository;
import com.example.electronicsaleshandbook.viewmodel.CustomerProductLinkViewModel;
import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class Customer_detail extends AppCompatActivity {
    private EditText etSurname, etFirstName, etPhone, etEmail, etBirthday, etAddress, etMaKH;
    private Button btnSua, btnLuu, btnXoa;
    private CustomerViewModel viewModel;
    private Customer customer;
    private RecyclerView productsRecyclerView;
    private ProductDetailAdapter productAdapter;
    private CustomerProductLinkViewModel linkViewModel;
    private SheetRepository productRepository;
    private MaterialAutoCompleteTextView etGender;
    private boolean isEditing = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Product> latestProducts = null;
    private List<CustomerProductLink> latestLinks = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_detail);

        View customerLayout = findViewById(R.id.customer_Detail);
        ViewCompat.setOnApplyWindowInsetsListener(customerLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.imageButton);
        btnBack.setOnClickListener(v -> finish());
        etMaKH = findViewById(R.id.etMaKH);
        etSurname = findViewById(R.id.etSurname);
        etFirstName = findViewById(R.id.etFirstName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etBirthday = findViewById(R.id.etBirthday);
        etAddress = findViewById(R.id.etAddress);
        etGender = findViewById(R.id.etGender);
        btnSua = findViewById(R.id.btnSua);
        btnLuu = findViewById(R.id.btnLuu);
        btnXoa = findViewById(R.id.btnXoa);
        productsRecyclerView = findViewById(R.id.products_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout1);

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new CustomerViewModel(Customer_detail.this);
                    }
                }).get(CustomerViewModel.class);

        String[] genderOptions = {"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        etGender.setAdapter(adapter);

        // Nhận Customer từ Intent
        customer = (Customer) getIntent().getSerializableExtra("CUSTOMER");
        if (customer != null) {
            displayCustomerDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        // Khởi tạo RecyclerView và Adapter
        productAdapter = new ProductDetailAdapter();
        productsRecyclerView.setAdapter(productAdapter);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            productRepository = SheetRepository.getInstance(this);
            linkViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    try {
                        return (T) new CustomerProductLinkViewModel(Customer_detail.this);
                    } catch (IOException | GeneralSecurityException e) {
                        Log.e("Customer_detail", "Failed to create CustomerProductLinkViewModel", e);
                        Toast.makeText(Customer_detail.this, "Lỗi tải dữ liệu liên kết", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }).get(CustomerProductLinkViewModel.class);
        } catch (IOException | GeneralSecurityException e) {
            Log.e("Customer_detail", "Failed to initialize repositories", e);
            Toast.makeText(this, "Không thể kết nối đến dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MediatorLiveData<List<Product>> customerProductsLiveData = new MediatorLiveData<>();
        LiveData<List<Product>> productsLiveData = productRepository.getProducts();
        LiveData<List<CustomerProductLink>> linksLiveData = linkViewModel.getLinks();

        customerProductsLiveData.addSource(productsLiveData, products -> {
            Log.d("Customer_detail", "Products updated, size: " + (products != null ? products.size() : 0));
            latestProducts = products;
            if (latestProducts != null && latestLinks != null) {
                combineData(customerProductsLiveData, latestProducts, latestLinks);
            }
        });
        customerProductsLiveData.addSource(linksLiveData, links -> {
            Log.d("Customer_detail", "Links updated, size: " + (links != null ? links.size() : 0));
            latestLinks = links;
            if (latestProducts != null && latestLinks != null) {
                combineData(customerProductsLiveData, latestProducts, latestLinks);
            }
        });

        customerProductsLiveData.observe(this, customerProducts -> {
            Log.d("Customer_detail", "Products used by customer " + customer.getId() + ": " + (customerProducts != null ? customerProducts.size() : 0));
            productAdapter.setProducts(customerProducts != null ? customerProducts : new ArrayList<>());
            swipeRefreshLayout.setRefreshing(false);
            if (customerProducts == null || customerProducts.isEmpty()) {
                Toast.makeText(this, "Khách hàng chưa sử dụng sản phẩm nào", Toast.LENGTH_SHORT).show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("Customer_detail", "Refreshing product list for customer " + customer.getId());
            try {
                if (productRepository != null) {
                    productRepository.invalidateCache();
                    productRepository.refreshProducts();
                    Log.d("Customer_detail", "ProductRepository refreshed");
                } else {
                    Log.w("Customer_detail", "ProductRepository is null, skipping refresh");
                    Toast.makeText(this, "Không thể làm mới sản phẩm", Toast.LENGTH_SHORT).show();
                }
                if (linkViewModel != null) {
                    linkViewModel.refreshLinks();
                    Log.d("Customer_detail", "LinkViewModel refreshed");
                } else {
                    Log.w("Customer_detail", "LinkViewModel is null, skipping refresh");
                    Toast.makeText(this, "Không thể làm mới liên kết", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("Customer_detail", "Refresh failed: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi làm mới dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            btnXoa.setText("Huỷ");
            btnXoa.setAlpha(1.0f);
            btnXoa.setEnabled(true);
            isEditing = true;
        });

        btnLuu.setOnClickListener(v -> {
            String surname = etSurname.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String gender = etGender.getText().toString().trim();

            // Validation
            if (!validateInputs(surname, firstName, phone, email, birthday, address, gender)) {
                return;
            }

            viewModel.updateCustomer(customer.getSheetRowIndex(), surname, firstName, address, phone, email, birthday, gender);
            Toast.makeText(this, "Đang cập nhật khách hàng...", Toast.LENGTH_SHORT).show();
            setEditMode(false);
            btnLuu.setEnabled(false);
            btnLuu.setBackgroundColor(getColor(android.R.color.darker_gray));
            btnLuu.setAlpha(1.0f);
            btnSua.setEnabled(true);
            btnSua.setAlpha(1.0f);
            btnXoa.setEnabled(true);
            btnXoa.setAlpha(1.0f);
            btnXoa.setText("Xóa");
            isEditing = false;

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
                displayCustomerDetails();
                btnXoa.setText("Xóa");
                btnXoa.setAlpha(1.0f);
                btnXoa.setEnabled(true);
                isEditing = false;
                return;
            }

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
        etMaKH.setText(customer.getId());
        etMaKH.setEnabled(false);
        etSurname.setText(customer.getSurname());
        etFirstName.setText(customer.getFirstName());
        etPhone.setText(customer.getPhone());
        etEmail.setText(customer.getEmail());
        etBirthday.setText(customer.getBirthday());
        etAddress.setText(customer.getAddress());
        etGender.setText(customer.getGender());
    }

    private void setEditMode(boolean isEditable) {
        etSurname.setFocusable(isEditable);
        etSurname.setFocusableInTouchMode(isEditable);
        etSurname.setClickable(isEditable);

        etFirstName.setFocusable(isEditable);
        etFirstName.setFocusableInTouchMode(isEditable);
        etFirstName.setClickable(isEditable);

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

        etGender.setEnabled(isEditable);
        etGender.setFocusable(isEditable);
        etGender.setFocusableInTouchMode(false);
        etGender.setClickable(isEditable);
        if (isEditable) {
            String[] genderOptions = {"Nam", "Nữ"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    genderOptions
            );
            etGender.setAdapter(adapter);
            etGender.setOnClickListener(v -> etGender.showDropDown());
        } else {
            etGender.setOnClickListener(null);
            etGender.setKeyListener(null);
        }
    }

    private void combineData(MediatorLiveData<List<Product>> customerProductsLiveData,
                             List<Product> products, List<CustomerProductLink> links) {
        Log.d("Customer_detail", "Combining data: products=" + (products != null ? products.size() : "null") +
                ", links=" + (links != null ? links.size() : "null"));
        List<Product> customerProducts = new ArrayList<>();
        if (products == null || links == null) {
            Log.w("Customer_detail", "Products or links are null, cannot combine data");
            customerProductsLiveData.setValue(customerProducts);
            Toast.makeText(this, "Không thể tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        for (CustomerProductLink link : links) {
            Log.d("Customer_detail", "Checking link: customerId=" + link.getCustomerId() + ", productId=" + link.getProductId());
            if (link.getCustomerId().equals(customer.getId())) {
                Product product = getProductById(link.getProductId(), products);
                if (product != null) {
                    customerProducts.add(product);
                    Log.d("Customer_detail", "Added product: " + product.getId());
                } else {
                    Log.w("Customer_detail", "Product not found for productId=" + link.getProductId());
                }
            }
        }
        Log.d("Customer_detail", "Combined products for customer " + customer.getId() + ": " + customerProducts.size());
        customerProductsLiveData.setValue(customerProducts);
    }

    private Product getProductById(String productId, List<Product> products) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private boolean validateInputs(String surname, String firstName, String phone, String email, String birthday, String address, String gender) {
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

        // Validate birthday (optional, but if provided, must be in dd/MM/yyyy format and a valid date)
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

        // Validate gender
        if (!gender.equals("Nam") && !gender.equals("Nữ")) {
            Toast.makeText(this, "Giới tính phải là 'Nam' hoặc 'Nữ'", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}