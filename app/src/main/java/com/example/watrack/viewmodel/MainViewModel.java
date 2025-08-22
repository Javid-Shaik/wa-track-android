package com.example.watrack.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.watrack.data.TrackerRepository;
import com.example.watrack.model.ActivityLog;
import com.example.watrack.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final TrackerRepository repository = new TrackerRepository();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<FilterType> filter = new MutableLiveData<>(FilterType.ALL);

    private final MediatorLiveData<List<Contact>> filteredContacts = new MediatorLiveData<>();

    public enum FilterType {
        ALL,
        ONLINE,
        OFFLINE,
        GROUPS // reserved for future
    }

    public MainViewModel() {
        filteredContacts.addSource(repository.getContacts(), contacts -> combineFilters());
        filteredContacts.addSource(searchQuery, query -> combineFilters());
        filteredContacts.addSource(filter, type -> combineFilters());
    }

    private void combineFilters() {
        List<Contact> baseList = repository.getContacts().getValue();
        if (baseList == null) baseList = new ArrayList<>();

        String query = searchQuery.getValue() != null ? searchQuery.getValue().toLowerCase() : "";
        FilterType type = filter.getValue() != null ? filter.getValue() : FilterType.ALL;

        List<Contact> result = new ArrayList<>();
        for (Contact c : baseList) {
            boolean matchesSearch = c.getName().toLowerCase().contains(query)
                    || c.getLastSeen().toLowerCase().contains(query)
                    || c.getDuration().toLowerCase().contains(query);

            boolean matchesFilter = false;

            if (type == FilterType.ALL) {
                matchesFilter = true;
            } else if (type == FilterType.ONLINE) {
                matchesFilter = c.isOnline();
            } else if (type == FilterType.OFFLINE) {
                matchesFilter = !c.isOnline();
            } else if (type == FilterType.GROUPS) {
                // TODO: handle group logic later
                matchesFilter = false;
            }

            if (matchesSearch && matchesFilter) {
                result.add(c);
            }
        }

        filteredContacts.setValue(result);
    }

    // Expose LiveData
    public LiveData<List<Contact>> getContacts() {
        return filteredContacts;
    }

    public LiveData<List<ActivityLog>> getActivity() {
        return repository.getActivity();
    }

    // Mutators
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setFilter(FilterType type) {
        filter.setValue(type);
    }

    public void addContact(Contact contact) {
        repository.addContact(contact);
    }
}
