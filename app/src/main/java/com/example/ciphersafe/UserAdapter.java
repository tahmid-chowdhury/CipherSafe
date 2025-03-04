package com.example.ciphersafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {
    private List<Map<String, Object>> userList;
    private List<Map<String, Object>> filteredList;

    public UserAdapter(List<Map<String, Object>> userList) {
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> user = filteredList.get(position);

        if (user == null) return;

        String name = (user.get("username") != null) ? user.get("username").toString() : "Unknown";
        String email = (user.get("email") != null) ? user.get("email").toString() : "No Email";
        String password = (user.get("password") != null) ? user.get("password").toString() : "No Password";

        holder.nameTextView.setText(name);
        holder.emailTextView.setText(email);
        holder.passwordTextView.setText(password);
    }

    @Override
    public int getItemCount() {
        return (filteredList != null) ? filteredList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, passwordTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            passwordTextView = itemView.findViewById(R.id.passwordTextView);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Map<String, Object>> filteredResults = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredResults.addAll(userList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Map<String, Object> user : userList) {
                        String name = user.get("username") != null ? user.get("username").toString().toLowerCase() : "";
                        if (name.contains(filterPattern)) {
                            filteredResults.add(user);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
