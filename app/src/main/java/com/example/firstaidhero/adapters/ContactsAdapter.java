package com.example.firstaidhero.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstaidhero.R;
import com.example.firstaidhero.models.EmergencyContact;
import java.util.List;

/**
 * محول لعرض قائمة جهات اتصال الطوارئ المخصصة.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private final List<EmergencyContact> contactList;
    private final OnContactActionListener actionListener;

    public interface OnContactActionListener {
        void onCallClick(String phoneNumber);
        void onEditClick(EmergencyContact contact);
        void onDeleteClick(EmergencyContact contact);
    }

    public ContactsAdapter(List<EmergencyContact> contactList, OnContactActionListener actionListener) {
        this.contactList = contactList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());

        holder.btnCall.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCallClick(contact.getPhoneNumber());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClick(contact);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnCall, btnEdit, btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvPhone = itemView.findViewById(R.id.tv_contact_phone);
            btnCall = itemView.findViewById(R.id.btn_call_contact);
            btnEdit = itemView.findViewById(R.id.btn_edit_contact);
            btnDelete = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}
