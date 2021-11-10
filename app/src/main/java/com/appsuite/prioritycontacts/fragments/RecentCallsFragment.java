package com.appsuite.prioritycontacts.fragments;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appsuite.prioritycontacts.MainActivity;
import com.appsuite.prioritycontacts.PermissionActivity;
import com.appsuite.prioritycontacts.R;
import com.appsuite.prioritycontacts.SplashActivity;
import com.appsuite.prioritycontacts.adapter.RecentCallsAdapter;
import com.appsuite.prioritycontacts.models.RecentCall;
import com.appsuite.prioritycontacts.viewModels.CallLogViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.content.Context.TELECOM_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class RecentCallsFragment extends Fragment {
    private RecyclerView recentCalls_rv;
    public static ArrayList<RecentCall> recentCalls = new ArrayList<>();
    private CallLogViewModel callLogViewModel;

    private SectionedRecyclerViewAdapter sectionedAdapter;
    private ProgressBar progressBar;

    public RecentCallsFragment() {
    }

    public static RecentCallsFragment newInstance(String param1, String param2) {
        RecentCallsFragment fragment = new RecentCallsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent_calls, container, false);
        recentCalls_rv = view.findViewById(R.id.recent_calls_rv);
        progressBar = view.findViewById(R.id.recent_calls_pb);
        callLogViewModel = new CallLogViewModel(getActivity().getApplication());
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
//                        getRecentCalls();
                        getRecentCalls();

                    } else {
                        Toast.makeText(getContext(), "Permission is required.", Toast.LENGTH_SHORT).show();
                    }
                });
        ContentObserver contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                // if there're any changes then perform the request and update the UI
//                if (ContextCompat.checkSelfPermission(getContext(), READ_CALL_LOG) != PERMISSION_GRANTED) {
//                    requestPermissionLauncher.launch(READ_CALL_LOG);
//                } else {
                    getRecentCalls();
                //}
            }
        };
        getActivity().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI,true,contentObserver);
        init();

//        if (ContextCompat.checkSelfPermission(getContext(), READ_CALL_LOG) != PERMISSION_GRANTED) {
//            requestPermissionLauncher.launch(READ_CALL_LOG);
//        } else {
            getRecentCalls();
       // }
        return view;
    }

    private void init() {
        recentCalls_rv.setHasFixedSize(true);
        recentCalls = new ArrayList<>();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(TELECOM_SERVICE);
            if (!getContext().getApplicationContext().getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                Intent intent = new Intent(getContext(), PermissionActivity.class);
                getContext().startActivity(intent);
                getActivity().finish();
            }
        }
    }

    private void setupAdapter() {
        sectionedAdapter = new SectionedRecyclerViewAdapter();

        final   Map<String, List<RecentCall>> contactsMap = getMap();
        for (final Map.Entry<String, List<RecentCall>> entry : contactsMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                sectionedAdapter.addSection(new RecentCallsAdapter(getContext(),entry.getValue(),true,entry.getKey()));
            }
        }
        recentCalls_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        recentCalls_rv.setAdapter(sectionedAdapter);
    }

    Map<String, List<RecentCall>> getMap() {
        final Map<String, List<RecentCall>> map = new LinkedHashMap<>();

        for (RecentCall recentCall : recentCalls) {

            final List<RecentCall> filteredContacts = getCallsOfSpecificDate(recentCalls,recentCall.getCallDate());
            if (filteredContacts.size() > 0) {
                map.put(recentCall.getCallDate(), filteredContacts);
            }
        }

        return map;
    }

    private List<RecentCall> getCallsOfSpecificDate(ArrayList<RecentCall> recentCalls, String callDate) {
        ArrayList<RecentCall> filteredList = new ArrayList<RecentCall>();

        for(RecentCall person : recentCalls) {
            if(person.getCallDate().equals(callDate))
                filteredList.add(person);
        }
        return filteredList;
    }

//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private void getRecentSimsInfo() {
//        if (ContextCompat.checkSelfPermission(getContext(), READ_PHONE_STATE) != PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
//        }
//        else {
//            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
//
//            tv.setVisibility(View.VISIBLE);
//
////            final SubscriptionManager subscriptionManager = SubscriptionManager.from(getContext().getApplicationContext());
////            final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
////            if(activeSubscriptionInfoList.size()>1){
////                tv.setVisibility(View.VISIBLE);
////                tv.setText("Sim 1 Info: ICCID"+ activeSubscriptionInfoList.get(0).getIccId()
////                        +"Carrier Id: "+activeSubscriptionInfoList.get(0).getCarrierId()+
////                        "Subscription Id: "+activeSubscriptionInfoList.get(0).getSubscriptionId()+
////                        "Sim 2 Info: ICCID"+ activeSubscriptionInfoList.get(1).getIccId()
////                        +"Carrier Id: "+activeSubscriptionInfoList.get(1).getCarrierId()+
////                        "Subscription Id: "+activeSubscriptionInfoList.get(1).getSubscriptionId());
////            }
////            else if(activeSubscriptionInfoList.size()>0){
////                tv.setVisibility(View.VISIBLE);
////                tv.setText("Sim 1 Info: ICCID"+ activeSubscriptionInfoList.get(0).getIccId()
////                        +"Carrier Id: "+activeSubscriptionInfoList.get(0).getCarrierId()+
////                        "Subscription Id: "+activeSubscriptionInfoList.get(0).getSubscriptionId()+
////                        "card Id: "+activeSubscriptionInfoList.get(0).getCardId()+
////                        "carrier name: "+activeSubscriptionInfoList.get(0).getCarrierName());
////            }
//        }
//    }

    public void getRecentCalls(){
        callLogViewModel.getRecentCalls().observe(getActivity(),myRecentCalls ->{
            if(myRecentCalls != null){
                recentCalls = myRecentCalls;
                setupAdapter();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private String queryPhone(String number) {
        String name = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);

        }
        cursor.close();
        return name;
    }

}