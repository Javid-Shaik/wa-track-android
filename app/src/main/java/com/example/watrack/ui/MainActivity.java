package com.example.watrack.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.watrack.R;
import com.example.watrack.adapter.ActivityAdapter;
import com.example.watrack.adapter.ContactAdapter;
import com.example.watrack.databinding.ActivityMainBinding;
import com.example.watrack.model.Contact;
import com.example.watrack.viewmodel.DashboardViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;
    private DashboardViewModel vm;
    private ContactAdapter contactAdapter;
    private ActivityAdapter activityAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Handle toolbar menu clicks
        b.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_notifications) {
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_settings) {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Contacts
        contactAdapter = new ContactAdapter(new ContactAdapter.Listener() {
            @Override public void onClick(Contact c) {
                Toast.makeText(MainActivity.this, c.getName(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onMore(View anchor, Contact c) {
                Toast.makeText(MainActivity.this, "More: "+c.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        b.recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerContacts.setAdapter(contactAdapter);

        // Activity
        activityAdapter = new ActivityAdapter();
        b.recyclerActivity.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerActivity.setAdapter(activityAdapter);

        // Observe data
        vm.getContacts().observe(this, contactAdapter::submitList);
        vm.getActivity().observe(this, activityAdapter::submitList);

        // Filter chips
        Chip chipOnline = b.chipOnline;
        Chip chipOffline = b.chipOffline;

        b.chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) { vm.setFilter(DashboardViewModel.Filter.ALL); return; }
            int id = ids.get(0);
            if (id == chipOnline.getId()) vm.setFilter(DashboardViewModel.Filter.ONLINE);
            else if (id == chipOffline.getId()) vm.setFilter(DashboardViewModel.Filter.OFFLINE);
        });

        // FAB - now shows a dialog for user input
        b.fabAdd.setOnClickListener(v -> showAddContactDialog());

        // Bottom nav
        b.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId(); // Get the ID once

                if (itemId == R.id.nav_dashboard) {
                    return true;
                } else if (itemId == R.id.nav_contacts) {
                    Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_reports) {
                    Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void showAddContactDialog() {
        // Inflate the new layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.add_contact_dialog, null);

        TextInputEditText etPhoneNumber = dialogView.findViewById(R.id.etPhoneNumber);
        CountryCodePicker ccp = dialogView.findViewById(R.id.countryCodePicker);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        // Get the buttons from the dialog view
        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String countryCode = ccp.getSelectedCountryCodeWithPlus();
            String phoneNumber = Objects.requireNonNull(etPhoneNumber.getText()).toString();
            String fullPhoneNumber = countryCode + phoneNumber;

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            } else {
                // Add the new contact with the entered phone number
                vm.addContact(new Contact(
                        fullPhoneNumber,
                        R.drawable.ic_person_circle,
                        "Last seen just now",
                        true,
                        "0 min"
                ));
                b.recyclerContacts.smoothScrollToPosition(0);
                Toast.makeText(this, "Adding " + fullPhoneNumber, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean onTopMenuItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
