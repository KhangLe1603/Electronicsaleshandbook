package com.example.customerlistapp.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.customerlistapp.models.Customer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.log4j.Logger;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

public class CustomerRepository {
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "KhachHang!B2:D";

    private final Sheets service;
    private final MutableLiveData<List<Customer>> customersLiveData = new MutableLiveData<>();
    private static final Logger logger = Logger.getLogger(CustomerRepository.class);

    public CustomerRepository(Context context) throws IOException, GeneralSecurityException {
        try {
            // 1. Tạo instance của JSON factory
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            // 2. Lấy thông tin xác thực
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                            context.getAssets().open("service_account.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

            // 3. Tạo HttpRequestInitializer từ credentials
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            // 4. Xây dựng dịch vụ Sheets
            service = new Sheets.Builder(
                    new NetHttpTransport(),
                    jsonFactory,
                    requestInitializer)
                    .setApplicationName("Customer List App")
                    .build();

            fetchCustomers();
        } catch (IOException e) {
            logger.error("Lỗi khi tạo dịch vụ Google Sheets", e);
            throw new IOException("Không tìm thấy file tài khoản dịch vụ trong assets", e);
        }
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

                if (values != null) {
                    for (List<Object> row : values) {
                        String name = !row.isEmpty() ? row.get(0).toString() : "";
                        String address = row.size() > 1 ? row.get(1).toString() : "";
                        String phone = row.size() > 2 ? row.get(2).toString() : "";
                        customers.add(new Customer(name, address, phone));
                    }
                }
                customersLiveData.postValue(customers);
            } catch (IOException e) {
                logger.error("Lỗi khi lấy danh sách khách hàng từ Google Sheets", e);
                customersLiveData.postValue(new ArrayList<>());
            }
        }).start();
    }
}