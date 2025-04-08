package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.SheetRepository;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
        searchQuery.postValue(""); // Dùng postValue thay vì setValue
        sortOption.postValue(0);   // Dùng postValue thay vì setValue
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

    // Thêm sản phẩm mới vào Google Sheets
    public void addProduct(String name, String description, String unitPrice, String sellingPrice, String unit) {
        new Thread(() -> {
            try {
                // Lấy dữ liệu hiện tại để tìm dòng trống cuối cùng từ cột B
                ValueRange existingData = repository.getSheetsService().spreadsheets().values()
                        .get("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", "Sheet1!B2:F")
                        .execute();
                int lastRow = existingData.getValues() != null ? existingData.getValues().size() + 1 : 1; // Dòng cuối + 1

                // Chỉ định phạm vi chính xác: B<row>:F<row>
                String range = "Sheet1!B" + (lastRow + 1) + ":F" + (lastRow + 1);

                // Chuẩn bị dữ liệu với thứ tự đúng: B (name), C (description), D (unitPrice), E (sellingPrice), F (unit)
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(
                                        name,
                                        description,
                                        Double.parseDouble(unitPrice),
                                        Double.parseDouble(sellingPrice),
                                        unit)
                        ));

                // Ghi dữ liệu vào phạm vi đã chỉ định
                repository.getSheetsService().spreadsheets().values()
                        .update("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", range, body)
                        .setValueInputOption("RAW")
                        .execute();

                // Làm mới danh sách
                refreshProducts();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void updateProduct(int sheetRowIndex, String name, String description, String unitPrice, String sellingPrice, String unit) {
        new Thread(() -> {
            try {
                String range = "Sheet1!B" + sheetRowIndex + ":F" + sheetRowIndex;

                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(name, description, Double.parseDouble(unitPrice), Double.parseDouble(sellingPrice), unit)
                        ));

                repository.getSheetsService().spreadsheets().values()
                        .update("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", range, body)
                        .setValueInputOption("RAW")
                        .execute();

                refreshProducts();
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void deleteProduct(int sheetRowIndex) {
        new Thread(() -> {
            try {
                int adjustedRowIndex = sheetRowIndex - 1; // Chỉ số dòng cho BatchUpdate bắt đầu từ 0

                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                        .setRange(new DimensionRange()
                                .setSheetId(0)
                                .setDimension("ROWS")
                                .setStartIndex(adjustedRowIndex)
                                .setEndIndex(adjustedRowIndex + 1)
                        );

                BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                        .setRequests(Collections.singletonList(
                                new Request().setDeleteDimension(deleteRequest)
                        ));

                repository.getSheetsService().spreadsheets()
                        .batchUpdate("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", batchRequest)
                        .execute();
                refreshProducts();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}