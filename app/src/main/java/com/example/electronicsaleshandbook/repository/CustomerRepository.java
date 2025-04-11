package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.Customer;
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
    private static final long MIN_REFRESH_INTERVAL = 2000; // Giới hạn 2 giây giữa các request

    private final Sheets service;
    private final MutableLiveData<List<Customer>> customersLiveData = new MutableLiveData<>();
    private List<Customer> cachedCustomers = null; // Cache dữ liệu khách hàng
    private long lastRefreshTime = 0;
    private boolean isRefreshing = false;
    private int requestCount = 0; // Đếm số request

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

            if (cachedCustomers == null) {
                fetchCustomersWithBackoff(0, 5); // Gọi lần đầu nếu cache trống
            } else {
                customersLiveData.postValue(cachedCustomers); // Dùng cache nếu đã có
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public LiveData<List<Customer>> getCustomers() {
        return customersLiveData;
    }

    public void refreshCustomers() {
        synchronized (this) {
            if (isRefreshing) {
                Log.d("CustomerRepository", "Skipping refresh, already refreshing");
                if (cachedCustomers != null) {
                    customersLiveData.postValue(cachedCustomers);
                }
                return;
            }
            // Chỉ làm mới nếu cache đã bị vô hiệu hóa
            if (cachedCustomers != null) {
                Log.d("CustomerRepository", "Using cached customers, size: " + cachedCustomers.size());
                customersLiveData.postValue(cachedCustomers);
                return;
            }
            isRefreshing = true;
            lastRefreshTime = System.currentTimeMillis();
        }
        fetchCustomersWithBackoff(0, 5);
        synchronized (this) {
            isRefreshing = false;
        }
    }

    public Sheets getSheetsService() {
        return service;
    }

    private void fetchCustomersWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                requestCount++;
                Log.d("CustomerRepository", "Sending request #" + requestCount);
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, "KhachHang!A2:I")
                        .execute();
                List<Customer> customers = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String id = row.size() > 1 ? row.get(1).toString() : "";
                        String surname = row.size() > 2 ? row.get(2).toString() : "";
                        String firstName = row.size() > 3 ? row.get(3).toString() : "";
                        String address = row.size() > 4 ? row.get(4).toString() : "";
                        String phone = row.size() > 5 ? row.get(5).toString() : "";
                        String email = row.size() > 6 ? row.get(6).toString() : "";
                        String birthday = row.size() > 7 ? row.get(7).toString() : "";
                        String gender = row.size() > 8 ? row.get(8).toString() : "";
                        Customer customer = new Customer(surname, firstName, address, phone, email, birthday, gender);
                        customer.setId(id);
                        customer.setSheetRowIndex(i + 2);
                        customers.add(customer);
                    }
                }
                cachedCustomers = customers;
                customersLiveData.postValue(customers);
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("CustomerRepository", "Quota exceeded, retrying in " + delay + "ms (attempt " + (attempt + 1) + "/" + maxAttempts + ")");
                    try {
                        Thread.sleep(delay);
                        fetchCustomersWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("CustomerRepository", "Interrupted during backoff", ie);
                        if (cachedCustomers != null) customersLiveData.postValue(cachedCustomers);
                    }
                } else {
                    Log.e("CustomerRepository", "Error fetching customers: " + e.getStatusCode(), e);
                    if (cachedCustomers != null) customersLiveData.postValue(cachedCustomers);
                }
            } catch (IOException e) {
                Log.e("CustomerRepository", "IO error fetching customers", e);
                if (cachedCustomers != null) customersLiveData.postValue(cachedCustomers);
            }
        }).start();
    }

    // Phương thức để vô hiệu hóa cache khi có thay đổi
    public void invalidateCache() {
        cachedCustomers = null;
        Log.d("CustomerRepository", "Customer cache invalidated");
    }
}