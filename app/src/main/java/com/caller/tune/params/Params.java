package com.caller.tune.params;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.caller.tune.R;
import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;

import java.util.ArrayList;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Params {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "contacts_db";
    public static final String TABLE_NAME = "contacts_table";


    //Keys of our table in db
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone_number";
    public static final String KEY_PIC = "contact_pic";
    public static final String KEY_CALL_MODE = "call_ring_mode";
    public static final String KEY_MSG_MODE = "msg_ring_mode";

    public static final String AM_RING_MODE = "ring";
    public static final String AM_VIBRATE_MODE = "vibrate";
    public static final String AM_SILENT_MODE = "silent";
    public static final String AM_SKIP_MODE = "block";

    public static long addContact(String name, String number,String type,Context context){
        long contactId ;
        Uri addContactsUri = ContactsContract.Data.CONTENT_URI;
        long rowContactId = getRawContactId(context);
        String displayName = name;
        insertContactDisplayName(addContactsUri, rowContactId, displayName,context);
        String phoneNumber = number;
        String phoneTypeStr = type;//work,home etc
        contactId = insertContactPhoneNumber(addContactsUri, rowContactId, phoneNumber, phoneTypeStr,context);
        return contactId;
    }

     static void insertContactDisplayName(Uri addContactsUri, long rawContactId, String displayName,Context context)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        // Put contact display name value.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, displayName);

        context.getContentResolver().insert(addContactsUri, contentValues);

    }


    static long getRawContactId(Context context)
    {
        // Inser an empty contact.
        ContentValues contentValues = new ContentValues();
        Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created contact raw id.
        long ret = ContentUris.parseId(rawContactUri);
        return ret;
    }

    static long insertContactPhoneNumber(Uri addContactsUri, long rawContactId, String phoneNumber, String phoneTypeStr,Context context) {
        // Create a ContentValues object.
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Put phone number value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

        // Calculate phone type by user selection.
        int phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;

        if ("home".equalsIgnoreCase(phoneTypeStr)) {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        } else if ("mobile".equalsIgnoreCase(phoneTypeStr)) {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        } else if ("work".equalsIgnoreCase(phoneTypeStr)) {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
        }
        // Put phone type value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneContactType);

        // Insert new contact data into phone contact list.
        Uri uri = context.getContentResolver().insert(addContactsUri, contentValues);
        final String[] projection = new String[] { ContactsContract.RawContacts.CONTACT_ID };
        final Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToNext();
        long contactId = cursor.getLong(0);
        cursor.close();
        return contactId;
    }




}
