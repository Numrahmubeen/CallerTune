package com.caller.tune.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.caller.tune.ChooseContactsActivity;
import com.caller.tune.MainActivity;
import com.caller.tune.R;
import com.caller.tune.adapter.ContactsAdapter;
import com.caller.tune.adapter.PriorityContactsAdapter;
import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


public class PriorityFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyDbHandler db;
    public PriorityContactsAdapter adapter;
    public ArrayList<ContactModel> priorityList;
    private ProgressBar progressBar;

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

        }else {
            Toast.makeText(getContext(), "No Priority Contact Found", Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setColorFilter(Color.WHITE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChooseContactsActivity.class);
                startActivity(intent);
            }
        });
        return view;

    }

    public void onPause() {
        super.onPause();
        if (PriorityContactsAdapter.actionMode != null) {
            PriorityContactsAdapter.actionMode.finish();
            PriorityContactsAdapter.actionMode = null;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        refreshRV();
    }

    public void refreshRV(){
        priorityList.clear();
        priorityList.addAll(db.getAllContacts());
        Collections.sort(priorityList, new Comparator<ContactModel>() {
            @Override
            public int compare(ContactModel o1, ContactModel o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        adapter = new PriorityContactsAdapter(getContext(),priorityList);
        adapter.notifyDataSetChanged();
    }
}