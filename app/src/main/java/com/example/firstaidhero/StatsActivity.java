package com.example.firstaidhero;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تفعيل وضع Edge-to-Edge
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);

        // ضبط المنطقة الآمنة
        View appBar = findViewById(R.id.app_bar);
        if (appBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        Toolbar toolbar = findViewById(R.id.toolbar_stats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        RecyclerView recyclerView = findViewById(R.id.rv_stats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadStatistics(recyclerView);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(0); // إلغاء تحديد الكل
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_contacts) {
                startActivity(new Intent(this, ContactsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadStatistics(RecyclerView recyclerView) {
        List<StatModel> stats = new ArrayList<>();
        Cursor cursor = dbHelper.getDangerLevelStats();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String label = cursor.getString(0); // danger_level
                int count = cursor.getInt(1); // count
                stats.add(new StatModel(label, count));
            } while (cursor.moveToNext());
            cursor.close();
        }

        recyclerView.setAdapter(new StatsAdapter(stats));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // نموذج بيانات بسيط للإحصاء
    static class StatModel {
        String label;
        int count;
        StatModel(String label, int count) { this.label = label; this.count = count; }
    }

    // محول داخلي سريع للإحصاءات
    static class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatViewHolder> {
        List<StatModel> list;
        StatsAdapter(List<StatModel> list) { this.list = list; }

        @NonNull
        @Override
        public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stat, parent, false);
            return new StatViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
            StatModel item = list.get(position);
            holder.tvLabel.setText(item.label);
            holder.tvCount.setText(String.valueOf(item.count));

            // تعيين الألوان بناءً على مستوى الخطورة
            String level = item.label;
            if (level != null) {
                int color;
                if (level.contains("حرِج") || level.contains("حرج")) {
                    color = Color.parseColor("#F44336"); // أحمر
                } else if (level.contains("متوسط")) {
                    color = Color.parseColor("#FFC107"); // أصفر/برتقالي
                } else if (level.contains("طفيف")) {
                    color = Color.parseColor("#4CAF50"); // أخضر
                } else {
                    color = Color.parseColor("#9E9E9E"); // رمادي
                }
                holder.tvCount.setBackgroundTintList(ColorStateList.valueOf(color));
                holder.tvCount.setTextColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class StatViewHolder extends RecyclerView.ViewHolder {
            TextView tvLabel, tvCount;
            StatViewHolder(View v) {
                super(v);
                tvLabel = v.findViewById(R.id.tv_stat_label);
                tvCount = v.findViewById(R.id.tv_stat_count);
            }
        }
    }
}
