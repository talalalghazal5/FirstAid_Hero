package com.example.firstaidhero;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.adapters.EmergencyAdapter;
import com.example.firstaidhero.database.DatabaseHelper;
import com.example.firstaidhero.models.EmergencyCase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EmergencyAdapter.OnCaseClickListener {

    private DatabaseHelper dbHelper;
    private EmergencyAdapter adapter;
    private List<EmergencyCase> casesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تفعيل وضع Edge-to-Edge برمجياً
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ضبط الحاشية العلوية لتجنب التداخل مع شريط الحالة
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        casesList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.rv_emergencies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyAdapter(casesList, this);
        recyclerView.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadData(newText);
                return true;
            }
        });

        setupBottomNavigation();
        loadData("");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_contacts) {
                startActivity(new Intent(this, ContactsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadData(String query) {
        casesList.clear();
        Cursor cursor = dbHelper.searchEmergencies(query);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("case_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String danger = cursor.getString(cursor.getColumnIndexOrThrow("danger_level"));
                String symptoms = cursor.getString(cursor.getColumnIndexOrThrow("symptoms"));
                casesList.add(new EmergencyCase(id, title, danger, symptoms));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.updateList(casesList);
    }

    @Override
    public void onCaseClick(EmergencyCase emergencyCase) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("CASE_ID", emergencyCase.getId());
        intent.putExtra("CASE_TITLE", emergencyCase.getTitle());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
