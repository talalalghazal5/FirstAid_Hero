package com.example.firstaidhero;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.adapters.StepsAdapter;
import com.example.firstaidhero.database.DatabaseHelper;
import com.example.firstaidhero.models.EmergencyContact;
import com.example.firstaidhero.models.FirstAidStep;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 123;
    private DatabaseHelper dbHelper;
    private int caseId;
    private String caseTitle;
    private String pendingPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        View toolbar = findViewById(R.id.toolbar_details);
        if (toolbar != null && toolbar.getParent() instanceof View) {
            ViewCompat.setOnApplyWindowInsetsListener((View) toolbar.getParent(), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        caseId = getIntent().getIntExtra("CASE_ID", -1);
        caseTitle = getIntent().getStringExtra("CASE_TITLE");

        setSupportActionBar((Toolbar) toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(caseTitle);
        }

        dbHelper = new DatabaseHelper(this);
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvDangerLevel = findViewById(R.id.tv_detail_danger_level);
        TextView tvSymptoms = findViewById(R.id.tv_detail_symptoms);
        RecyclerView rvSteps = findViewById(R.id.rv_steps);

        tvTitle.setText(caseTitle);
        loadCaseDetails(tvDangerLevel, tvSymptoms, rvSteps);

        findViewById(R.id.btn_emergency_call_detail).setOnClickListener(v -> showEmergencyNumbersDialog());
    }

    private void loadCaseDetails(TextView tvDangerLevel, TextView tvSymptoms, RecyclerView rvSteps) {
        Cursor caseCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT symptoms, danger_level FROM emergencies WHERE case_id = ?",
                new String[]{String.valueOf(caseId)});
        
        if (caseCursor != null && caseCursor.moveToFirst()) {
            String symptoms = caseCursor.getString(0);
            String dangerLevel = caseCursor.getString(1);
            
            tvSymptoms.setText("الأعراض: " + symptoms);
            tvDangerLevel.setText("مستوى الخطورة: " + dangerLevel);

            if (dangerLevel != null) {
                if (dangerLevel.contains("حرِج") || dangerLevel.contains("حرج")) {
                    tvDangerLevel.setTextColor(Color.parseColor("#F44336"));
                    tvDangerLevel.setBackgroundColor(Color.parseColor("#FFE0E0"));
                } else if (dangerLevel.contains("متوسط")) {
                    tvDangerLevel.setTextColor(Color.parseColor("#FF9800"));
                    tvDangerLevel.setBackgroundColor(Color.parseColor("#FFF9C4"));
                } else if (dangerLevel.contains("طفيف")) {
                    tvDangerLevel.setTextColor(Color.parseColor("#4CAF50"));
                    tvDangerLevel.setBackgroundColor(Color.parseColor("#E8F5E9"));
                }
            }
            caseCursor.close();
        }

        List<FirstAidStep> steps = new ArrayList<>();
        Cursor stepsCursor = dbHelper.getStepsForCase(caseId);

        if (stepsCursor != null && stepsCursor.moveToFirst()) {
            do {
                int stepId = stepsCursor.getInt(stepsCursor.getColumnIndexOrThrow("step_id"));
                int num = stepsCursor.getInt(stepsCursor.getColumnIndexOrThrow("step_number"));
                String text = stepsCursor.getString(stepsCursor.getColumnIndexOrThrow("instruction_text"));
                steps.add(new FirstAidStep(stepId, caseId, num, text));
            } while (stepsCursor.moveToNext());
            stepsCursor.close();
        }

        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setAdapter(new StepsAdapter(steps));
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
