package com.caller.tune.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.role.RoleManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.caller.tune.ChooseContactsActivity;
import com.caller.tune.R;
import com.caller.tune.adapter.ContactsAdapter;
import com.caller.tune.adapter.PhoneContactsAdapter;
import com.caller.tune.models.ContactModel;
import com.caller.tune.viewModels.ContactViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class PhoneFragment extends Fragment implements View.OnClickListener {

    private EditText screen;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private ImageView dialpad_call_iv;
    private PhoneContactsAdapter contactsAdapter;
    private RecyclerView recyclerView;
    private ArrayList<ContactModel> contactList = new ArrayList<>();
    private ConstraintLayout dialPad_cl;
    private ImageView showDialPad_iv;
    private RelativeLayout btn0_rl;
    private ContactViewModel contactViewModel;
    private EditText searchView_et;

    public PhoneFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        initializeView(view);

        setupAdapter();
        setupSearchRV();

        if (ContextCompat.checkSelfPermission(getContext(), CALL_PHONE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{CALL_PHONE}, MY_PERMISSIONS_REQUEST);
        }

        showDialPad_iv.setOnClickListener(v -> {
            dialPad_cl.setVisibility(View.VISIBLE);
            showDialPad_iv.setVisibility(View.GONE);
            searchView_et.setText(null);
            InputMethodManager imm = (InputMethodManager) view.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
        return view;
    }

//    String retrieveLastCallSummary() {
//        String phNumber = null;
//        Uri contacts = CallLog.Calls.CONTENT_URI;
//        Cursor managedCursor = getContext().getContentResolver().query(
//                contacts, null, null, null, null);
//        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
//        if (managedCursor.moveToFirst() == true) {
//            phNumber = managedCursor.getString(number);
//        }
//        managedCursor.close();
//        return phNumber;
//    }

    private void setupSearchRV() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        contactViewModel.getContacts().observe(getViewLifecycleOwner(), contactModels -> {
                            contactList.clear();
                            contactsAdapter.setItems(contactModels);
                            contactsAdapter.notifyDataSetChanged();
                        });
                    } else {
                        Toast.makeText(getContext(), "Permission is required to Select from contacts", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(getContext(), READ_CONTACTS) != PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(READ_CONTACTS);
        } else {
            contactViewModel.getContacts().observe(getViewLifecycleOwner(), contactModels -> {
                contactList.clear();
                contactsAdapter.setItems(contactModels);
                contactsAdapter.notifyDataSetChanged();
            });
        }
        screen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactsAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchView_et.setOnTouchListener((v, event) -> {
            dialPad_cl.setVisibility(View.GONE);
            screen.setText(null);
            showDialPad_iv.setVisibility(View.VISIBLE);
            return false;
        });
        searchView_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactsAdapter.filter(s.toString());
//                contactsAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                showDialPad_iv.setVisibility(View.VISIBLE);
                dialPad_cl.setVisibility(View.GONE);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    private void setupAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        contactsAdapter = new PhoneContactsAdapter(getContext(), item -> {
            screen.getText().clear();
            screen.getText().insert(screen.getSelectionStart(), item.getMobileNumber().toLowerCase().replaceAll("\\p{Z}", ""));
            dialPad_cl.setVisibility(View.VISIBLE);
            showDialPad_iv.setVisibility(View.GONE);
        });
        recyclerView.setAdapter(contactsAdapter);
    }

    private void initializeView(View view) {
        screen = view.findViewById(R.id.screen);
        searchView_et = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.fragPhone_rv);
        dialPad_cl = view.findViewById(R.id.dialPad_cl);
        showDialPad_iv = view.findViewById(R.id.showDialPad_iv);
        screen.setShowSoftInputOnFocus(false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialpad_call_iv = view.findViewById(R.id.dialpad_call_button);
        dialpad_call_iv.setOnClickListener(this);
        int idList[] = {R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDel, R.id.btnStar,
                R.id.btn0, R.id.btnHash};

        for (int d : idList) {
            View v = (View) view.findViewById(d);
            v.setOnClickListener(this);
        }
        btn0_rl = view.findViewById(R.id.btn0);

        btn0_rl.setOnLongClickListener(v -> {
            display("+");
            return true;
        });
        contactViewModel = new ContactViewModel(getActivity().getApplication());
    }

    public void display(String val) {
        screen.getText().insert(screen.getSelectionStart(), val);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                display("1");
                break;
            case R.id.btn2:
                display("2");
                break;
            case R.id.btn3:
                display("3");
                break;
            case R.id.btn4:
                display("4");
                break;
            case R.id.btn5:
                display("5");
                break;
            case R.id.btn6:
                display("6");
                break;
            case R.id.btn7:
                display("7");
                break;
            case R.id.btn8:
                display("8");
                break;
            case R.id.btn9:
                display("9");
                break;
            case R.id.btn0:
                display("0");
                break;
            case R.id.btnStar:
                display("*");
                break;
            case R.id.btnHash:
                display("#");
                break;
            case R.id.dialpad_call_button:
                if (screen.getText().length() > 2) {
                    makeCall();
                }
                else {
                    Toast.makeText(getContext(),"Please dial or select a number to make a call.", Toast.LENGTH_SHORT).show();
//                    if(retrieveLastCallSummary() != null)
//                        screen.setText(retrieveLastCallSummary());
                }
                break;
            case R.id.btnDel:
                int pos = screen.getSelectionStart();
                int startSelection= screen.getSelectionStart();
                int endSelection= screen.getSelectionEnd();
                String selectedText = screen.getText().toString().substring(startSelection, endSelection);

                if (pos > 0) {
                    screen.setText(screen.getText().delete(pos - 1, pos).toString());
                    screen.setSelection(pos - 1);
                }
                else if(!selectedText.isEmpty())
                {
                    //If you wish to delete the selected text
                    String selectionDeletedString= screen.getText().toString().replace(selectedText,"");
                    screen.setText(selectionDeletedString);
                }
                break;
//            case R.id.btnClr:
//                screen.setText(" ");
//                break;
            default:
                break;
        }

    }
//    private boolean checkSimAvailability() {
//        // cursor_call_logs.getColumnIndexOrThrow("subscription_id")
//        final SubscriptionManager subscriptionManager = SubscriptionManager.from(getContext().getApplicationContext());
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getContext(), "Need permission: READ PHONE STATE", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
//        int simCount = activeSubscriptionInfoList.size();
//        if(simCount > 1){
//            return true;
//        }
//            return false;
//    }
//
//    private void selectSim(){
//        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_select_sim);
//
//        TextView sim1_tv = dialog.findViewById(R.id.sim1Choose_tv);
//        TextView sim2_tv = dialog.findViewById(R.id.sim2Choose_tv);
//
//        sim1_tv.setOnClickListener(v -> {
//            makeCall(0);
//            dialog.dismiss();
//        });
//        sim2_tv.setOnClickListener(v -> {
//            makeCall(1);
//            dialog.dismiss();
//        });
//        dialog.show();
//
//
//    }
    private void makeCall() {

        Intent intent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+Uri.encode(screen.getText().toString())));
        intent.setData(Uri.parse("tel:"+Uri.encode(screen.getText().toString())));
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        intent.setPackage("com.android.server.telecom");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getActivity().getSystemService(Context.TELECOM_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission required", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{READ_PHONE_STATE}, 2);
                return;
            }
            startActivity(intent);
        }
        else
            Toast.makeText(getContext(), "Your device incompatible to make a call from this app.", Toast.LENGTH_SHORT).show();
    }
}