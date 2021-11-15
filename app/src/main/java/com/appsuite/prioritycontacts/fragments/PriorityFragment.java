package com.appsuite.prioritycontacts.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsuite.prioritycontacts.ChooseContactsActivity;
import com.appsuite.prioritycontacts.PermissionActivity;
import com.appsuite.prioritycontacts.R;
import com.appsuite.prioritycontacts.adapter.PriorityContactsAdapter;
import com.appsuite.prioritycontacts.data.MyDbHandler;
import com.appsuite.prioritycontacts.models.ContactModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.Context.TELECOM_SERVICE;


public class PriorityFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyDbHandler db;
    public PriorityContactsAdapter adapter;
    public ArrayList<ContactModel> priorityList;
    private ProgressBar progressBar;
    private TextView noContacts_tv;

    public PriorityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_priority, container, false);


        db = new MyDbHandler(getContext());
        priorityList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.priority_contacts_rv);
        progressBar = view.findViewById(R.id.progressbar);
        noContacts_tv = view.findViewById(R.id.noContacts_tv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        priorityList = db.getAllContacts();

        if(priorityList.size() > 0)
        {
            Collections.sort(priorityList, new Comparator<ContactModel>() {
                        @Override
                        public int compare(ContactModel o1, ContactModel o2) {
                            return o1.getName().compareToIgnoreCase(o2.getName());
                        }
                    });
            adapter = new PriorityContactsAdapter(getContext(), priorityList);
            recyclerView.setAdapter(adapter);
            noContacts_tv.setVisibility(View.GONE);

        }else {
            noContacts_tv.setVisibility(View.VISIBLE);
//            Toast.makeText(getContext(), "No Priority Contact Found", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setColorFilter(Color.WHITE);
        fab.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), ChooseContactsActivity.class);
            startActivity(intent);
        });
        return view;

    }
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(TELECOM_SERVICE);
            if (!getContext().getApplicationContext().getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                Intent intent = new Intent(getContext(), PermissionActivity.class);
                getContext().startActivity(intent);
                getActivity().finish();
            }
        }
        refreshRV();

    }
    public void onPause() {
        super.onPause();
        if (PriorityContactsAdapter.actionMode != null) {
            PriorityContactsAdapter.actionMode.finish();
            PriorityContactsAdapter.actionMode = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshRV();
    }

    public void refreshRV(){
        priorityList.clear();
        priorityList.addAll(db.getAllContacts());
        Collections.sort(priorityList, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        if(priorityList.size() == 0){
            noContacts_tv.setVisibility(View.VISIBLE);
        }
        else
            noContacts_tv.setVisibility(View.GONE);
        adapter = new PriorityContactsAdapter(getContext(),priorityList);
        adapter.notifyDataSetChanged();
    }
}