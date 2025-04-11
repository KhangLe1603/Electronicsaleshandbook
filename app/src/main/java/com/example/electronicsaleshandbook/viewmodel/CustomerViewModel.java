package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.Customer;
import com.example.electronicsaleshandbook.repository.CustomerRepository;
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

public class CustomerViewModel extends ViewModel {
    private final CustomerRepository repository;
    private final LiveData<List<Customer>> allCustomers;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> sortOption = new MutableLiveData<>(0);

    public CustomerViewModel(Context context) {
        try {
            repository = new CustomerRepository(context);
            allCustomers = repository.getCustomers();
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

    public LiveData<List<Customer>> getFilteredCustomers() {
        return Transformations.switchMap(searchQuery, query ->
                Transformations.switchMap(sortOption, sort ->
                        Transformations.map(allCustomers, customers -> {
                            if (customers == null) {
                                return null; // Trả về null nếu không có dữ liệu
                            }
                            // Khai báo rõ ràng filteredList là List<Customer>
                            List<Customer> filteredList = customers;
                            // Lọc theo từ khóa tìm kiếm (nếu có)
                            if (query != null && !query.isEmpty()) {
                                filteredList = filteredList.stream()
                                        .filter(customer ->
                                                (customer.getFirstName() != null && customer.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                                                        (customer.getSurname() != null && customer.getSurname().toLowerCase().contains(query.toLowerCase())) ||
                                                        (customer.getAddress() != null && customer.getAddress().toLowerCase().contains(query.toLowerCase())) ||
                                                        (customer.getPhone() != null && customer.getPhone().toLowerCase().contains(query.toLowerCase())))
                                        .collect(Collectors.toList());
                            }

                            // Sắp xếp theo tiêu chí
                            switch (sort) {
                                case 0: // Tên A-Z (dùng firstName)
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Customer::getFirstName, Comparator.nullsLast(String::compareTo)))
                                            .collect(Collectors.toList());
                                case 1: // Tên Z-A (dùng firstName)
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Customer::getFirstName, Comparator.nullsLast(String::compareTo).reversed()))
                                            .collect(Collectors.toList());
                                case 2: // Theo địa chỉ (A-Z)
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Customer::getAddress, Comparator.nullsLast(String::compareTo)))
                                            .collect(Collectors.toList());
                                default:
                                    return filteredList; // Không sắp xếp
                            }
                        })));
    }

    public void refreshCustomers() {
        Log.d("CustomerViewModel", "Refreshing customer...");
            searchQuery.postValue("");
            sortOption.postValue(0);
            repository.invalidateCache();
            repository.refreshCustomers();
    }


    public void addCustomer(String surname, String firstName, String address, String phone,
                            String email, String birthday, String gender) {
        new Thread(() -> {
            try {
                // Lấy dữ liệu từ cột B để đếm số dòng (không cần cột A vì STT tự động)
                ValueRange existingData = repository.getSheetsService().spreadsheets().values()
                        .get("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", "KhachHang!B2:B")
                        .execute();
                int lastRow = existingData.getValues() != null ? existingData.getValues().size() + 1 : 1;
                String newId = String.format("KH%03d", lastRow + 1); // Tạo mã KHxxx

                // Phạm vi: B<row>:I<row> (bỏ cột A)
                String range = "KhachHang!B" + (lastRow + 1) + ":I" + (lastRow + 1);

                // Chuẩn bị dữ liệu: MÃ KH, Họ và đệm, Tên, ...
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(newId, surname, firstName, address, phone, email, birthday, gender)
                        ));

                repository.getSheetsService().spreadsheets().values()
                        .update("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", range, body)
                        .setValueInputOption("RAW")
                        .execute();

                Thread.sleep(2000);
                Log.d("CustomerViewModel", "Customer added successfully with ID: " + newId + " at row " + (lastRow + 1));
            } catch (IOException e) {
                Log.e("CustomerViewModel", "Error adding customer", e);
            } catch (InterruptedException e) {
                Log.w("CustomerViewModel", "Thread interrupted while waiting", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void updateCustomer(int sheetRowIndex, String surname, String firstName, String address,
                               String phone, String email, String birthday, String gender) {
        new Thread(() -> {
            try {
                // Chỉ cập nhật từ cột C (Họ và đệm) đến I (Giới Tính), giữ nguyên STT và MÃ KH
                String range = "KhachHang!C" + sheetRowIndex + ":I" + sheetRowIndex;
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(surname, firstName, address, phone, email, birthday, gender)
                        ));

                repository.getSheetsService().spreadsheets().values()
                        .update("1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A", range, body)
                        .setValueInputOption("RAW")
                        .execute();

                Thread.sleep(2000);
                Log.d("CustomerViewModel", "Customer updated successfully at row " + sheetRowIndex);
            } catch (IOException e) {
                Log.e("CustomerViewModel", "Error updating customer", e);
            } catch (InterruptedException e) {
                Log.w("CustomerViewModel", "Thread interrupted while waiting", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void deleteCustomer(int sheetRowIndex) {
        new Thread(() -> {
            try {
                int adjustedRowIndex = sheetRowIndex - 1;
                DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                        .setRange(new DimensionRange()
                                .setSheetId(341227420)
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

                Thread.sleep(2000);
                Log.d("CustomerViewModel", "Customer deleted successfully at row " + sheetRowIndex);
            } catch (IOException e) {
                Log.e("CustomerViewModel", "Error deleting customer", e);
            } catch (InterruptedException e) {
                Log.w("CustomerViewModel", "Thread interrupted while waiting", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}