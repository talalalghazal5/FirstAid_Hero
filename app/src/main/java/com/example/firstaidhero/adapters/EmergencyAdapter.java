package com.example.firstaidhero.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.R;
import com.example.firstaidhero.models.EmergencyCase;
import java.util.List;

/**
 * محول لعرض قائمة الحالات الإسعافية.
 */
public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {

    private List<EmergencyCase> casesList;
    private OnCaseClickListener listener;

    public interface OnCaseClickListener {
        void onCaseClick(EmergencyCase emergencyCase);
    }

    public EmergencyAdapter(List<EmergencyCase> casesList, OnCaseClickListener listener) {
        this.casesList = casesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyCase emergencyCase = casesList.get(position);
        holder.tvTitle.setText(emergencyCase.getTitle());
        holder.tvDangerLevel.setText("مستوى الخطورة: " + emergencyCase.getDangerLevel());
        holder.tvSymptoms.setText("الأعراض: " + emergencyCase.getSymptoms());

        // تعيين لون مستوى الخطورة
        String level = emergencyCase.getDangerLevel();
        if (level != null) {
            if (level.contains("حرِج") || level.contains("حرج")) {
                holder.tvDangerLevel.setTextColor(Color.parseColor("#F44336")); // أحمر
                holder.tvDangerLevel.setBackgroundColor(Color.parseColor("#FFE0E0"));
            } else if (level.contains("متوسط")) {
                holder.tvDangerLevel.setTextColor(Color.parseColor("#FF9800")); // برتقالي/أصفر غامق للقراءة
                holder.tvDangerLevel.setBackgroundColor(Color.parseColor("#FFF9C4"));
            } else if (level.contains("طفيف")) {
                holder.tvDangerLevel.setTextColor(Color.parseColor("#4CAF50")); // أخضر
                holder.tvDangerLevel.setBackgroundColor(Color.parseColor("#E8F5E9"));
            } else {
                holder.tvDangerLevel.setTextColor(Color.GRAY);
                holder.tvDangerLevel.setBackgroundColor(Color.parseColor("#F5F5F5"));
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onCaseClick(emergencyCase));
    }

    @Override
    public int getItemCount() {
        return casesList.size();
    }

    public void updateList(List<EmergencyCase> newList) {
        this.casesList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDangerLevel, tvSymptoms;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDangerLevel = itemView.findViewById(R.id.tv_danger_level);
            tvSymptoms = itemView.findViewById(R.id.tv_symptoms_preview);
        }
    }
}
