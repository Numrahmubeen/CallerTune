package com.caller.tune.repository;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.lifecycle.MutableLiveData;

import com.caller.tune.models.ContactModel;
import com.caller.tune.models.RecentCall;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactRepository {

    private Context context;
    private  MutableLiveData<ArrayList<ContactModel>> contactsList;

    public ContactRepository(Context context) {
        this.context = context;
        contactsList = new MutableLiveData<>();
    }
    //todo fetch contacts from Content Uri
    public MutableLiveData<ArrayList<ContactModel>> fetchContacts() {

        ExecutorService service =  Executors.newSingleThreadExecutor();
        service.submit(() -> {

        ArrayList<ContactModel> contacts = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC");
        if ((cursor != null ? cursor.getCount() : 0) > 0) {
            while (cursor.moveToNext()) {

                ContactModel info = new ContactModel();
                info.setId(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                info.setMobileNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                String imageUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media
                            .getBitmap(context.getContentResolver(),
                                    Uri.parse(imageUri));

                } catch (Exception e) {
                }
                info.setPhoto(bitmap);
                contacts.add(info);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        // now that you have the fresh user data in freshUserList,
            // make it available to outside observers of the "users"
            // MutableLiveData object
            ArrayList<ContactModel> distinctList = removeDuplicates(contacts);
            contactsList.postValue(distinctList);
        });

        return contactsList;
    }
    public ArrayList<ContactModel> removeDuplicates(ArrayList<ContactModel> list){
        Set<ContactModel> set = new TreeSet((Comparator<ContactModel>) (o1, o2) -> {
            String str1 = o1.getMobileNumber().replaceAll("\\s", "");
            str1 = str1.replaceAll("-","");

            String str2 = o2.getMobileNumber().replaceAll("\\s", "");
            str2 = str2.replaceAll("-","");
            if(str1.contains(str2)){
                return 0;
            }
            return 1;
        });
        set.addAll(list);

        final ArrayList newList = new ArrayList(set);
        return newList;
    }

}
