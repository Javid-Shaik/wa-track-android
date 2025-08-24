package com.example.watrack.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.watrack.model.ActivityLog;
import com.example.watrack.model.ActivityLogResponse;
import com.example.watrack.model.Contact;
import com.example.watrack.model.ContactResponse;
import com.example.watrack.model.SessionResponse;
import com.example.watrack.repository.ApiClient;
import com.example.watrack.repository.ApiService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackerRepository {

    private final ApiService apiService;
    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityLog>> activity = new MutableLiveData<>();

    private final MutableLiveData<SessionResponse> linkedUser = new MutableLiveData<>();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String firebaseUid = auth.getCurrentUser().getUid();

    public LiveData<SessionResponse> getLinkedUser() {
        return linkedUser;
    }

    public void fetchLinkedUser(String firebaseUid) {
        apiService.getSessionByUser(firebaseUid).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("TrackerRepository", "Fetched linked user: " + response.body().toString());
                    linkedUser.postValue(response.body());
                } else {
                    Log.e("TrackerRepository", "Failed to fetch linked user: " + response.code());
                    linkedUser.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Log.e("TrackerRepository", "Error fetching linked user", t);
                linkedUser.postValue(null);
            }
        });
    }


    public TrackerRepository() {
        apiService = ApiClient.getApiService();
        fetchContacts(firebaseUid);
        fetchActivityLogs();
    }

    private void fetchContacts(String firebaseUid) {
        apiService.getContacts(firebaseUid).enqueue(new Callback<ContactResponse>() {
            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    contacts.postValue(response.body().getContacts());
                    Log.d("TrackerRepository", "Fetched contacts: " + response.body().toString());
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
                    fetchContacts(firebaseUid);
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