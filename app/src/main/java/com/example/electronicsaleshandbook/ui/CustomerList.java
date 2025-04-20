package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class CustomerList extends AppCompatActivity {
    private CustomerViewModel viewModel_Customer;
    private ProductViewModel viewModel_Product;
    private CustomerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabFilter, fabOption_CustomerList, fabOption_ProductList, fabOption_AddProduct, fabOption_AddCustomer, fabOption_AddLink;
    private MaterialAutoCompleteTextView sortDropdown;
    private boolean isFabMenuOpen = false;
    private boolean expectingChange = false;
    private int lastCustomerSize = -1;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    private static final int REQUEST_ADD_CUSTOMER = 1;
    private static final int REQUEST_CUSTOMER_DETAIL = 2;
    private static final int REQUEST_ADD_PRODUCT = 3;
    private static final int REQUEST_ADD_LINK = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        View mainLayout = findViewById(R.id.customerListLayout);

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        // Khởi tạo RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter();
        recyclerView.setAdapter(adapter);

        // Gắn listener cho adapter để mở Customer_detail
        adapter.setOnCustomerClickListener(customer -> {
            Intent intent = new Intent(CustomerList.this, Customer_detail.class);
            intent.putExtra("CUSTOMER", customer);
            startActivityForResult(intent, REQUEST_CUSTOMER_DETAIL);
        });

        EditText searchBar = findViewById(R.id.searchBar);

        // Khởi tạo Filter Button
        ImageButton btnFilter = findViewById(R.id.imageButtonSearch);
        btnFilter.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            viewModel_Customer.setSearchQuery(query);
        });

        // Khởi tạo Dropdown sắp xếp
        sortDropdown = findViewById(R.id.sortDropdown);
        String[] sortOptions = new String[]{"Từ A-Z", "Từ Z-A", "Theo địa chỉ"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sortOptions);
        sortDropdown.setAdapter(sortAdapter);
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            viewModel_Customer.setSortOption(position); // Cập nhật tiêu chí sắp xếp
            Toast.makeText(this, "Sắp xếp theo: " + sortAdapter.getItem(position), Toast.LENGTH_SHORT).show();
        });

        // Khởi tạo FABs
        fabFilter = findViewById(R.id.fabFilter);
        fabOption_CustomerList = findViewById(R.id.fabOption1);
        fabOption_ProductList = findViewById(R.id.fabOption2);
        fabOption_AddProduct = findViewById(R.id.fabAddProduct);
        fabOption_AddCustomer = findViewById(R.id.fabAddCustomer);
        fabOption_AddLink = findViewById(R.id.fabAddLink);
        setupFabMenu();

        // Khởi tạo ViewModel
        viewModel_Customer = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new CustomerViewModel(CustomerList.this);
                    }
                }).get(CustomerViewModel.class);

        // Quan sát danh sách khách hàng
        viewModel_Customer.getFilteredCustomers().observe(this, customers -> {
            int currentSize = customers != null ? customers.size() : 0;
            Log.d("CustomerList", "Customers updated, size: " + currentSize);
            adapter.setCustomers(customers);
            swipeRefreshLayout.setRefreshing(false);

            if (expectingChange && lastCustomerSize != -1 && currentSize <= lastCustomerSize && retryCount < MAX_RETRIES) {
                retryCount++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("CustomerList", "Retrying refresh, attempt " + retryCount + "/" + MAX_RETRIES);
                    viewModel_Customer.refreshCustomers();
                }, 2000);
            } else {
                expectingChange = false;
                retryCount = 0;
                if (customers == null || customers.isEmpty()) {
                    Toast.makeText(this, "Không có khách hàng nào", Toast.LENGTH_SHORT).show();
                }
            }
            lastCustomerSize = currentSize;
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel_Customer.refreshCustomers();
            searchBar.setText("");
            sortDropdown.setText(sortAdapter.getItem(0), false);
        });


    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void setupFabMenu() {
        fabFilter.setOnClickListener(v -> {
            if (!isFabMenuOpen) {
                // Mở menu
                fabOption_ProductList.setVisibility(View.VISIBLE);
                fabOption_AddProduct.setVisibility(View.VISIBLE);
                fabOption_AddCustomer.setVisibility(View.VISIBLE);
                fabOption_AddLink.setVisibility(View.VISIBLE);

                fabOption_ProductList.animate().translationY(-dpToPx(80)).setDuration(200).start();
                fabOption_AddProduct.animate().translationY(-dpToPx(160)).setDuration(200).start();
                fabOption_AddCustomer.animate().translationY(-dpToPx(240)).setDuration(200).start();
                fabOption_AddLink.animate().translationY(-dpToPx(320)).setDuration(200).start();
                fabFilter.setImageResource(R.drawable.close);
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_open_color, getTheme()));
            } else {
                // Đóng menu
                fabOption_ProductList.animate().translationY(0).setDuration(200)
                        .withEndAction(() -> fabOption_ProductList.setVisibility(View.GONE)).start();
                fabOption_AddProduct.animate().translationY(0).setDuration(200)
                        .withEndAction(() -> fabOption_AddProduct.setVisibility(View.GONE)).start();
                fabOption_AddCustomer.animate().translationY(0).setDuration(200)
                        .withEndAction(() -> fabOption_AddCustomer.setVisibility(View.GONE)).start();
                fabOption_AddLink.animate().translationY(0).setDuration(200)
                        .withEndAction(() -> fabOption_AddLink.setVisibility(View.GONE)).start();
                fabFilter.setImageResource(R.drawable.menu_opiton);
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_close_color, getTheme()));
            }
            isFabMenuOpen = !isFabMenuOpen;
        });

        fabOption_ProductList.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerList.this, ProductsView.class);
            startActivity(intent);
            finish(); // Chuyển hoàn toàn sang ProductsView
        });

        fabOption_AddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerList.this, AddProductActivity.class);
            startActivityForResult(intent, REQUEST_ADD_PRODUCT);
        });

        fabOption_AddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerList.this, AddCustomer.class);
            startActivityForResult(intent, REQUEST_ADD_CUSTOMER);
        });
        fabOption_AddLink.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerList.this, LinkCustomerProduct.class);
            startActivityForResult(intent, REQUEST_ADD_LINK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CustomerList", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (resultCode == RESULT_OK && data != null && data.getBooleanExtra("REFRESH", false)) {
            expectingChange = true;
            retryCount = 0;
            swipeRefreshLayout.setRefreshing(true);
            switch (requestCode) {
                case REQUEST_ADD_CUSTOMER: // Từ AddCustomer
                    viewModel_Customer.refreshCustomers();
                    break;
                case REQUEST_CUSTOMER_DETAIL: // Từ Customer_detail
                    viewModel_Customer.refreshCustomers();
                    break;
                case REQUEST_ADD_PRODUCT: // Từ AddProductActivity
                    viewModel_Product.refreshProducts(); // Nếu CustomerList hiển thị sản phẩm
                    break;
                case REQUEST_ADD_LINK:
                    viewModel_Product.refreshProducts();
                    break;
                default:
                    Log.w("CustomerList", "Unknown requestCode: " + requestCode);
            }
        }
    }
}