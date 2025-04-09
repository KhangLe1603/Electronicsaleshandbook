package com.example.customerlistapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.customerlistapp.R;
import com.example.customerlistapp.viewmodel.CustomerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class CustomerList extends AppCompatActivity {
    private CustomerViewModel viewModel;
    private CustomerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabFilter, fabOption1, fabOption2;
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


        // Khởi tạo Search EditText
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString().trim());
            }
        });

        // Khởi tạo Filter Button
        Button btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng lọc trạng thái chưa được triển khai", Toast.LENGTH_SHORT).show();
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
        fabOption1 = findViewById(R.id.fabOption1);
        fabOption2 = findViewById(R.id.fabOption2);

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
            adapter.setCustomers(customers);

            if (customers == null || customers.isEmpty()) {
                Toast.makeText(this, "Không có khách hàng nào", Toast.LENGTH_SHORT).show();
            }

            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }

            // Kiểm tra nếu đang chờ thay đổi và kích thước không tăng
            if (expectingChange && lastCustomerSize != -1 && currentSize <= lastCustomerSize) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("CustomerList", "Retrying refresh due to unchanged size");
                    viewModel.refreshCustomers();
                }, 2000); // Thử lại sau 2 giây
            } else {
                expectingChange = false; // Reset cờ sau khi cập nhật thành công
            }
            lastCustomerSize = currentSize;
        });


        // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                viewModel.refreshCustomers();
                etSearch.setText("");
                sortDropdown.setText(sortAdapter.getItem(0), false);
                expectingChange = true; // Đặt cờ khi làm mới
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
                fabOption1.setVisibility(View.VISIBLE);
                fabOption2.setVisibility(View.VISIBLE);
                fabOption1.animate().translationY(-dpToPx(80)).setDuration(200).start();
                fabOption2.animate().translationY(-dpToPx(160)).setDuration(200).start();
                fabFilter.setImageResource(R.drawable.close); // Cần drawable "close"
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_open_color, getTheme()));
            } else {
                // Đóng menu
                fabOption1.animate().translationY(0).setDuration(200).start();
                fabOption2.animate().translationY(0).setDuration(200).start();
                fabOption1.setVisibility(View.GONE);
                fabOption2.setVisibility(View.GONE);
                fabFilter.setImageResource(R.drawable.menu_opiton); // Trở lại icon ban đầu
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_close_color, getTheme()));
            }
            isFabMenuOpen = !isFabMenuOpen;
        });

        fabOption1.setOnClickListener(v -> {
            Toast.makeText(this, "Tùy chọn 1 được chọn", Toast.LENGTH_SHORT).show();
            setupFabMenu(); // Đóng menu sau khi chọn
        });

        fabOption2.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm khách hàng", Toast.LENGTH_SHORT).show();
            setupFabMenu(); // Đóng menu sau khi chọn
            // Thêm logic thêm khách hàng ở đây
        });
    }
}