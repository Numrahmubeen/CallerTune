package com.appsuite.prioritycontacts.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.appsuite.prioritycontacts.models.RecentCall;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallLogRepository extends LiveData<ArrayList<RecentCall>> {
    private Context context;
    ContentResolver contentResolver;
    private MutableLiveData<ArrayList<RecentCall>> callLogs;
    private String str_number, str_contact_name, str_call_type, str_call_full_date,
            str_call_date, str_call_time, str_call_time_formatted, str_call_duration;

//    private final ContentObserver contentObserver = new ContentObserver(new Handler()) {
//        @Override
//        public void onChange(boolean selfChange) {
//            // if there're any changes then perform the request and update the UI
//            fetchCallLogs();
//        }
//    };
//    @Override
//    protected void onActive() {
//        super.onActive();
//
//        contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI,true,contentObserver);
//    }
//
//    @Override
//    protected void onInactive() {
//        super.onInactive();
//        contentResolver.unregisterContentObserver(contentObserver);
//
//    }

    public CallLogRepository(Context context) {

        this.context = context;
        contentResolver = context.getContentResolver();
        callLogs = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<RecentCall>> fetchCallLogs(){

        ExecutorService service =  Executors.newSingleThreadExecutor();
        service.submit(() -> {
            // on background thread, obtain a fresh list of users
            ArrayList<RecentCall> recentCalls = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null,null, CallLog.Calls.DATE + " DESC");
            if ((cursor != null ? cursor.getCount() : 0) > 0) {
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

            }
            if (cursor != null) {
                cursor.close();
            }

            // now that you have the fresh user data in freshUserList,
            // make it available to outside observers of the "users"
            // MutableLiveData object
            callLogs.postValue(recentCalls);
        });
        return callLogs;
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
}
