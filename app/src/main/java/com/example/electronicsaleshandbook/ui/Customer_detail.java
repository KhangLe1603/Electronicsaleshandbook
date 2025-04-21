package com.example.electronicsaleshandbook.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.example.electronicsaleshandbook.repository.CustomerRepository;
import com.example.electronicsaleshandbook.repository.SheetRepository;
import com.example.electronicsaleshandbook.viewmodel.CustomerProductLinkViewModel;
import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class Customer_detail extends AppCompatActivity {
    private EditText etPhone, etEmail, etBirthday, etAddress, editId;
    private Button btnSua, btnLuu, btnXoa;
    private CustomerViewModel viewModel;
    private Customer customer;
    private TextView tvCustomerName;
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
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.imageButton);
        btnBack.setOnClickListener(v -> finish());
        editId = findViewById(R.id.etMaKH);
        tvCustomerName = findViewById(R.id.tvCustomerName);
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

            // Chuyển nút Xoá thành nút Huỷ
            btnXoa.setText("Huỷ");
            btnXoa.setAlpha(1.0f);
            btnXoa.setEnabled(true);
            isEditing = true;
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
            isEditing = false;
            btnXoa.setText("Xóa");

            Intent intent = new Intent();
            intent.putExtra("REFRESH", true);
            setResult(RESULT_OK, intent);
            finish();
        });

        btnXoa.setOnClickListener(v -> {
            if (isEditing) {
                // Đang ở chế độ sửa → giờ là nút Huỷ
                setEditMode(false);
                btnLuu.setEnabled(false);
                btnLuu.setBackgroundColor(getColor(android.R.color.darker_gray));

                btnSua.setEnabled(true);
                btnSua.setAlpha(1.0f);
                // Khôi phục dữ liệu ban đầu
                displayCustomerDetails();

                // Đổi lại nút Huỷ thành Xoá
                btnXoa.setText("Xoá");
                btnXoa.setAlpha(1.0f);
                btnXoa.setEnabled(true);
                isEditing = false;
                return;
            }

            // Không phải đang sửa → xử lý xoá thật
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
        editId.setText(customer.getId());
        editId.setEnabled(false);
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
        // Chỉ gán OnClickListener khi ở chế độ chỉnh sửa
        if (isEditable) {
            etBirthday.setOnClickListener(v -> showDatePickerDialog());
        } else {
            etBirthday.setOnClickListener(null); // Xóa listener khi không chỉnh sửa
        }

        etAddress.setFocusable(isEditable);
        etAddress.setFocusableInTouchMode(isEditable);
        etAddress.setClickable(isEditable);

        etGender.setEnabled(isEditable);
        etGender.setFocusable(isEditable);
        etGender.setFocusableInTouchMode(false);
        etGender.setClickable(isEditable); // chỉ cho bấm khi đang sửa
        if (isEditable) {
            // Gán lại adapter mỗi khi vào chế độ sửa
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