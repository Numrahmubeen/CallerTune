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
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class PhoneFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private EditText screen;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private ImageView dialpad_call_iv;
    private PhoneContactsAdapter contactsAdapter;
    private RecyclerView recyclerView;
    private ArrayList<ContactModel> contactList = new ArrayList<>();
    private ConstraintLayout dialPad_cl;
    private ImageView showDialPad_iv;
    private RelativeLayout btn0_rl;
    private static final int CONTACT_LOADER = 0;
  //  private EditText searchView;

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

        setupSearchRV();

        if (ContextCompat.checkSelfPermission(getContext(), CALL_PHONE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{CALL_PHONE}, MY_PERMISSIONS_REQUEST);
        }

        showDialPad_iv.setOnClickListener(v -> {
            dialPad_cl.setVisibility(View.VISIBLE);
            showDialPad_iv.setVisibility(View.GONE);
        });

        return view;
    }

    private void setupSearchRV() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        getActivity().getLoaderManager().initLoader(CONTACT_LOADER, null, this);
                    } else {
                        Toast.makeText(getContext(), "Permission is required to Select from contacts", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(getContext(), READ_CONTACTS) != PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(READ_CONTACTS);
        }
        else {
            getActivity().getLoaderManager().initLoader(CONTACT_LOADER, null, this);
        }
        screen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>2)
                    contactsAdapter.filter(s.toString());
                else
                    contactsAdapter.setItems(contactList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        searchView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.length()>2)
//                    contactsAdapter.filter(s.toString());
//                else
//                    contactsAdapter.setItems(contactList);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
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
        setupAdapter();

    }
    public ArrayList<ContactModel> getContacts(Cursor cursor) {
        ArrayList<ContactModel> list = new ArrayList<>();
        if (cursor.getCount() > 0) {


            while (cursor.moveToNext()) {
                ContactModel info = new ContactModel();
                info.setId(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                info.setMobileNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                String imageUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media
                            .getBitmap(getContext().getContentResolver(),
                                    Uri.parse(imageUri));

                } catch (Exception e) {
                }
                info.setPhoto(bitmap);
                list.add(info);
//
//                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
//                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
//                    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
//                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));
//
//                    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id));
//                    Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.PHOTO);
//
//                    Bitmap photo = null;
//                    if (inputStream != null) {
//                        photo = BitmapFactory.decodeStream(inputStream);
//                    }
//                    while (cursorInfo.moveToNext()) {
//                        ContactModel info = new ContactModel();
//                        info.setId(id);
//                        info.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
//                        info.setMobileNumber(cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
//                        info.setPhoto(photo);
//                        info.setPhotoUri(pURI.toString());
//                        list.add(info);
//                    }
//
            }
            cursor.close();
        }
        return list;
    }
    private void setupAdapter() {
        Collections.sort(contactList, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        contactsAdapter = new PhoneContactsAdapter(getContext(), item -> {
            screen.getText().clear();
            screen.getText().insert(screen.getSelectionStart(),item.getMobileNumber().toLowerCase().replaceAll("\\p{Z}",""));
            dialPad_cl.setVisibility(View.VISIBLE);
            showDialPad_iv.setVisibility(View.GONE);
        });
        recyclerView.setAdapter(contactsAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getContext(), CALL_PHONE) == PERMISSION_GRANTED) {
                        Toast.makeText(getContext(), "Permission Granted!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No Permission Granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initializeView(View view) {
        screen = view.findViewById(R.id.screen);
//        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.fragPhone_rv);
        dialPad_cl = view.findViewById(R.id.dialPad_cl);
        showDialPad_iv = view.findViewById(R.id.showDialPad_iv);
//        screen.setCursorVisible(true);
//        screen.setTextIsSelectable(true);
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

        btn0_rl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                display("+");
                return true;
            }
        });
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
                boolean dualActive = checkSimAvailability();
                if (dualActive) {
                    selectSim();
                }
                else
                    makeCall(-1);
                break;
            case R.id.btnDel:
                int pos = screen.getSelectionStart();
                if (pos > 0) {
                    screen.setText(screen.getText().delete(pos - 1, pos).toString());
                    screen.setSelection(pos - 1);
                }
                break;
//            case R.id.btnClr:
//                screen.setText(" ");
//                break;
            default:
                break;
        }

    }

    private boolean checkSimAvailability() {
        // cursor_call_logs.getColumnIndexOrThrow("subscription_id")
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(getContext().getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        int simCount = activeSubscriptionInfoList.size();
        if(simCount > 1){
            return true;
        }
//        btnBack.setText(simCount+" Sim available");
//        Log.d("MainActivity: ","simCount:" +simCount);
//        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
//            Log.d("MainActivity: ","iccId :"+ subscriptionInfo.getIccId()+" , name : "+ subscriptionInfo.getDisplayName());
//        }
            return false;
    }

    private void selectSim(){
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_sim);

        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.select_sim_radio_group);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.sim1_rb)
                {
                    makeCall(0);
                    dialog.dismiss();
                }
                else if(checkedId == R.id.sim2_rb)
                {
                    makeCall(1);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();


    }

    private void makeCall(int simNumber) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + screen.getText().toString()));
        intent.setData(Uri.parse("tel:" + screen.getText().toString()));
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getActivity().getSystemService(Context.TELECOM_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission required", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{READ_PHONE_STATE}, 2);
                return;
            }
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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri CONTACT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        return new CursorLoader(getContext(), CONTACT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            contactList = getContacts(data);
        } catch (Exception e) {
            Toast.makeText(getContext(), "ERROR: " +e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        Collections.sort(contactList, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        contactsAdapter.setItems(contactList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}