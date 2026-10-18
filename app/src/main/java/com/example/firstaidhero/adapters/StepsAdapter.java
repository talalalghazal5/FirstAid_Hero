package com.example.firstaidhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.R;
import com.example.firstaidhero.models.FirstAidStep;
import java.util.List;

/**
 * محول لعرض قائمة خطوات الإسعاف الأولية مرتبة.
 */
public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepViewHolder> {

    private final List<FirstAidStep> stepsList;

    public StepsAdapter(List<FirstAidStep> stepsList) {
        this.stepsList = stepsList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        FirstAidStep step = stepsList.get(position);
        // عرض رقم الخطوة
        holder.tvStepNumber.setText(String.valueOf(step.getStepNumber()));
        // عرض نص التعليمات
        holder.tvStepInstruction.setText(step.getInstructionText());
    }

    @Override
    public int getItemCount() {
        return stepsList.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepInstruction;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tv_step_number);
            tvStepInstruction = itemView.findViewById(R.id.tv_step_instruction);
        }
    }
}
