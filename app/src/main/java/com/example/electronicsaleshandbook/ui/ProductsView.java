package com.example.electronicsaleshandbook.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

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

        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.Factory() {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new ProductViewModel(ProductsView.this);
                    }
                }).get(ProductViewModel.class);

        viewModel.getProducts().observe(this, products -> {
            adapter.setProducts(products);
        });

        // Xử lý FAB và search bar
        setupFabMenu();
        setupSearchBar();
    }

    private void setupFabMenu() {
        FloatingActionButton fabFilter = findViewById(R.id.fabFilter);
        FloatingActionButton fabOption1 = findViewById(R.id.fabOption1);
        FloatingActionButton fabOption2 = findViewById(R.id.fabOption2);

        fabFilter.setOnClickListener(v -> {
            boolean isVisible = fabOption1.getVisibility() == View.VISIBLE;
            fabOption1.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            fabOption2.setVisibility(isVisible ? View.GONE : View.VISIBLE);
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