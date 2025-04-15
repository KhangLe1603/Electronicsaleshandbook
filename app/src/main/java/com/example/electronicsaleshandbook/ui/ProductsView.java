package com.example.electronicsaleshandbook.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.viewmodel.CustomerViewModel;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class ProductsView extends AppCompatActivity {

    private ProductViewModel viewModel_Product;
    private CustomerViewModel viewModel_Customer;
    private ProductAdapter adapter;
    private int lastProductSize = -1;
    private boolean expectingChange = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    private static final int REQUEST_ADD_CUSTOMER = 3;
    private static final int REQUEST_PRODUCT_DETAIL = 2;
    private static final int REQUEST_ADD_PRODUCT = 1;
    private ImageView userIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_management);

        View ProductListLayout = findViewById(R.id.ProductList_Layout);
        ViewCompat.setOnApplyWindowInsetsListener(ProductListLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,  // dùng đúng khoảng cách status bar thực tế
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        EditText searchBar = findViewById(R.id.searchBar);
        ImageButton searchButton = findViewById(R.id.imageButtonSearch);

        MaterialAutoCompleteTextView sortDropdown = findViewById(R.id.sortDropdown);
        ArrayAdapter<CharSequence> adapterDropdown = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_dropdown_item_1line
        );
        sortDropdown.setAdapter(adapterDropdown);

        viewModel_Product = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ProductViewModel(ProductsView.this);
                    }
                }).get(ProductViewModel.class);

        viewModel_Product.getFilteredProducts().observe(this, products -> {
            int currentSize = products != null ? products.size() : 0;
            Log.d("ProductsView", "Observer triggered, products size: " + currentSize);
            adapter.setProducts(products);
            swipeRefreshLayout.setRefreshing(false);

            if (expectingChange && lastProductSize != -1 && currentSize <= lastProductSize && retryCount < MAX_RETRIES) {
                retryCount++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("ProductsView", "Retrying refresh due to unchanged size, attempt " + retryCount + "/" + MAX_RETRIES);
                    viewModel_Product.refreshProducts();
                }, 1000);
            } else {
                expectingChange = false;
                retryCount = 0; // Reset sau khi thành công hoặc hết retry
            }
            lastProductSize = currentSize;
        });

        adapter.setOnProductClickListener(product -> {
            Intent intent = new Intent(ProductsView.this, ProductDetail.class);
            intent.putExtra("PRODUCT", product);
            startActivityForResult(intent, REQUEST_PRODUCT_DETAIL);
        });
        searchButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            viewModel_Product.setSearchQuery(query); // Cập nhật từ khóa tìm kiếm
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel_Product.refreshProducts(); // Gọi làm mới dữ liệu
            searchBar.setText("");
            sortDropdown.setText(adapterDropdown.getItem(0), false);
        });

        sortDropdown.setOnItemClickListener((parent, view, position, id) -> {
            viewModel_Product.setSortOption(position); // Cập nhật tiêu chí sắp xếp
        });

        userIcon = findViewById(R.id.userIcon);

        userIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsView.this, AccountActivity.class);
            startActivity(intent);
        });

        setupFabMenu();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void setupFabMenu() {
        FloatingActionButton fabFilter = findViewById(R.id.fabFilter);
        FloatingActionButton fabOption_CustomerList = findViewById(R.id.fabOption1);
        FloatingActionButton fabOption_ProductList = findViewById(R.id.fabOption2);
        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
        FloatingActionButton fabAddCustomer = findViewById(R.id.fabAddCustomer);

        final boolean[] isExpanded = {false};

        fabFilter.setOnClickListener(v -> {
            if (!isExpanded[0]) {
                fabOption_CustomerList.setVisibility(View.VISIBLE);
                fabAddProduct.setVisibility(View.VISIBLE);
                fabAddCustomer.setVisibility(View.VISIBLE);

                fabOption_CustomerList.animate().translationY(-dpToPx(80)).setDuration(200).start();
                fabAddProduct.animate().translationY(-dpToPx(160)).setDuration(200).start();
                fabAddCustomer.animate().translationY(-dpToPx(240)).setDuration(200).start();

                fabFilter.setImageResource(R.drawable.close); // icon back
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_open_color, getTheme()));
            } else {
                // Đóng menu: di chuyển xuống và ẩn
                fabOption_CustomerList.animate().translationY(0).setDuration(200).withEndAction(() -> fabOption_CustomerList.setVisibility(View.GONE)).start();
                fabAddProduct.animate().translationY(0).setDuration(200).withEndAction(() -> fabAddProduct.setVisibility(View.GONE)).start();
                fabAddCustomer.animate().translationY(0).setDuration(200).withEndAction(() -> fabAddCustomer.setVisibility(View.GONE)).start();

                fabFilter.setImageResource(R.drawable.menu_opiton); // icon gốc
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_close_color, getTheme()));
            }

            isExpanded[0] = !isExpanded[0];
        });

        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsView.this, AddProductActivity.class);
            startActivityForResult(intent, 1);
        });
        fabAddCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsView.this, AddCustomer.class);
            startActivityForResult(intent, 3);
        });
        fabOption_CustomerList.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsView.this, CustomerList.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ProductsView", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (resultCode == RESULT_OK && data != null && data.getBooleanExtra("REFRESH", false)) {
            expectingChange = true;
            retryCount = 0;
            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(true);

            // Làm mới dữ liệu dựa trên requestCode
            switch (requestCode) {
                case REQUEST_ADD_PRODUCT: // Thêm sản phẩm
                    viewModel_Product.refreshProducts();
                    break;
                case REQUEST_PRODUCT_DETAIL: // Chi tiết sản phẩm
                    viewModel_Product.refreshProducts();
                    break;
                case REQUEST_ADD_CUSTOMER: // Thêm khách hàng
                    viewModel_Customer.refreshCustomers();
                    break;
                default:
                    Log.w("ProductsView", "Unknown requestCode: " + requestCode);
            }
        }
    }

}