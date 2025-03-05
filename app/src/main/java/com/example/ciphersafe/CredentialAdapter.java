package com.example.ciphersafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CredentialAdapter extends RecyclerView.Adapter<CredentialAdapter.CredentialViewHolder> {

    private List<Credential> credentials;
    private OnCredentialClickListener listener;

    public interface OnCredentialClickListener {
        void onEditClick(Credential credential, int position);
        void onDeleteClick(Credential credential, int position);
        void onShowPasswordClick(Credential credential, int position);
    }

    public CredentialAdapter(OnCredentialClickListener listener) {
        this.credentials = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CredentialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_credential, parent, false);
        return new CredentialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CredentialViewHolder holder, int position) {
        Credential credential = credentials.get(position);
        holder.bind(credential, position);
    }

    @Override
    public int getItemCount() {
        return credentials.size();
    }

    public void setCredentials(List<Credential> credentials) {
        this.credentials = credentials;
        notifyDataSetChanged();
    }

    public void addCredential(Credential credential) {
        credentials.add(credential);
        notifyItemInserted(credentials.size() - 1);
    }

    public void updateCredential(Credential credential, int position) {
        credentials.set(position, credential);
        notifyItemChanged(position);
    }

    public void removeCredential(int position) {
        credentials.remove(position);
        notifyItemRemoved(position);
    }

    class CredentialViewHolder extends RecyclerView.ViewHolder {
        private TextView serviceNameTextView;
        private TextView usernameTextView;
        private TextView passwordTextView;
        private ImageButton showPasswordButton;
        private ImageButton editButton;
        private ImageButton deleteButton;
        private boolean isPasswordVisible = false;

        public CredentialViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceNameTextView = itemView.findViewById(R.id.serviceNameTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            passwordTextView = itemView.findViewById(R.id.passwordTextView);
            showPasswordButton = itemView.findViewById(R.id.showPasswordButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final Credential credential, final int position) {
            serviceNameTextView.setText(credential.getServiceName());
            usernameTextView.setText(credential.getUsername());
            // Always start with masked password
            passwordTextView.setText("••••••••");
            isPasswordVisible = false;

            showPasswordButton.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    passwordTextView.setText("••••••••");
                    isPasswordVisible = false;
                } else {
                    listener.onShowPasswordClick(credential, position);
                    passwordTextView.setText(credential.getPassword());
                    isPasswordVisible = true;
                }
            });

            editButton.setOnClickListener(v -> {
                listener.onEditClick(credential, position);
            });

            deleteButton.setOnClickListener(v -> {
                listener.onDeleteClick(credential, position);
            });
        }
    }
}
