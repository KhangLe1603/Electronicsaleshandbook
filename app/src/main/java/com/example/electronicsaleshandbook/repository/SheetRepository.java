package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.Product;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SheetRepository {
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "Sheet1!B2:F"; // Lấy từ B2 đến E
    private static final long MIN_REFRESH_INTERVAL = 2000;
    private long lastRefreshTime = 0;
    private boolean isRefreshing = false;
    private final Sheets service;
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>();

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
            fetchProductsWithBackoff(0, 5);
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public LiveData<List<Product>> getProducts() {
        return productsLiveData;
    }

    public void refreshProducts() {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            if (isRefreshing || (currentTime - lastRefreshTime < MIN_REFRESH_INTERVAL)) {
                Log.d("SheetRepository", "Skipping refresh, too soon or already refreshing");
                return;
            }
            isRefreshing = true;
            lastRefreshTime = currentTime;
        }
        fetchProductsWithBackoff(0, 5);
        synchronized (this) {
            isRefreshing = false;
        }
    }

    public Sheets getSheetsService() {
        return service;
    }

    private int requestCount = 0; // Thêm biến đếm trong class

    private void fetchProductsWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                requestCount++;
                Log.d("SheetRepository", "Sending request #" + requestCount);
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, RANGE)
                        .execute();
                List<Product> products = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                Log.d("SheetRepository", "Raw response values: " + (values != null ? values.toString() : "null"));
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String name = row.size() > 0 ? row.get(0).toString() : "";
                        String description = row.size() > 1 ? row.get(1).toString() : "";
                        String unitPrice = row.size() > 2 ? row.get(2).toString() : "";
                        String sellingPrice = row.size() > 3 ? row.get(3).toString() : "";
                        String unit = row.size() > 4 ? row.get(4).toString() : "";
                        Product product = new Product(name, description, unitPrice, sellingPrice, unit);
                        product.setSheetRowIndex(i + 2);
                        products.add(product);
                    }
                }
                Log.d("SheetRepository", "Fetched products, size: " + products.size());
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
                        productsLiveData.postValue(productsLiveData.getValue());
                    }
                } else {
                    Log.e("SheetRepository", "Error fetching products: " + e.getStatusCode(), e);
                    productsLiveData.postValue(productsLiveData.getValue());
                }
            } catch (IOException e) {
                Log.e("SheetRepository", "Error fetching products", e);
                productsLiveData.postValue(productsLiveData.getValue());
            }
        }).start();
    }

}