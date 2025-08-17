package com.example.watrack.viewmodel;

import android.app.Application;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.watrack.model.QrResponse;
import com.example.watrack.model.SessionResponse;
import com.example.watrack.repository.ApiClient;
import com.example.watrack.repository.ApiService;
import com.example.watrack.util.SessionPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrLinkViewModel extends AndroidViewModel {

    private final ApiService api;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<QrResponse> qrData = new MutableLiveData<>();
    private final MutableLiveData<String> uiStatus = new MutableLiveData<>("Requesting QR…");
    private final MutableLiveData<Long> countdown = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> linked = new MutableLiveData<>(false);

    private CountDownTimer timer;
    private String sessionId;

    private static final long QR_VALIDITY_MS = TimeUnit.MINUTES.toMillis(1); // ~60s

    public QrLinkViewModel(@NonNull Application app) {
        super(app);
        api = ApiClient.getApiService();
        sessionId = SessionPrefs.getSessionId(app);
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<QrResponse> getQrData() { return qrData; }
    public LiveData<String> getUiStatus() { return uiStatus; }
    public LiveData<Long> getCountdown() { return countdown; }
    public LiveData<Boolean> getLinked() { return linked; }

    public void fetchQr() {
        loading.setValue(true);
        uiStatus.setValue("Fetching QR…");

        if (sessionId == null) {
            createSession();
        } else {
            requestQr();
        }
    }

    private void createSession() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            uiStatus.setValue("User not authenticated");
            return;
        }

        api.createSession(Collections.singletonMap("firebaseUid", user.getUid())).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessionId = response.body().getSessionId();
                    SessionPrefs.saveSessionId(getApplication(), sessionId);
                    startClient();
                } else {
                    loading.setValue(false);
                    uiStatus.setValue("Failed to create session");
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                loading.setValue(false);
                uiStatus.setValue("Error: " + t.getMessage());
            }
        });
    }

    private void startClient() {
        api.startClient(Collections.singletonMap("sessionId", sessionId))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            requestQr();
                        } else {
                            loading.setValue(false);
                            uiStatus.setValue("Failed to start client");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        loading.setValue(false);
                        uiStatus.setValue("Error: " + t.getMessage());
                    }
                });
    }

    private void requestQr() {
        api.getQr2(sessionId).enqueue(new Callback<QrResponse>() {
            @Override
            public void onResponse(Call<QrResponse> call, Response<QrResponse> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    QrResponse data = response.body();
                    qrData.setValue(data);

                    if (data.getQr() != null) {
                        uiStatus.setValue("Scan this QR in WhatsApp");
                        startExpireTimer(QR_VALIDITY_MS);
                        pollStatus();
                    } else {
                        uiStatus.setValue("No QR available");
                    }
                } else {
                    uiStatus.setValue("Failed to fetch QR");
                }
            }

            @Override
            public void onFailure(Call<QrResponse> call, Throwable t) {
                loading.setValue(false);
                uiStatus.setValue("Error: " + t.getMessage());
            }
        });
    }

    private void pollStatus() {
        new Handler().postDelayed(() -> {
            api.getUserSession(sessionId).enqueue(new Callback<SessionResponse>() {
                @Override
                public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if ("linked".equalsIgnoreCase(response.body().getStatus())) {
                            SessionPrefs.saveSessionId(getApplication(), sessionId);
                            onConnected();
                        } else {
                            pollStatus(); // keep polling until connected
                        }
                    }
                }

                @Override
                public void onFailure(Call<SessionResponse> call, Throwable t) {
                    // just retry later
                    pollStatus();
                }
            });
        }, 5000);
    }

    private void startExpireTimer(long duration) {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(duration, 1000) {
            public void onTick(long ms) { countdown.postValue(ms); }
            public void onFinish() { countdown.postValue(0L); }
        };
        timer.start();
    }

    private void onConnected() {
        if (timer != null) timer.cancel();
        uiStatus.setValue("✅ Linked successfully!");
        linked.setValue(true);
    }

    @Override
    protected void onCleared() {
        if (timer != null) timer.cancel();
        super.onCleared();
    }
}
