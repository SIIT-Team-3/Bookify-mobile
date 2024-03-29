package com.example.bookify.fragments.user;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bookify.R;
import com.example.bookify.adapters.data.AllUsersAdapter;
import com.example.bookify.clients.ClientUtils;
import com.example.bookify.model.user.UserDTO;
import com.example.bookify.utils.JWTUtils;
import com.example.bookify.utils.SearchWatcher;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AllUsersFragment extends Fragment {

    private List<UserDTO> users;
    private ListView allUsersListView;
    private AllUsersAdapter adapter;
    private Activity activity;
    private TextInputEditText searchTextBox;

    public AllUsersFragment(Activity activity) {
        // Required empty public constructor
        this.activity = activity;
    }

    public static AllUsersFragment newInstance(Activity activity) {
        AllUsersFragment fragment = new AllUsersFragment(activity);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allUsersListView = view.findViewById(R.id.all_users);
        getAllUsers();
        searchTextBox = view.findViewById(R.id.search);
        searchTextBox.addTextChangedListener(new SearchWatcher(searchTextBox) {
            @Override
            public void applySearch(TextView textView, String text) {
                adapter.filterUsers(text);
            }
        });
    }

    private void getAllUsers(){
        Call<List<UserDTO>> call = ClientUtils.accountService.getAllUsers();
        call.enqueue(new Callback<List<UserDTO>>() {
            @Override
            public void onResponse(Call<List<UserDTO>> call, Response<List<UserDTO>> response) {
                if(response.body() != null && response.code() == 200) {
                    users = response.body();
                    adapter = new AllUsersAdapter(requireActivity(), users);
                    allUsersListView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<UserDTO>> call, Throwable t) {
                JWTUtils.autoLogout((AppCompatActivity) activity, t);
            }
        });
    }
}