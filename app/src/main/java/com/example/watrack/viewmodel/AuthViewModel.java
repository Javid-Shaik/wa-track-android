package com.example.watrack.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.watrack.model.SessionResponse;
import com.example.watrack.repository.ApiClient;
import com.example.watrack.repository.ApiService;
import com.example.watrack.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Response<Void>> registerResponseLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        apiService = ApiClient.getApiService();
    }

    public LiveData<Response<Void>> registerUser(String firebaseIdToken, String deviceToken) {
        RegisterRequest request = new RegisterRequest(firebaseIdToken, deviceToken, 10);

        Call<Void> call = apiService.registerUser(request);
        // Log the URL before making the request
        Log.d("AuthViewModel", "Request URL: " + call.request().url());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Log the URL and response details
                Log.d("AuthViewModel", "Request URL: " + call.request().url());
                Log.d("AuthViewModel", "Response code: " + response.code());
                Log.d("AuthViewModel", "Response message: " + response.message());
                registerResponseLiveData.postValue(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Log the URL and error
                Log.e("AuthViewModel", "Request URL: " + call.request().url());
                Log.e("AuthViewModel", "API call failed: " + t.getMessage(), t);
                registerResponseLiveData.postValue(null);
            }
        });

        return registerResponseLiveData;
    }

    public LiveData<SessionResponse> getUserSession(String firebaseUid) {
        MutableLiveData<SessionResponse> data = new MutableLiveData<>();
        apiService.getSessionByUser(firebaseUid).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

}