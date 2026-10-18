package com.example.firstaidhero.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.R;
import com.example.firstaidhero.models.TreatedCase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * محول لعرض سجل الحالات التي تم إسعافها.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<TreatedCase> historyList;
    private final List<TreatedCase> historyListFull;
    private final OnHistoryActionListener actionListener;

    public interface OnHistoryActionListener {
        void onEditClick(TreatedCase treatedCase);
        void onDeleteClick(TreatedCase treatedCase);
        void onItemClick(TreatedCase treatedCase);
    }

    public HistoryAdapter(List<TreatedCase> historyList, OnHistoryActionListener actionListener) {
        this.historyList = historyList;
        this.historyListFull = new ArrayList<>(historyList);
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TreatedCase treatedCase = historyList.get(position);
        holder.tvDesc.setText(treatedCase.getDescription());
        holder.tvDate.setText(treatedCase.getDate());
        holder.tvProcedures.setText("الإجراءات: " + treatedCase.getProcedures());

        if (treatedCase.getPhotoPath() != null && !treatedCase.getPhotoPath().isEmpty()) {
            File imgFile = new File(treatedCase.getPhotoPath());
            if (imgFile.exists()) {
                holder.ivPhoto.setImageURI(Uri.fromFile(imgFile));
                holder.ivPhoto.setVisibility(View.VISIBLE);
            } else {
                holder.ivPhoto.setVisibility(View.GONE);
            }
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(treatedCase);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClick(treatedCase);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClick(treatedCase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateList(List<TreatedCase> newList) {
        this.historyList = newList;
        notifyDataSetChanged();
    }

    public void setFullList(List<TreatedCase> fullList) {
        this.historyList = fullList;
        this.historyListFull.clear();
        this.historyListFull.addAll(fullList);
        notifyDataSetChanged();
    }

    public List<TreatedCase> getFullList() {
        return historyListFull;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvDate, tvProcedures;
        ImageView ivPhoto;
        ImageButton btnEdit, btnDelete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tv_history_desc);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            tvProcedures = itemView.findViewById(R.id.tv_history_procedures);
            ivPhoto = itemView.findViewById(R.id.iv_history_photo);
            btnEdit = itemView.findViewById(R.id.btn_edit_history);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);
        }
    }
}
