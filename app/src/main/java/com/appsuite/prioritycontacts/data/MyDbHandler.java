package com.appsuite.prioritycontacts.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.appsuite.prioritycontacts.models.ContactModel;
import com.appsuite.prioritycontacts.params.Params;

import java.util.ArrayList;

public class MyDbHandler extends SQLiteOpenHelper {

    public MyDbHandler(Context context) {
        super(context, Params.DB_NAME, null, Params.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + Params.TABLE_NAME + "("
                + Params.KEY_ID + " TEXT PRIMARY KEY," + Params.KEY_NAME
                + " TEXT, " + Params.KEY_PHONE + " TEXT, " + Params.KEY_PIC
                + " TEXT, " +Params.KEY_MSG_MODE + " TEXT, " + Params.KEY_CALL_MODE
                + " TEXT)";
        db.execSQL(create);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addContact(ContactModel contact){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Params.KEY_ID, contact.getId());
        values.put(Params.KEY_NAME, contact.getName());
        values.put(Params.KEY_PHONE, contact.getMobileNumber());
        values.put(Params.KEY_PIC, contact.getPhotoUri());
        values.put(Params.KEY_MSG_MODE, contact.getCallRingMode());
        values.put(Params.KEY_CALL_MODE, contact.getMsgRingMode());

        db.insert(Params.TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContactModel> getAllContacts(){
        ArrayList<ContactModel> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Generate the query to read from the database
        String select = "SELECT * FROM " + Params.TABLE_NAME;
        Cursor cursor = db.rawQuery(select, null);

        //Loop through now
        if(cursor.moveToFirst()){
            do{
                ContactModel contact = new ContactModel();
                contact.setId(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setMobileNumber(cursor.getString(2));
                contact.setPhotoUri(cursor.getString(3));
                contact.setMsgRingMode(cursor.getString(4));
                contact.setCallRingMode(cursor.getString(5));
                contactList.add(contact);
            }while(cursor.moveToNext());
        }
        return contactList;
    }

    public int updateContact(ContactModel contact){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Params.KEY_NAME, contact.getName());
        values.put(Params.KEY_PHONE, contact.getMobileNumber());
        values.put(Params.KEY_PIC, contact.getPhotoUri());
        values.put(Params.KEY_MSG_MODE, contact.getMsgRingMode());
        values.put(Params.KEY_CALL_MODE, contact.getCallRingMode());
        //Lets update now
        return db.update(Params.TABLE_NAME, values, Params.KEY_ID + "=?",
                new String[]{String.valueOf(contact.getId())});


    }

    public void deleteContactById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Params.TABLE_NAME, Params.KEY_ID +"=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteContact(ContactModel contact){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Params.TABLE_NAME, Params.KEY_ID +"=?", new String[]{String.valueOf(contact.getId())});
        db.close();
    }

    public int getCount(){
        String query = "SELECT  * FROM " + Params.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();

    }

}