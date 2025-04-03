package com.example.electronicsaleshandbook.viewmodel;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.model.Products;
import com.example.electronicsaleshandbook.ui.RecyclerView_Adapter_Products;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class ProductManagement extends AppCompatActivity {
    FloatingActionButton fabMain, fab1, fab2;
    boolean isFabOpen = false;
    private RecyclerView recyclerView;
    private RecyclerView_Adapter_Products productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_management);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabMain = findViewById(R.id.fabFilter);
        fab1 = findViewById(R.id.fabOption1);
        fab2 = findViewById(R.id.fabOption2);
        fabMain.setOnClickListener(view -> {
            if (isFabOpen) {
                closeMenu();
            } else {
                openMenu();
            }
        });

        // Tạo danh sách sản phẩm
        List<Products> productList = Arrays.asList(
                new Products("Sản phẩm A", "1.000.000 VNĐ"),
                new Products("Sản phẩm B", "2.000.000 VNĐ"),
                new Products("Sản phẩm C", "3.000.000 VNĐ"),
                new Products("Sản phẩm D", "4.000.000 VNĐ"),
                new Products("Sản phẩm E", "4.000.000 VNĐ"),
                new Products("Sản phẩm F", "4.000.000 VNĐ"),
                new Products("Sản phẩm G", "4.000.000 VNĐ"),
                new Products("Sản phẩm H", "4.000.000 VNĐ"),
                new Products("Sản phẩm H", "4.000.000 VNĐ"),
                new Products("Sản phẩm J", "4.000.000 VNĐ"),
                new Products("Sản phẩm K", "5.000.000 VNĐ")
        );

        // Gán adapter cho RecyclerView
        productAdapter = new RecyclerView_Adapter_Products(productList);
        recyclerView.setAdapter(productAdapter);
    }

    private void openMenu() {
        fab1.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);

        fab1.animate().translationY(-180).alpha(1).setDuration(200);
        fab2.animate().translationY(-360).alpha(1).setDuration(200);

        fabMain.setImageResource(R.drawable.close); // Chuyển thành nút đóng
        isFabOpen = true;
    }

    private void closeMenu() {
        fab1.animate().translationY(0).alpha(0).setDuration(200);
        fab2.animate().translationY(0).alpha(0).setDuration(200);

        new Handler().postDelayed(() -> {
            fab1.setVisibility(View.GONE);
            fab2.setVisibility(View.GONE);
        }, 200);

        fabMain.setImageResource(R.drawable.icon_menu); // Quay lại icon menu
        isFabOpen = false;
    }
}