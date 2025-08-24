package com.example.watrack.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.watrack.data.TrackerRepository;
import com.example.watrack.model.ActivityLog;
import com.example.watrack.model.Contact;
import com.example.watrack.model.SessionResponse;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends ViewModel {

    public enum Filter { ONLINE, OFFLINE, ALL }

    private final TrackerRepository repo = new TrackerRepository();
    private final MediatorLiveData<List<Contact>> filtered = new MediatorLiveData<>();
    private Filter current = Filter.ALL;

    public DashboardViewModel() {
        filtered.addSource(repo.getContacts(), contacts -> applyFilter(contacts, current));
    }

    public LiveData<List<Contact>> getContacts() { return filtered; }
    public LiveData<List<ActivityLog>> getActivity() { return repo.getActivity(); }

    public void setFilter(Filter f) {
        current = f;
        List<Contact> c = repo.getContacts().getValue();
        applyFilter(c, f);
    }

    private void applyFilter(List<Contact> src, Filter f) {
        if (src == null) { filtered.setValue(new ArrayList<>()); return; }
        List<Contact> out = new ArrayList<>();
        for (Contact c : src) {
            if (f == Filter.ONLINE && c.isOnline()) out.add(c);
            else if (f == Filter.OFFLINE && !c.isOnline()) out.add(c);
            else if (f == Filter.ALL) out.add(c);
        }
        filtered.setValue(out);
    }

    public void addContact(Contact contact) { repo.addContact(contact); }

    public LiveData<SessionResponse> getLinkedUser() {
        return repo.getLinkedUser();
    }

    public void loadLinkedUser(String firebaseUid) {
        repo.fetchLinkedUser(firebaseUid);
    }

}
