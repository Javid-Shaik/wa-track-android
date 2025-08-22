package com.example.watrack.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.watrack.model.ActivityLog;
import com.example.watrack.model.ActivityLogResponse;
import com.example.watrack.model.Contact;
import com.example.watrack.model.ContactResponse;
import com.example.watrack.repository.ApiClient;
import com.example.watrack.repository.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackerRepository {

    private final ApiService apiService;
    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityLog>> activity = new MutableLiveData<>();

    public TrackerRepository() {
        apiService = ApiClient.getApiService();
        fetchContacts();
        fetchActivityLogs();
    }

    private void fetchContacts() {
        apiService.getContacts().enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    contacts.postValue(response.body().getContacts());
                } else {
                    Log.e("TrackerRepository", "Failed to fetch contacts: " + response.code());
                    contacts.postValue(null); // Or an empty list
                }
            }

            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                Log.e("TrackerRepository", "Error fetching contacts", t);
                contacts.postValue(null); // Or an empty list
            }
        });
    }

    private void fetchActivityLogs() {
        // NOTE: In a real app, you might fetch activity logs for a specific user
        // so you'd need to pass an ID here. For this example, we'll assume a general endpoint.
        apiService.getActivityLogs("user_id_here").enqueue(new Callback<ActivityLogResponse>() {
            @Override
            public void onResponse(Call<ActivityLogResponse> call, Response<ActivityLogResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activity.postValue(response.body().getActivityLogs());
                } else {
                    Log.e("TrackerRepository", "Failed to fetch activity logs: " + response.code());
                    activity.postValue(null); // Or an empty list
                }
            }

            @Override
            public void onFailure(Call<ActivityLogResponse> call, Throwable t) {
                Log.e("TrackerRepository", "Error fetching activity logs", t);
                activity.postValue(null); // Or an empty list
            }
        });
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }

    public LiveData<List<ActivityLog>> getActivity() {
        return activity;
    }

    public void addContact(Contact contact) {
        apiService.addContact(contact).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("TrackerRepository", "Contact added successfully!");
                    // Re-fetch the data to update the UI with the new contact from the backend
                    fetchContacts();
                } else {
                    Log.e("TrackerRepository", "Failed to add contact: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("TrackerRepository", "Error adding contact", t);
            }
        });
    }
}