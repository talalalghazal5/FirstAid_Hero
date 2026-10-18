package com.example.firstaidhero;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.firstaidhero.database.DatabaseHelper;
import com.example.firstaidhero.models.EmergencyContact;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TreatedCaseDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 124;
    private DatabaseHelper dbHelper;
    private String pendingPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_treated_case_details);

        View toolbar = findViewById(R.id.toolbar_treated_details);
        if (toolbar != null && toolbar.getParent() instanceof View) {
            ViewCompat.setOnApplyWindowInsetsListener((View) toolbar.getParent(), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setSupportActionBar((Toolbar) toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        TextView tvTitle = findViewById(R.id.tv_treated_title);
        TextView tvDate = findViewById(R.id.tv_treated_date);
        TextView tvProcedures = findViewById(R.id.tv_treated_procedures);
        ImageView ivPhoto = findViewById(R.id.iv_treated_photo);

        int caseId = getIntent().getIntExtra("CASE_ID", -1);
        String title = getIntent().getStringExtra("CASE_TITLE");
        String date = getIntent().getStringExtra("CASE_DATE");
        String procedures = getIntent().getStringExtra("CASE_PROCEDURES");
        String photoPath = getIntent().getStringExtra("CASE_PHOTO");

        tvTitle.setText(title);
        tvDate.setText(date);
        tvProcedures.setText(procedures);

        if (photoPath != null && !photoPath.isEmpty()) {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                ivPhoto.setImageURI(Uri.fromFile(imgFile));
                ivPhoto.setVisibility(View.VISIBLE);
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        findViewById(R.id.btn_emergency_call_treated).setOnClickListener(v -> showEmergencyNumbersDialog());
    }

    private void showEmergencyNumbersDialog() {
        List<EmergencyContact> contacts = new ArrayList<>();
        // أرقام الطوارئ العامة
        contacts.add(new EmergencyContact(-1, getString(R.string.ambulance), "997"));
        contacts.add(new EmergencyContact(-2, getString(R.string.fire_department), "998"));

        // جلب الأرقام المخصصة من قاعدة البيانات
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM custom_contacts", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                contacts.add(new EmergencyContact(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
            cursor.close();
        }

        String[] displayItems = new String[contacts.size()];
        for (int i = 0; i < contacts.size(); i++) {
            displayItems[i] = contacts.get(i).getName() + " (" + contacts.get(i).getPhoneNumber() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("اختر رقم للاتصال")
                .setItems(displayItems, (dialog, which) -> {
                    makePhoneCall(contacts.get(which).getPhoneNumber());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void makePhoneCall(String phoneNumber) {
        pendingPhoneNumber = phoneNumber;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            makePhoneCall(pendingPhoneNumber);
        } else {
            Toast.makeText(this, "يجب منح صلاحية الاتصال لإجراء المكالمة", Toast.LENGTH_SHORT).show();
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
}
