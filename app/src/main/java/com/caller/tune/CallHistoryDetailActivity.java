package com.caller.tune;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.caller.tune.adapter.RecentCallsAdapter;
import com.caller.tune.fragments.RecentCallsFragment;
import com.caller.tune.models.RecentCall;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

import static android.Manifest.permission.READ_PHONE_STATE;

public class CallHistoryDetailActivity extends AppCompatActivity {

    private String phoneNumber, name, callerDp;
    private TextView callerName_tv, callerPhone_tv, gotoContactDetails_tv;
    private ImageView callerDp_iv, back_iv;
    private LinearLayout gotoMessage_ll, makeCall_ll, addToContacts_ll;

    private RecyclerView callsHistory_rv;
    private ArrayList<RecentCall> callsHistoryList = new ArrayList<>();
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private View gotoContactDetailsDivider_view;
    private SectionedRecyclerViewAdapter sectionedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history_detail);
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        if(!isDefaultSimSetForCall()){
                            selectSim();
                        }
                        else
                            makeCall(-1);
                    } else {
                        Toast.makeText(this, "Permission is required.", Toast.LENGTH_SHORT).show();
                    }
                });

        bindView();
        setView();
        initRv();
        extractCallHistory();
        clickActions();
    }

    private void clickActions() {
        gotoMessage_ll.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
            startActivity(intent);
        });
        makeCall_ll.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(READ_PHONE_STATE);
            } else
                {
                    if(!isDefaultSimSetForCall()){
                        selectSim();
                    }
                    else
                        makeCall(-1);            }
        });
        addToContacts_ll.setOnClickListener(v->{
            Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
            contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

            contactIntent
                    .putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);

            startActivity(contactIntent);
        });
        gotoContactDetails_tv.setOnClickListener(v->{
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] projection = new String[]{ ContactsContract.PhoneLookup._ID };

            Cursor cur = getContentResolver().query(uri, projection, null, null, null);

            if (cur != null && cur.moveToNext()) {
                Long id = cur.getLong(0);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
                intent.setData(contactUri);
                startActivity(intent);

                cur.close();
            }
        });
        back_iv.setOnClickListener(v->{
            finish();
        });

    }
    private void initRv() {
        callsHistory_rv.setHasFixedSize(true);
        callsHistory_rv.setLayoutManager(new LinearLayoutManager(this));
        callsHistoryList = new ArrayList<>();
    }

    private void extractCallHistory() {
        callsHistoryList.clear();
        for(RecentCall call : RecentCallsFragment.recentCalls)
        {
            if(PhoneNumberUtils.compare(call.getPhoneNumber(), phoneNumber)){
            callsHistoryList.add(new RecentCall("",call.getCallDate()+" " + call.getCallTime(),
                    call.getCallTyp(),"",call.getCallDuration(),"","",call.getCallerDp()));
            }
        }
        if(callsHistoryList.size()>0){
            setupAdapter();
        }
    }
    private void setupAdapter() {
        sectionedAdapter = new SectionedRecyclerViewAdapter();

        final   Map<String, List<RecentCall>> contactsMap = getMap();
        for (final Map.Entry<String, List<RecentCall>> entry : contactsMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                sectionedAdapter.addSection(new RecentCallsAdapter(this,entry.getValue(),false,entry.getKey()));
            }
        }
        callsHistory_rv.setLayoutManager(new LinearLayoutManager(this));
        callsHistory_rv.setAdapter(sectionedAdapter);
    }

    Map<String, List<RecentCall>> getMap() {
        final Map<String, List<RecentCall>> map = new LinkedHashMap<>();

        for (RecentCall recentCall : callsHistoryList) {

            final List<RecentCall> filteredContacts = getCallsOfSpecificDate(callsHistoryList,recentCall.getCallDate());
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
    private void setView() {
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        name = getIntent().getStringExtra("name");
        callerDp = getIntent().getStringExtra("callerDp");

        if(phoneNumber != null)
            callerPhone_tv.setText(phoneNumber);
        if (name != null){
            callerName_tv.setText(name);
            addToContacts_ll.setVisibility(View.GONE);
        }
        else {
            callerPhone_tv.setVisibility(View.GONE);
            gotoContactDetails_tv.setVisibility(View.GONE);
            gotoContactDetailsDivider_view.setVisibility(View.GONE);
            if(phoneNumber != null)
                callerName_tv.setText(phoneNumber);

        }
        if (callerDp != null)
        {
            Glide.with(this).asBitmap().load(callerDp).centerCrop().into(new BitmapImageViewTarget(callerDp_iv) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    callerDp_iv.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
        else {
            Glide.with(this).asBitmap().load(R.drawable.ic_person).centerCrop().into(new BitmapImageViewTarget(callerDp_iv) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    callerDp_iv.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
    }
    private void bindView() {
        callerName_tv = findViewById(R.id.callHistory_callerName_tv);
        callerPhone_tv = findViewById(R.id.callHistory_callerPhone_tv);
        callerDp_iv = findViewById(R.id.callHistory_callerDp_iv);
        gotoMessage_ll = findViewById(R.id.callHistory_gotoMessage_ll);
        makeCall_ll = findViewById(R.id.callHistory_makeCall_ll);
        addToContacts_ll = findViewById(R.id.callHistory_addToContacts_ll);
        callsHistory_rv = findViewById(R.id.callHistory_rv);
        gotoContactDetails_tv = findViewById(R.id.callHistory_gotoContactDetails_tv);
        back_iv = findViewById(R.id.callHistory_back_iv);
        gotoContactDetailsDivider_view = findViewById(R.id.callHistory_gotoContactDetails_view);
    }
    private void selectSim(){
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_sim);

        TextView sim1_tv = dialog.findViewById(R.id.sim1Choose_tv);
        TextView sim2_tv = dialog.findViewById(R.id.sim2Choose_tv);

        sim1_tv.setOnClickListener(v -> {
            makeCall(0);
            dialog.dismiss();
        });
        sim2_tv.setOnClickListener(v -> {
            makeCall(1);
            dialog.dismiss();
        });
        dialog.show();


    }

    boolean isDefaultSimSetForCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE}, 2);
        } else {
            TelecomManager telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
            PhoneAccountHandle defaultPhoneAccount = telecomManager.getDefaultOutgoingPhoneAccount(Uri.fromParts("tel", "text", null).getScheme());
            if (defaultPhoneAccount != null) {
                return true;
            }
        }
        return false;
    }


    private void makeCall(int simNumber) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE}, 2);
                return;
            }
            intent.setPackage("com.android.server.telecom");
            List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
            if (simNumber == 0) {  // simNumber = 0 or 1 according to sim......
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0)
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
            } else if(simNumber == 1) {
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 1)
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
            }

            startActivity(intent);
        }
        else
            Toast.makeText(this, "Your device incompatible to make a call from this app.", Toast.LENGTH_SHORT).show();
    }
}