package com.example.firstaidhero;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.adapters.HistoryAdapter;
import com.example.firstaidhero.database.DatabaseHelper;
import com.example.firstaidhero.models.TreatedCase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryActionListener {

    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;
    private TextView tvEmptyHistory;
    
    private Uri selectedImageUri;
    private ImageView dialogImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        View toolbarView = findViewById(R.id.toolbar_history);
        if (toolbarView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbarView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);

        Toolbar toolbarWidget = (Toolbar) toolbarView;
        setSupportActionBar(toolbarWidget);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.rv_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fab_add_history).setOnClickListener(v -> showAddHistoryDialog());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (dialogImageView != null) {
                            dialogImageView.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        setupBottomNavigation();
        loadHistory();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_contacts) {
                startActivity(new Intent(this, ContactsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_history) {
                return true;
            }
            return false;
        });
    }

    private void loadHistory() {
        List<TreatedCase> cases = dbHelper.getAllTreatedCases();
        adapter.setFullList(cases);
        updateEmptyView(cases.isEmpty(), "لا توجد حالات مسجلة");
    }

    private void updateEmptyView(boolean isEmpty, String message) {
        if (tvEmptyHistory != null) {
            tvEmptyHistory.setText(message);
            tvEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void showAddHistoryDialog() {
        selectedImageUri = null;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_history, null);
        EditText etDesc = view.findViewById(R.id.et_case_description);
        EditText etProc = view.findViewById(R.id.et_case_procedures);
        EditText etDate = view.findViewById(R.id.et_case_date);
        dialogImageView = view.findViewById(R.id.iv_case_photo);
        Button btnSelectPhoto = view.findViewById(R.id.btn_select_photo);

        String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        etDate.setText(currentDate);

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%d/%02d/%02d", year, month + 1, dayOfMonth);
                etDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        new AlertDialog.Builder(this)
                .setTitle("تسجيل حالة معالجة")
                .setView(view)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    String desc = etDesc.getText().toString().trim();
                    String proc = etProc.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String photoPath = "";

                    if (selectedImageUri != null) {
                        photoPath = saveImageToInternalStorage(selectedImageUri);
                    }

                    if (!desc.isEmpty()) {
                        dbHelper.addTreatedCase(desc, date, proc, photoPath);
                        loadHistory();
                    } else {
                        Toast.makeText(this, "يرجى كتابة وصف للحالة", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public void onEditClick(TreatedCase treatedCase) {
        selectedImageUri = null;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_history, null);
        EditText etDesc = view.findViewById(R.id.et_case_description);
        EditText etProc = view.findViewById(R.id.et_case_procedures);
        EditText etDate = view.findViewById(R.id.et_case_date);
        dialogImageView = view.findViewById(R.id.iv_case_photo);
        Button btnSelectPhoto = view.findViewById(R.id.btn_select_photo);

        etDesc.setText(treatedCase.getDescription());
        etProc.setText(treatedCase.getProcedures());
        etDate.setText(treatedCase.getDate());

        if (treatedCase.getPhotoPath() != null && !treatedCase.getPhotoPath().isEmpty()) {
            File imgFile = new File(treatedCase.getPhotoPath());
            if (imgFile.exists()) {
                dialogImageView.setImageURI(Uri.fromFile(imgFile));
            }
        }

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                String[] parts = treatedCase.getDate().split("/");
                calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
            } catch (Exception ignored) {}

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%d/%02d/%02d", year, month + 1, dayOfMonth);
                etDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        btnSelectPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        new AlertDialog.Builder(this)
                .setTitle("تعديل حالة معالجة")
                .setView(view)
                .setPositiveButton("تحديث", (dialog, which) -> {
                    String desc = etDesc.getText().toString().trim();
                    String proc = etProc.getText().toString().trim();
                    String date = etDate.getText().toString().trim();
                    String photoPath = treatedCase.getPhotoPath();

                    if (selectedImageUri != null) {
                        photoPath = saveImageToInternalStorage(selectedImageUri);
                    }

                    if (!desc.isEmpty()) {
                        dbHelper.updateTreatedCase(treatedCase.getId(), desc, date, proc, photoPath);
                        loadHistory();
                    } else {
                        Toast.makeText(this, "يرجى كتابة وصف للحالة", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public void onDeleteClick(TreatedCase treatedCase) {
        new AlertDialog.Builder(this)
                .setTitle("حذف حالة")
                .setMessage("هل أنت متأكد من حذف هذه الحالة من السجل؟")
                .setPositiveButton("حذف", (dialog, which) -> {
                    dbHelper.deleteTreatedCase(treatedCase.getId());
                    loadHistory();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public void onItemClick(TreatedCase treatedCase) {
        Intent intent = new Intent(this, TreatedCaseDetailsActivity.class);
        intent.putExtra("CASE_ID", treatedCase.getId());
        intent.putExtra("CASE_TITLE", treatedCase.getDescription());
        intent.putExtra("CASE_DATE", treatedCase.getDate());
        intent.putExtra("CASE_PROCEDURES", treatedCase.getProcedures());
        intent.putExtra("CASE_PHOTO", treatedCase.getPhotoPath());
        startActivity(intent);
    }

    private String saveImageToInternalStorage(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            File file = new File(getFilesDir(), fileName);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("بحث في الحالات...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        return true;
    }

    private void filter(String text) {
        if (adapter == null) return;
        List<TreatedCase> fullList = adapter.getFullList();
        if (text == null || text.trim().isEmpty()) {
            adapter.updateList(new ArrayList<>(fullList));
            updateEmptyView(fullList.isEmpty(), "لا توجد حالات مسجلة");
            return;
        }

        String query = normalizeArabic(text);
        List<TreatedCase> filteredList = new ArrayList<>();

        for (TreatedCase item : fullList) {
            String desc = normalizeArabic(item.getDescription());
            
            boolean match = false;
            if (query.length() == 1) {
                match = wordStartsWith(desc, query);
            } else {
                String proc = normalizeArabic(item.getProcedures());
                match = desc.contains(query) || proc.contains(query);
            }

            if (match) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
        updateEmptyView(filteredList.isEmpty(), "لم يتم العثور على نتائج");
    }

    private boolean wordStartsWith(String text, String query) {
        if (text == null || query == null || query.isEmpty()) return false;
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.startsWith(query)) return true;
            if (word.startsWith("ال") && word.length() > 2) {
                if (word.substring(2).startsWith(query)) return true;
            }
        }
        return false;
    }

    private String normalizeArabic(String text) {
        if (text == null) return "";
        return text.toLowerCase(Locale.getDefault())
                .replaceAll("[أإآ]", "ا")
                .replaceAll("ة", "ه")
                .replaceAll("ى", "ي")
                .replaceAll("[\u064B-\u0652]", "")
                .trim();
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
        bottomNav.setSelectedItemId(R.id.nav_history);
    }
}
