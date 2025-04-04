package com.example.electronicsaleshandbook.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.viewmodel.ProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductsView extends AppCompatActivity {

    private ProductViewModel viewModel;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_management);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        EditText searchBar = findViewById(R.id.searchBar);
        ImageButton searchButton = findViewById(R.id.imageButtonSearch);

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ProductViewModel(ProductsView.this);
                    }
                }).get(ProductViewModel.class);

        viewModel.getFilteredProducts().observe(this, products -> {
            adapter.setProducts(products);
            swipeRefreshLayout.setRefreshing(false);
        });


        searchButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            viewModel.setSearchQuery(query); // Cập nhật từ khóa tìm kiếm
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshProducts(); // Gọi làm mới dữ liệu
            searchBar.setText("");
        });

        setupFabMenu();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void setupFabMenu() {
        FloatingActionButton fabFilter = findViewById(R.id.fabFilter);
        FloatingActionButton fabOption1 = findViewById(R.id.fabOption1);
        FloatingActionButton fabOption2 = findViewById(R.id.fabOption2);

        final boolean[] isExpanded = {false};

        fabFilter.setOnClickListener(v -> {
            if (!isExpanded[0]) {
                // Mở menu: hiện 2 nút và di chuyển lên theo Y (đơn vị pixel)
                fabOption1.setVisibility(View.VISIBLE);
                fabOption2.setVisibility(View.VISIBLE);

                fabOption1.animate().translationY(-dpToPx(80)).setDuration(200).start();
                fabOption2.animate().translationY(-dpToPx(160)).setDuration(200).start();

                fabFilter.setImageResource(R.drawable.close); // icon back
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_open_color, getTheme()));
            } else {
                // Đóng menu: di chuyển xuống và ẩn
                fabOption1.animate().translationY(0).setDuration(200).withEndAction(() -> fabOption1.setVisibility(View.GONE)).start();
                fabOption2.animate().translationY(0).setDuration(200).withEndAction(() -> fabOption2.setVisibility(View.GONE)).start();

                fabFilter.setImageResource(R.drawable.icon_menu); // icon gốc
                fabFilter.setBackgroundTintList(getResources().getColorStateList(R.color.fab_close_color, getTheme()));
            }

            isExpanded[0] = !isExpanded[0];
        });
    }

    private void setupSearchBar() {
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Lọc danh sách sản phẩm theo search
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}