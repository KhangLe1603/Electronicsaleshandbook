package com.example.customerlistapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.customerlistapp.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class CustomerList extends AppCompatActivity {
    private CustomerViewModel viewModel;
    private CustomerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabFilter, fabOption_CustomerList, fabOption_ProductList, fabOption_AddProduct, fabOption_AddCustomer;
    private MaterialAutoCompleteTextView sortDropdown;
    private boolean isFabMenuOpen = false;
    private boolean expectingChange = false;
    private int lastCustomerSize = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        // Khởi tạo RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter();
        recyclerView.setAdapter(adapter);

        // Gắn listener cho adapter để mở Customer_detail
        adapter.setOnCustomerClickListener(customer -> {
            Intent intent = new Intent(CustomerList.this, Customer_detail.class);
            intent.putExtra("CUSTOMER", customer);
            startActivityForResult(intent, 2);
        });

        EditText searchBar = findViewById(R.id.searchBar);

        // Khởi tạo Filter Button
        ImageButton btnFilter = findViewById(R.id.imageButtonSearch);
        btnFilter.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            viewModel.setSearchQuery(query);
        });

        // Khởi tạo Dropdown sắp xếp
        sortDropdown = findViewById(R.id.sortDropdown);
        String[] sortOptions = new String[]{"Từ A-Z", "Từ Z-A", "Theo địa chỉ"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sortOptions);
        sortDropdown.setAdapter(sortAdapter);
        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            viewModel.setSortOption(position); // Cập nhật tiêu chí sắp xếp
            Toast.makeText(this, "Sắp xếp theo: " + sortAdapter.getItem(position), Toast.LENGTH_SHORT).show();
        });

        // Khởi tạo FABs
        fabFilter = findViewById(R.id.fabFilter);
        fabOption_CustomerList = findViewById(R.id.fabOption1);
        fabOption_ProductList = findViewById(R.id.fabOption2);
        fabOption_AddProduct = findViewById(R.id.fabAddProduct);
        fabOption_AddCustomer = findViewById(R.id.fabAddCustomer);

        setupFabMenu();

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new CustomerViewModel(CustomerList.this);
                    }
                }).get(CustomerViewModel.class);

        // Quan sát danh sách khách hàng
        viewModel.getFilteredCustomers().observe(this, customers -> {
            int currentSize = customers != null ? customers.size() : 0;
            Log.d("CustomerList", "Customers updated, size: " + currentSize);
            adapter.setCustomers(customers);
            if (customers == null || customers.isEmpty()) {
                Toast.makeText(this, "Không có khách hàng nào", Toast.LENGTH_SHORT).show();
            }
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (expectingChange && lastCustomerSize != -1 && currentSize <= lastCustomerSize) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    viewModel.refreshCustomers();
                }, 3000); // Tăng lên 3 giây
            } else {
                expectingChange = false;
            }
            lastCustomerSize = currentSize;
        });


        // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                viewModel.refreshCustomers();
                searchBar.setText("");
                sortDropdown.setText(sortAdapter.getItem(0), false);
            });
        }


    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void setupFabMenu() {
        fabFilter.setOnClickListener(v -> {
            if (!isFabMenuOpen) {
                // Mở menu
                fabOption_CustomerList.setVisibility(View.VISIBLE);
                fabOption_ProductList.setVisibility(View.VISIBLE);
                fabOption_AddProduct.setVisibility(View.VISIBLE);
                fabOption_AddCustomer.setVisibility(View.VISIBLE);
                fabOption_CustomerList.animate().translationY(-dpToPx(80)).setDuration(200).start();
                fabOption_ProductList.animate().translationY(-dpToPx(160)).setDuration(200).start();
                fabOption_AddProduct.animate().translationY(-dpToPx(240)).setDuration(200).start();
                fabOption_AddCustomer.animate().translationY(-dpToPx(320)).setDuration(200).start();
                fabFilter.setImageResource(R.drawable.close); // Cần drawable "close"
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_open_color, getTheme()));
            } else {
                // Đóng menu
                fabOption_CustomerList.animate().translationY(0).setDuration(200).start();
                fabOption_ProductList.animate().translationY(0).setDuration(200).start();
                fabOption_AddProduct.animate().translationY(0).setDuration(200).start();
                fabOption_AddCustomer.animate().translationY(0).setDuration(200).start();
                fabOption_CustomerList.setVisibility(View.GONE);
                fabOption_ProductList.setVisibility(View.GONE);
                fabOption_AddProduct.setVisibility(View.GONE);
                fabOption_AddCustomer.setVisibility(View.GONE);
                fabFilter.setImageResource(R.drawable.menu_opiton); // Trở lại icon ban đầu
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_close_color, getTheme()));
            }
            isFabMenuOpen = !isFabMenuOpen;
        });

        fabOption_ProductList.setOnClickListener(v -> {

        });

        fabOption_AddProduct.setOnClickListener(v -> {

        });

        fabOption_AddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerList.this, AddCustomer.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == 1 || requestCode == 2)) {
            if (data != null && data.getBooleanExtra("REFRESH", false)) {
                expectingChange = true;
                SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
                swipeRefreshLayout.setRefreshing(true); // Hiển thị loading
                viewModel.refreshCustomers(); // Gọi làm mới
            }
        }
    }
}