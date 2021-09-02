package com.caller.tune;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.caller.tune.adapter.ContactsAdapter;
import com.caller.tune.adapter.PriorityContactsAdapter;
import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ChooseContactsActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int MY_PERMISSIONS_REQUEST =11 ;
    private ContactsAdapter contactsAdapter;
    private RecyclerView recyclerView;
    public static ProgressBar progressBar;
    private ArrayList<ContactModel> contactList = new ArrayList<>();
    private Button done_bt;
    private TextView noContacts_tv;
    private Toolbar toolbar;
    // Identifies a particular Loader being used in this component
    private static final int CONTACT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contacts);
//        ActivityResultLauncher<String> requestPermissionLauncher =
//                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                    if (isGranted) {
//                        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                contactList = getContacts();
//                                setupAdapter();
//                            }
//                        }, 1);
//                    } else {
//                        Toast.makeText(this, "Permission is required to Select from contacts", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PERMISSION_GRANTED) {
//            requestPermissionLauncher.launch(READ_CONTACTS);
//        }
//        else {
//            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    contactList = getContacts();
//                    setupAdapter();
//                }
//            }, 1);
//        }
        toolbar = findViewById(R.id.myToolbar);
        noContacts_tv = findViewById(R.id.noContact);
        setSupportActionBar(toolbar);
        progressBar=findViewById(R.id.progressbar);
        done_bt = findViewById(R.id.done_bt);

        recyclerView = findViewById(R.id.contacts_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        contactsAdapter = new ContactsAdapter(this);
        recyclerView.setAdapter(contactsAdapter);

        ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                    getLoaderManager().initLoader(CONTACT_LOADER, null, ChooseContactsActivity.this);
            } else {
                Toast.makeText(this, "Permission is required to Select from contacts", Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(READ_CONTACTS);
        }
        else{
            getLoaderManager().initLoader(CONTACT_LOADER, null, this);
        }
        clickEvents();

    }


    public void showAddNumberDialog(Context context)
    {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_add_number);

        EditText numberEt = dialog.findViewById(R.id.number_et);
        EditText nameEt = dialog.findViewById(R.id.name_et);
        Spinner spinner = dialog.findViewById(R.id.numberTyp_sp);
        CheckBox shouldAddToContacts = dialog.findViewById(R.id.shouldAddToPhone);
        TextView cancel_tv = dialog.findViewById(R.id.cancel_tv);
        TextView add_tv = dialog.findViewById(R.id.add_tv);

        cancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        add_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameEt.getText().length()>0 && numberEt.getText().length()>0)
                {
                    MyDbHandler db = new MyDbHandler(context);

                    ContactModel contact = new ContactModel();
                    contact.setId(numberEt.getText().toString()+System.currentTimeMillis());
                    contact.setName(nameEt.getText().toString());
                    contact.setMobileNumber(numberEt.getText().toString());
                    contact.setPhotoUri(null);
                    contact.setCallRingMode(Params.AM_RING_MODE);
                    contact.setMsgRingMode(Params.AM_RING_MODE);
                    if(shouldAddToContacts.isChecked()){
                        if (ContextCompat.checkSelfPermission(context, WRITE_CONTACTS) != PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, WRITE_CONTACTS)) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{WRITE_CONTACTS}, 11);
                            } else {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{WRITE_CONTACTS}, 11);
                            }
                        }
                        else {
                            long cId = Params.addContact(nameEt.getText().toString(),numberEt.getText().toString(),spinner.getSelectedItem().toString(),context);
                            contact.setId(String.valueOf(cId));
                            db.addContact(contact);
                            Toast.makeText(context, "New Contact Successfully Saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        db.addContact(contact);
                        Toast.makeText(context, "Contact Successfully Saved as Priority Contact!", Toast.LENGTH_SHORT).show();

                    }
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(context, "Please Enter complete details.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();


            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_choose_contacts_toolbar,menu);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.item_search).getActionView();
        searchView.onActionViewExpanded();
        searchView.setQueryHint("Search Contacts");
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsAdapter.filter(newText);
                return true;            }
        });

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_add:
                showAddNumberDialog(ChooseContactsActivity.this);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clickEvents() {
        done_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDbHandler db = new MyDbHandler(ChooseContactsActivity.this);
                ArrayList<ContactModel> selectedContactList = contactsAdapter.getSelectedContacts();
                if(selectedContactList != null && selectedContactList.size()>0)
                {
                    for (ContactModel contact : selectedContactList){
                        contact.setCallRingMode(Params.AM_RING_MODE);
                        contact.setMsgRingMode(Params.AM_RING_MODE);
                        db.addContact(contact);
                    }
                    Toast.makeText(ChooseContactsActivity.this, selectedContactList.size()+" Contact successfully added to Priority " +selectedContactList.size(), Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(ChooseContactsActivity.this, "You didn't select any contact.", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(ChooseContactsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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
                            .getBitmap(this.getContentResolver(),
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri CONTACT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        return new CursorLoader(this, CONTACT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            contactList = getContacts(data);
        } catch (Exception e) {
            Toast.makeText(this, "ERROR: " +e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (contactList.size() == 0) {
            noContacts_tv.setVisibility(View.VISIBLE);
        }
        else {
            Collections.sort(contactList, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            contactsAdapter.setItems(contactList);
        }

        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}