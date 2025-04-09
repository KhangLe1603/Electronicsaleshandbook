package com.example.customerlistapp.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.customerlistapp.models.Customer;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerRepository {
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "KhachHang!B2:H"; // Đọc từ B2 tới H

    private final Sheets service;
    private final MutableLiveData<List<Customer>> customersLiveData = new MutableLiveData<>();

    public CustomerRepository(Context context) throws IOException, GeneralSecurityException {
        try {
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                            context.getAssets().open("service_account.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            HttpCredentialsAdapter requestInitializer = new HttpCredentialsAdapter(credentials);

            service = new Sheets.Builder(
                    new NetHttpTransport(),
                    jsonFactory,
                    requestInitializer)
                    .setApplicationName("Customer List App")
                    .build();

            fetchCustomersWithBackoff(0, 5); // Gọi với backoff ngay khi khởi tạo
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public LiveData<List<Customer>> getCustomers() {
        return customersLiveData;
    }

    public void refreshCustomers() {
        fetchCustomersWithBackoff(0, 5); // Gọi lại với backoff khi làm mới
    }

    public Sheets getSheetsService() {
        return service;
    }

    private void fetchCustomersWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, RANGE)
                        .execute();
                List<Customer> customers = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null && !values.isEmpty()) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String surname = row.size() > 0 && row.get(0) != null ? row.get(0).toString() : "";
                        String firstName = row.size() > 1 && row.get(1) != null ? row.get(1).toString() : "";
                        String address = row.size() > 2 && row.get(2) != null ? row.get(2).toString() : "";
                        String phone = row.size() > 3 && row.get(3) != null ? row.get(3).toString() : "";
                        String email = row.size() > 4 && row.get(4) != null ? row.get(4).toString() : "";
                        String birthday = row.size() > 5 && row.get(5) != null ? row.get(5).toString() : "";
                        String gender = row.size() > 6 && row.get(6) != null ? row.get(6).toString() : "";
                        Customer customer = new Customer(surname, firstName, address, phone, email, birthday, gender);
                        customer.setSheetRowIndex(i + 2);
                        customers.add(customer);
                    }
                }
                Log.d("CustomerRepository", "Fetched customers, size: " + customers.size());
                customersLiveData.postValue(customers);
            } catch (GoogleJsonResponseException e) {
                Log.e("CustomerRepository", "Google API error: " + e.getStatusCode(), e);
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("CustomerRepository", "Quota exceeded, retrying in " + delay + "ms");
                    try {
                        Thread.sleep(delay);
                        fetchCustomersWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("CustomerRepository", "Interrupted during backoff", ie);
                        customersLiveData.postValue(customersLiveData.getValue());
                    }
                } else {
                    customersLiveData.postValue(customersLiveData.getValue());
                }
            } catch (IOException e) {
                Log.e("CustomerRepository", "IO error fetching customers", e);
                if (attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("CustomerRepository", "Retrying in " + delay + "ms");
                    try {
                        Thread.sleep(delay);
                        fetchCustomersWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("CustomerRepository", "Interrupted during retry", ie);
                        customersLiveData.postValue(customersLiveData.getValue());
                    }
                } else {
                    customersLiveData.postValue(customersLiveData.getValue());
                }
            }
        }).start();
    }
}