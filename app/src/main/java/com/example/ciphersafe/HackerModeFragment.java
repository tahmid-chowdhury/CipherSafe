package com.example.ciphersafe;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ciphersafe.databinding.FragmentHackerModeBinding;

import java.util.List;
import java.util.Map;

public class HackerModeFragment extends Fragment {
    private FragmentHackerModeBinding binding;
    private UserAdapter userAdapter;
    private DatabaseManager databaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHackerModeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseManager = new DatabaseManager((MainActivity) getActivity());


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        loadUserData();


        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (userAdapter != null && !TextUtils.isEmpty(query)) {
                    userAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (userAdapter != null) {
                    userAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }


    private void loadUserData() {
        databaseManager.getAllUsers(new DatabaseManager.FirebaseUserDataListener() {
            @Override
            public void onSuccess(List<Map<String, Object>> userList) {
                if (!userList.isEmpty()) {
                    userAdapter = new UserAdapter(userList);
                    binding.recyclerView.setAdapter(userAdapter);
                } else {
                    Log.w("HackerMode", "No user data found.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HackerMode", "Error fetching users: " + errorMessage);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
