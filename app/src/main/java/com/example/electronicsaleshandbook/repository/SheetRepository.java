package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.Product;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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
    private static final String RANGE = "Sheet1!B2:E"; // Lấy từ B2 đến E

    private final Sheets service;
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>();

    public SheetRepository(Context context) throws IOException, GeneralSecurityException {
        try {
            GoogleCredential credential = GoogleCredential.fromStream(
                            context.getAssets().open("service_account.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

            service = new Sheets.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Your App Name")
                    .build();
            fetchProducts();
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public LiveData<List<Product>> getProducts() {
        return productsLiveData;
    }

    public void refreshProducts() {
        fetchProducts();
    }

    private void fetchProducts() {
        new Thread(() -> {
            try {
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, RANGE)
                        .execute();

                List<Product> products = new ArrayList<>();
                List<List<Object>> values = response.getValues();

                if (values != null) {
                    for (List<Object> row : values) {
                        String name = row.size() > 0 ? row.get(0).toString() : "";
                        String price = row.size() > 3 ? row.get(3).toString() : "";
                        products.add(new Product(name, price));
                    }
                }
                productsLiveData.postValue(products);
            } catch (IOException e) {
                e.printStackTrace();
                productsLiveData.postValue(new ArrayList<>());
            }
        }).start();
    }

}