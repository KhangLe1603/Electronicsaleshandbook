package com.example.customerlistapp.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.customerlistapp.models.Customer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerRepository {
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "KhachHang!B2:E"; // Đọc từ B2 tới E

    private final Sheets service;
    private final MutableLiveData<List<Customer>> customersLiveData = new MutableLiveData<>();

    public CustomerRepository(Context context) throws IOException, GeneralSecurityException {
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        context.getAssets().open("service_account.json"))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        HttpCredentialsAdapter requestInitializer = new HttpCredentialsAdapter(credentials);

        service = new Sheets.Builder(
                new NetHttpTransport(),
                jsonFactory,
                requestInitializer)
                .setApplicationName("Customer List App")
                .build();

        fetchCustomers();
    }

    public LiveData<List<Customer>> getCustomers() {
        return customersLiveData;
    }

    public void refreshCustomers() {
        fetchCustomers();
    }

    private void fetchCustomers() {
        new Thread(() -> {
            try {
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, RANGE)
                        .execute();

                List<Customer> customers = new ArrayList<>();
                List<List<Object>> values = response.getValues();

                if (values != null && !values.isEmpty()) {
                    for (List<Object> row : values) {
                        // Gán dữ liệu trực tiếp từ các cột
                        String surname = row.size() > 0 && row.get(0) != null ? row.get(0).toString() : "";   // Cột B
                        String firstName = row.size() > 1 && row.get(1) != null ? row.get(1).toString() : ""; // Cột C
                        String address = row.size() > 2 && row.get(2) != null ? row.get(2).toString() : "";   // Cột D
                        String phone = row.size() > 3 && row.get(3) != null ? row.get(3).toString() : "";     // Cột E

                        // Tạo đối tượng Customer
                        Customer customer = new Customer(surname, firstName, address, phone);
                        customers.add(customer);
                    }
                }
                customersLiveData.postValue(customers);
            } catch (IOException e) {
                e.printStackTrace();
                customersLiveData.postValue(new ArrayList<>());
            }
        }).start();
    }
}