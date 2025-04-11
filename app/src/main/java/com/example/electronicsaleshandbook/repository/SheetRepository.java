package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.Product;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SheetRepository {
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "Sheet1!B2:F"; // Lấy từ B2 đến F
    private static final long MIN_REFRESH_INTERVAL = 2000; // 2 giây giữa các refresh

    private final Sheets service;
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>();
    private List<Product> cachedProducts = null; // Cache dữ liệu sản phẩm
    private long lastRefreshTime = 0;
    private boolean isRefreshing = false;
    private int requestCount = 0; // Đếm số request

    public SheetRepository(Context context) throws IOException, GeneralSecurityException {
        try {
            GoogleCredential credential = GoogleCredential.fromStream(
                            context.getAssets().open("service_account.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            service = new Sheets.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Your App Name")
                    .build();
            if (cachedProducts == null) {
                fetchProductsWithBackoff(0, 5); // Lấy dữ liệu lần đầu nếu cache trống
            } else {
                productsLiveData.postValue(cachedProducts); // Dùng cache nếu đã có
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public LiveData<List<Product>> getProducts() {
        return productsLiveData;
    }

    public void refreshProducts() {
        synchronized (this) {
            if (isRefreshing) {
                Log.d("SheetRepository", "Skipping refresh, already refreshing");
                if (cachedProducts != null) {
                    productsLiveData.postValue(cachedProducts);
                }
                return;
            }
            // Chỉ làm mới nếu cache đã bị vô hiệu hóa
            if (cachedProducts != null) {
                Log.d("SheetRepository", "Using cached products, size: " + cachedProducts.size());
                productsLiveData.postValue(cachedProducts);
                return;
            }
            isRefreshing = true;
            lastRefreshTime = System.currentTimeMillis();
        }
        fetchProductsWithBackoff(0, 5);
        synchronized (this) {
            isRefreshing = false;
        }
    }

    public Sheets getSheetsService() {
        return service;
    }

    private void fetchProductsWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                requestCount++;
                Log.d("SheetRepository", "Sending request #" + requestCount);
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, "Sheet1!A2:G")
                        .execute();
                List<Product> products = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String id = row.size() > 1 ? row.get(1).toString() : "";
                        String name = row.size() > 2 ? row.get(2).toString() : "";
                        String description = row.size() > 3 ? row.get(3).toString() : "";
                        String unitPrice = row.size() > 4 ? row.get(4).toString() : "";
                        String sellingPrice = row.size() > 5 ? row.get(5).toString() : "";
                        String unit = row.size() > 6 ? row.get(6).toString() : "";
                        Product product = new Product(name, description, unitPrice, sellingPrice, unit);
                        product.setId(id);
                        product.setSheetRowIndex(i + 2);
                        products.add(product);
                    }
                }
                cachedProducts = products;
                productsLiveData.postValue(products);
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("SheetRepository", "Quota exceeded, retrying in " + delay + "ms (attempt " + (attempt + 1) + "/" + maxAttempts + ")");
                    try {
                        Thread.sleep(delay);
                        fetchProductsWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("SheetRepository", "Interrupted during backoff", ie);
                        if (cachedProducts != null) productsLiveData.postValue(cachedProducts);
                    }
                } else {
                    Log.e("SheetRepository", "Error fetching products: " + e.getStatusCode(), e);
                    if (cachedProducts != null) productsLiveData.postValue(cachedProducts);
                }
            } catch (IOException e) {
                Log.e("SheetRepository", "IO error fetching products", e);
                if (cachedProducts != null) productsLiveData.postValue(cachedProducts);
            }
        }).start();
    }

    // Phương thức để vô hiệu hóa cache khi có thay đổi
    public void invalidateCache() {
        cachedProducts = null;
        Log.d("SheetRepository", "Product cache invalidated");
    }
}