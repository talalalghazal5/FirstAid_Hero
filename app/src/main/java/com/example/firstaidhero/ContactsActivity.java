package com.example.firstaidhero;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.adapters.ContactsAdapter;
import com.example.firstaidhero.database.DatabaseHelper;
import com.example.firstaidhero.models.EmergencyContact;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactActionListener {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private DatabaseHelper dbHelper;
    private ContactsAdapter adapter;
    private List<EmergencyContact> contactList;
    private String tempPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacts);

        View toolbarView = findViewById(R.id.toolbar_contacts);
        if (toolbarView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        contactList = new ArrayList<>();

        Toolbar toolbar = (Toolbar) toolbarView;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.rv_custom_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contactList, this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_call_ambulance).setOnClickListener(v -> onCallClick("997"));
        findViewById(R.id.btn_call_fire).setOnClickListener(v -> onCallClick("998"));
        findViewById(R.id.btn_add_contact).setOnClickListener(v -> showAddContactDialog());

        setupBottomNavigation();
        loadContacts();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_contacts);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_contacts) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadContacts() {
        contactList.clear();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM custom_contacts", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                contactList.add(new EmergencyContact(id, name, phone));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddContactDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText etName = view.findViewById(R.id.et_contact_name);
        EditText etPhone = view.findViewById(R.id.et_contact_phone);

        new AlertDialog.Builder(this)
                .setTitle("إضافة جهة اتصال طوارئ")
                .setView(view)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        dbHelper.addContact(name, phone);
                        loadContacts();
                    } else {
                        Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public void onCallClick(String phoneNumber) {
        tempPhoneNumber = phoneNumber;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            makeCall(phoneNumber);
        }
    }

    @Override
    public void onEditClick(EmergencyContact contact) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText etName = view.findViewById(R.id.et_contact_name);
        EditText etPhone = view.findViewById(R.id.et_contact_phone);

        etName.setText(contact.getName());
        etPhone.setText(contact.getPhoneNumber());

        new AlertDialog.Builder(this)
                .setTitle("تعديل جهة اتصال")
                .setView(view)
                .setPositiveButton("تحديث", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        dbHelper.updateContact(contact.getId(), name, phone);
                        loadContacts();
                    } else {
                        Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public void onDeleteClick(EmergencyContact contact) {
        new AlertDialog.Builder(this)
                .setTitle("حذف جهة اتصال")
                .setMessage("هل أنت متأكد من حذف " + contact.getName() + "؟")
                .setPositiveButton("حذف", (dialog, which) -> {
                    dbHelper.deleteContact(contact.getId());
                    loadContacts();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void makeCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makeCall(tempPhoneNumber);
        } else {
            Toast.makeText(this, "عذراً، يجب تفعيل صلاحية الاتصال لإجراء المكالمة", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_contacts);
    }
}
