package com.example.bookify.fragments.reservation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.example.bookify.R;
import com.example.bookify.adapters.data.GuestRequestsListAdapter;
import com.example.bookify.adapters.data.OwnerRequestsListAdapter;
import com.example.bookify.clients.ClientUtils;
import com.example.bookify.enumerations.Status;
import com.example.bookify.model.DropdownItem;
import com.example.bookify.model.reservation.ReservationDTO;
import com.example.bookify.utils.JWTUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestFragmentOwner#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestFragmentOwner extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    FloatingActionButton filterButton;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OwnerRequestsListAdapter adapter;
    private ListView listView;

    Long dateStart;
    Long dateEnd;
    Status[] saveStatuses = {Status.PENDING, Status.ACCEPTED, Status.CANCELED, Status.REJECTED};

    public RequestFragmentOwner() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestFragmentGuest.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestFragmentOwner newInstance(String param1, String param2) {
        RequestFragmentOwner fragment = new RequestFragmentOwner();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_owner, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        filterButton = (FloatingActionButton) getView().findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

    }

    private void getData(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
        Long ownerId = sharedPreferences.getLong(JWTUtils.USER_ID, -1);
        Call<List<ReservationDTO>> call = ClientUtils.reservationService.getAllRequestsForOwner(ownerId);
        call.enqueue(new Callback<List<ReservationDTO>>() {
            @Override
            public void onResponse(Call<List<ReservationDTO>> call, Response<List<ReservationDTO>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<ReservationDTO> result = response.body();
                    showResults(result);
                }
            }

            @Override
            public void onFailure(Call<List<ReservationDTO>> call, Throwable t) {
                Log.d("Error", "Reservation error");
                JWTUtils.autoLogout((AppCompatActivity) getActivity(), t);
            }
        });
    }

    private void showResults(List<ReservationDTO> reservations){
        adapter = new OwnerRequestsListAdapter(getActivity(), reservations);
        listView = getView().findViewById(R.id.resultList);
        listView.setAdapter(adapter);
    }

    private void showBottomDialog(){
        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter_requests);

        final Long[] accommodationId = new Long[1];
        ArrayAdapter<DropdownItem> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, getAccommodations());
        AutoCompleteTextView autoCompleteTextView = dialog.findViewById(R.id.filled_exposed);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            DropdownItem selectedItem = (DropdownItem) parent.getItemAtPosition(position);
            accommodationId[0] = selectedItem.getId();
        });

        loadStatuses(dialog);

        Button editDate = dialog.findViewById(R.id.editButton);
        if (dateStart == null || dateEnd == null) {
            dateStart = MaterialDatePicker.thisMonthInUtcMilliseconds();
            dateEnd = MaterialDatePicker.todayInUtcMilliseconds();
        }
        else{
            String startDate = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(new Date(dateStart));
            String endDate = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(new Date(dateEnd));
            editDate.setText(startDate + " - " + endDate);
        }
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(new Pair<>(
                        dateStart,
                        dateEnd
                )).build();

                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        String startDate = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(new Date(selection.first));
                        String endDate = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(new Date(selection.second));

                        dateStart = selection.first;
                        dateEnd = selection.second;

                        editDate.setText(startDate + " - " + endDate);
                    }
                });

                materialDatePicker.show(getParentFragmentManager(), "tag");
            }
        });

        Button save = dialog.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
                Long ownerId = sharedPreferences.getLong(JWTUtils.USER_ID, -1);
                if (accommodationId[0] != null && !editDate.getText().equals("")) {
                    Call<List<ReservationDTO>> call = ClientUtils.reservationService.getFilteredRequestsForOwner(ownerId, accommodationId[0], editDate.getText().toString().split(" - ")[0], editDate.getText().toString().split(" - ")[1], getStatuses(dialog));
                    call.enqueue(new Callback<List<ReservationDTO>>() {
                        @Override
                        public void onResponse(Call<List<ReservationDTO>> call, Response<List<ReservationDTO>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                showResults(response.body());
                                dialog.cancel();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ReservationDTO>> call, Throwable t) {
                            Log.d("Save", "Error");
                            JWTUtils.autoLogout((AppCompatActivity) getActivity(), t);
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Please select accommodation and date range", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button remove = dialog.findViewById(R.id.remove);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
                Long ownerId = sharedPreferences.getLong(JWTUtils.USER_ID, -1);
                Call<List<ReservationDTO>> call = ClientUtils.reservationService.getAllRequestsForOwner(ownerId);
                call.enqueue(new Callback<List<ReservationDTO>>() {
                    @Override
                    public void onResponse(Call<List<ReservationDTO>> call, Response<List<ReservationDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            showResults(response.body());
                            dateStart = null;
                            dateEnd = null;
                            saveStatuses = new Status[]{Status.PENDING, Status.ACCEPTED, Status.CANCELED, Status.REJECTED};
                            dialog.cancel();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ReservationDTO>> call, Throwable t) {
                        Log.d("RemoveAll", "Error");
                        JWTUtils.autoLogout((AppCompatActivity) getActivity(), t);
                    }
                });
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void loadStatuses(BottomSheetDialog dialog){
        CheckBox pending = dialog.findViewById(R.id.pending);
        CheckBox accepted = dialog.findViewById(R.id.accepted);
        CheckBox rejected = dialog.findViewById(R.id.rejected);
        CheckBox canceled = dialog.findViewById(R.id.canceled);

        for (Status s : saveStatuses){
            if (s == Status.ACCEPTED)
                accepted.setChecked(true);
            if (s == Status.PENDING)
                pending.setChecked(true);
            if (s == Status.REJECTED)
                rejected.setChecked(true);
            if (s == Status.CANCELED)
                canceled.setChecked(true);
        }
    }

    private Status[] getStatuses(BottomSheetDialog dialog){
        CheckBox pending = dialog.findViewById(R.id.pending);
        CheckBox accepted = dialog.findViewById(R.id.accepted);
        CheckBox rejected = dialog.findViewById(R.id.rejected);
        CheckBox canceled = dialog.findViewById(R.id.canceled);

        Status[] statuses = new Status[4];
        int index = 0;

        if (pending.isChecked()) {
            statuses[index] = Status.PENDING;
            index++;
        }
        if (accepted.isChecked()) {
            statuses[index] = Status.ACCEPTED;
            index++;
        }
        if (rejected.isChecked()) {
            statuses[index] = Status.REJECTED;
            index++;
        }
        if (canceled.isChecked())
            statuses[index] = Status.CANCELED;

        saveStatuses = statuses;

        return statuses;
    }

    private List<DropdownItem> getAccommodations(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
        Long ownerId = sharedPreferences.getLong(JWTUtils.USER_ID, -1);
        List<DropdownItem> itemList = new ArrayList<>();
        Call<List<Object[]>> call = ClientUtils.reservationService.getAccommodationNamesOwner(ownerId);
        call.enqueue(new Callback<List<Object[]>>() {
            @Override
            public void onResponse(Call<List<Object[]>> call, Response<List<Object[]>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Object[] o : response.body()){
                        double doubleValue = Double.parseDouble(o[0].toString());
                        long longValue = (long) doubleValue;
                        itemList.add(new DropdownItem(o[1].toString(), longValue));
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Object[]>> call, Throwable t) {
                Log.d("AccommodationNames", "Accommodation names not here");
                JWTUtils.autoLogout((AppCompatActivity) getActivity(), t);
            }
        });
        return itemList;
    }
}