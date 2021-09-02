package com.caller.tune.fragments;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.caller.tune.R;
import com.caller.tune.adapter.RecentCallsAdapter;
import com.caller.tune.models.RecentCall;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class RecentCallsFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private RecyclerView recentCalls_rv;
    public static ArrayList<RecentCall> recentCalls = new ArrayList<>();
    private String str_number, str_contact_name, str_call_type, str_call_full_date,
            str_call_date, str_call_time, str_call_time_formatted, str_call_duration;

    private SectionedRecyclerViewAdapter sectionedAdapter;


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
        init();
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
//                        getRecentCalls();
                        getActivity().getLoaderManager().initLoader(12, null, this);

                    } else {
                        Toast.makeText(getContext(), "Permission is required.", Toast.LENGTH_SHORT).show();
                    }
                });


        if (ContextCompat.checkSelfPermission(getContext(), READ_CALL_LOG) != PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(READ_CALL_LOG);
        } else {
            getActivity().getLoaderManager().initLoader(12, null, this);
        }

        return view;
    }

    private void init() {
        recentCalls_rv.setHasFixedSize(true);
        recentCalls_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        recentCalls = new ArrayList<>();
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

    public void getRecentCalls(Cursor cursor){
        // reading all data in descending order according to DATE


        //clearing the arraylist
        recentCalls.clear();

        //looping through the cursor to add data into arraylist
        while (cursor.moveToNext()){
            str_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            str_contact_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
       //     str_contact_name = str_contact_name==null || str_contact_name.equals("") ? "Unknown" : str_contact_name;
            str_call_type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            str_call_full_date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            str_call_duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "dd MMM yyyy");
            str_call_date = dateFormatter.format(new Date(Long.parseLong(str_call_full_date)));

            SimpleDateFormat timeFormatter = new SimpleDateFormat(
                    "HH:mm:ss");
            str_call_time = timeFormatter.format(new Date(Long.parseLong(str_call_full_date)));
            str_call_time_formatted = getFormattedDateTime(str_call_time,"HH:mm:ss","hh:mm a");

            //str_call_time = getFormatedDateTime(str_call_time, "HH:mm:ss", "hh:mm ss");

            str_call_duration = DurationFormat(str_call_duration);

            switch(Integer.parseInt(str_call_type)){
                case CallLog.Calls.INCOMING_TYPE:
                    str_call_type = "Incoming";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    str_call_type = "Outgoing";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    str_call_type = "Missed";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    str_call_type = "Voicemail";
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    str_call_type = "Rejected";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    str_call_type = "Blocked";
                    break;
                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                    str_call_type = "Externally Answered";
                    break;
                default:
                    str_call_type = "NA";
            }
            String carrierId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
            String photoPath = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI));
            RecentCall callLogItem = new RecentCall(str_number, str_contact_name, str_call_type,
                    str_call_date, str_call_time_formatted, str_call_duration,carrierId,photoPath);

            recentCalls.add(callLogItem);
        }
        cursor.close();
        setupAdapter();
    }


    private String getFormattedDateTime(String dateStr, String strInputFormat, String strOutputFormat) {
        String formattedDate = dateStr;
        DateFormat inputFormat = new SimpleDateFormat(strInputFormat, Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat(strOutputFormat, Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(dateStr);
        } catch (ParseException e) {
        }

        if (date != null) {
            formattedDate = outputFormat.format(date);
        }
        return formattedDate;
    }
    private String DurationFormat(String duration) {
        String durationFormatted=null;
        if(Integer.parseInt(duration) < 60){
            durationFormatted = duration+" sec";
        }
        else{
            int min = Integer.parseInt(duration)/60;
            int sec = Integer.parseInt(duration)%60;

            if(sec==0)
                durationFormatted = min + " min" ;
            else
                durationFormatted = min + " min " + sec + " sec";

        }
        return durationFormatted;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri CONTACT_URI = CallLog.Calls.CONTENT_URI;

        return new CursorLoader(getContext(),CONTACT_URI,null,null,null,android.provider.CallLog.Calls.DATE + " DESC");   }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        getRecentCalls(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}