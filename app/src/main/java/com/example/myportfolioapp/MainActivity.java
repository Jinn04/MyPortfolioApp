package com.example.myportfolioapp;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Declare UI components
    EditText editName, editEmail, editPhone, editAboutMe, editAddress, editSkills, editWebsite;
    Button btnSave, btnDelete, btnSearch;
    ImageView profileImage;
    ListView nameListView;
    SearchView searchName;

    // For data persistence
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "MyPortfolioData";
    private static final String DATA_SAVED_KEY = "dataSaved";
    private static final String NAME_LIST_KEY = "nameList";
    private static final String NAME_DATA_PREFIX = "nameData_";

    private ArrayAdapter<String> adapter;
    private ArrayList<String> nameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAboutMe = findViewById(R.id.editAboutMe);
        editAddress = findViewById(R.id.editAddress);
        editSkills = findViewById(R.id.editSkills);
        editWebsite = findViewById(R.id.editWebsite);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnSearch = findViewById(R.id.btnSearch);
        nameListView = findViewById(R.id.nameListView);
        searchName = findViewById(R.id.searchName);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // Initialize ListView
        nameList = new ArrayList<>(sharedPreferences.getStringSet(NAME_LIST_KEY, new HashSet<>()));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, nameList);
        nameListView.setAdapter(adapter);

        // Load saved data if it has been previously saved
        if (sharedPreferences.getBoolean(DATA_SAVED_KEY, false)) {
            loadSavedData();
        }

        // Set a click listener on the save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        // Set a click listener on the delete button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedName();
            }
        });

        // Set a click listener on the search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAndDisplayData();
            }
        });

        // Set up search functionality
        searchName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAndDisplayData();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void loadSavedData() {
        editName.setText(sharedPreferences.getString("name", ""));
        editEmail.setText(sharedPreferences.getString("email", ""));
        editPhone.setText(sharedPreferences.getString("phone", ""));
        editAboutMe.setText(sharedPreferences.getString("aboutMe", ""));
        editAddress.setText(sharedPreferences.getString("address", ""));
        editSkills.setText(sharedPreferences.getString("skills", ""));
        editWebsite.setText(sharedPreferences.getString("website", ""));
    }

    private void saveData() {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String phone = editPhone.getText().toString();
        String aboutMe = editAboutMe.getText().toString();
        String address = editAddress.getText().toString();
        String skills = editSkills.getText().toString();
        String website = editWebsite.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("aboutMe", aboutMe);
        editor.putString("address", address);
        editor.putString("skills", skills);
        editor.putString("website", website);
        editor.putBoolean(DATA_SAVED_KEY, true);
        editor.apply();

        // Save data to a unique key based on the name
        SharedPreferences.Editor nameEditor = sharedPreferences.edit();
        nameEditor.putString(NAME_DATA_PREFIX + name, email + "," + phone + "," + aboutMe + "," + address + "," + skills + "," + website);
        nameEditor.apply();

        // Update the list of names
        Set<String> names = new HashSet<>(sharedPreferences.getStringSet(NAME_LIST_KEY, new HashSet<>()));
        names.add(name);
        editor.putStringSet(NAME_LIST_KEY, names);
        editor.apply();

        // Update ListView
        nameList.clear();
        nameList.addAll(names);
        adapter.notifyDataSetChanged();

        Toast.makeText(MainActivity.this,
                "Data Saved!\nName: " + name + "\nEmail: " + email + "\nPhone: " + phone + "\nAbout Me: " + aboutMe + "\nAddress: " + address + "\nSkills: " + skills + "\nWebsite: " + website,
                Toast.LENGTH_LONG).show();
    }

    private void deleteSelectedName() {
        int position = nameListView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            String nameToDelete = nameList.get(position);

            // Remove from SharedPreferences
            Set<String> names = new HashSet<>(sharedPreferences.getStringSet(NAME_LIST_KEY, new HashSet<>()));
            names.remove(nameToDelete);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(NAME_LIST_KEY, names);
            editor.remove(NAME_DATA_PREFIX + nameToDelete); // Also remove associated data
            editor.apply();

            // Remove from ListView
            nameList.remove(position);
            adapter.notifyDataSetChanged();
            nameListView.clearChoices();
            Toast.makeText(MainActivity.this, "Name Deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Select a name to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchAndDisplayData() {
        String query = searchName.getQuery().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter a name to search", Toast.LENGTH_SHORT).show();
            return;
        }

        String data = sharedPreferences.getString(NAME_DATA_PREFIX + query, null);
        if (data != null) {
            String[] details = data.split(",");
            if (details.length == 6) {
                editEmail.setText(details[0]);
                editPhone.setText(details[1]);
                editAboutMe.setText(details[2]);
                editAddress.setText(details[3]);
                editSkills.setText(details[4]);
                editWebsite.setText(details[5]);
                editName.setText(query);
            } else {
                Toast.makeText(MainActivity.this, "Data format is incorrect", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No data found for the name", Toast.LENGTH_SHORT).show();
        }
    }
}