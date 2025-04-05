package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.SheetRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProductViewModel extends ViewModel {
    private final SheetRepository repository;
    private final LiveData<List<Product>> allProducts;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> sortOption = new MutableLiveData<>(0);

    public ProductViewModel(Context context) {
        try {
            repository = new SheetRepository(context);
            allProducts = repository.getProducts();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize repository", e);
        }
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSortOption(int option) {
        sortOption.setValue(option);
    }

    public LiveData<List<Product>> getFilteredProducts() {
        return Transformations.switchMap(searchQuery, query ->
                Transformations.switchMap(sortOption, sort ->
                        Transformations.map(allProducts, products -> {
                            if (products == null) {
                                return null; // Trả về null nếu không có dữ liệu
                            }

                            // Khai báo rõ ràng filteredList là List<Product>
                            List<Product> filteredList = products;

                            // Lọc theo từ khóa tìm kiếm (nếu có)
                            if (query != null && !query.isEmpty()) {
                                filteredList = filteredList.stream()
                                        .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                                        .collect(Collectors.toList());
                            }

                            // Sắp xếp theo tiêu chí
                            switch (sort) {
                                case 0: // Tên A-Z
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Product::getName))
                                            .collect(Collectors.toList());
                                case 1: // Tên Z-A
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Product::getName, Comparator.reverseOrder()))
                                            .collect(Collectors.toList());
                                case 2: // Giá tăng dần
                                    return filteredList.stream()
                                            .sorted(Comparator.comparingDouble(product -> parsePrice(product.getPrice())))
                                            .collect(Collectors.toList());
                                case 3: // Giá giảm dần
                                    return filteredList.stream()
                                            .sorted(Comparator.comparingDouble((Product product) -> parsePrice(product.getPrice())).reversed())
                                            .collect(Collectors.toList());
                                default:
                                    return filteredList; // Không sắp xếp
                            }
                        })));
    }

    public void refreshProducts() {
        searchQuery.setValue("");
        sortOption.setValue(0);
        repository.refreshProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    private double parsePrice(String price) {
        if (price == null || price.isEmpty()) {
            Log.w("ProductViewModel", "Price is null or empty");
            return 0.0; // Giá trị mặc định nếu giá rỗng
        }

        String cleanedPrice = price.replaceAll("[^0-9.]", "");

        cleanedPrice = cleanedPrice.replaceAll("\\.(?=.*\\.)", "");

        Log.d("ProductViewModel", "Cleaned price: " + cleanedPrice);

        try {
            return Double.parseDouble(cleanedPrice);
        } catch (NumberFormatException e) {
            Log.e("ProductViewModel", "Failed to parse price: " + price, e);
            return 0.0;
        }
    }

}