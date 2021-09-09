package com.caller.tune;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.InCallService;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.caller.tune.CallActivity;
import com.caller.tune.OngoingCall;
import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;
import com.caller.tune.params.Preference;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static androidx.core.app.NotificationCompat.*;
import static com.caller.tune.OngoingCall.state;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CallService extends InCallService {

    private static final int NOTIF_ID = 111;
    public static CallService sInstance;
    private ArrayList<ContactModel> priorityContactsList;
    private MyDbHandler db;
    private boolean isIncomingNumberPriority = false;
    public static ContactModel incomingCallContact;
    public static int ringerMode, requiredRingMode;
    public static String ringerModeName;
    private String number;
    public static AudioManager am;
    public static Ringtone r;
    private String callerName, callDirection = "Calling";
    private Builder notificationBuilder;
    private final String ACCEPT_CALL ="ACCEPT_CALL" , REJECT_CALL = "REJECT_CALL";
    private Call myCall;
    private Preference preference;
    private RemoteViews smallNotificationLayout,notificationLayout;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preference = new Preference(this);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if(intent != null)
        {
            String action = intent.getAction();
            if(action != null){
                switch (action)
                {
                    case ACCEPT_CALL:
                        OngoingCall.answer();
                        if(myCall != null){
                            state.subscribe(this::updateUi);
                            new OngoingCall().setCall(myCall);
                            CallActivity.start(this, myCall);
                        }

                        break;
                    case REJECT_CALL:
                        OngoingCall.hangup();
                        if(preference.getRingMode() != preference.getRequiredRingMode())
                        {
                            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            am.setRingerMode(preference.getRingMode());
                            incomingCallContact = null;
                            if(preference.getRingerModeName() != null){
                                Toast.makeText(this, "Ringer Mode Changed to: "+ preference.getRingerModeName(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        stopForeground(true);
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCallAdded(Call call) {
        preference = new Preference(this);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        new OngoingCall().setCall(call);
        myCall = call;
        number = call.getDetails().getHandle().getSchemeSpecificPart();
        sInstance = this;
        CallActivity.start(this, call);
        ContactModel contactModel = retrieveContactInfo(this,number);
        callerName = contactModel.getName();
        if(callerName == null)
            callerName = number;
        state.subscribe(this::updateUi);
        manageNotification(callDirection,"  "+ callerName);

    }

    @SuppressLint("RestrictedApi")
    private void updateUi(Integer state) {
        if(state == Call.STATE_RINGING){
            managePriorityContacts();
            callDirection =  "  Incoming Call";
        }
        else {
            if(r != null){
                if(r.isPlaying())
                    r.stop();
            }
        }

        if(state == Call.STATE_DIALING){
            callDirection =  "  Outgoing Call";
        }
        if(state == Call.STATE_ACTIVE || state == Call.STATE_DIALING)
        {
//            notificationBuilder.mActions.remove(0);
            notificationLayout.setViewVisibility(R.id.notification_answer_call_tv,GONE);
            smallNotificationLayout.setViewVisibility(R.id.notification_small_accept_call_iv,GONE);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIF_ID, notificationBuilder.build());

        }
        if(state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING)
        {

            if(preference.getRingMode() != preference.getRequiredRingMode())
            {
                am.setRingerMode(preference.getRingMode());
                incomingCallContact = null;
                if(preference.getRingerModeName() != null){
                    Toast.makeText(this, "Ringer Mode Changed to: "+ preference.getRingerModeName(), Toast.LENGTH_SHORT).show();
                }
            }
            stopForeground(true);
        }
    }

    private void managePriorityContacts() {
        db = new MyDbHandler(this);
        priorityContactsList = db.getAllContacts();
        for (ContactModel c:priorityContactsList)
        {
            String cNo = c.getMobileNumber().replaceAll("\\p{Z}","");
            if(PhoneNumberUtils.compare(number, cNo))
            {
                isIncomingNumberPriority = true;
                incomingCallContact = c;
                break;
            }
        }
        if(incomingCallContact != null) {
            ringerMode = am.getRingerMode();
            switch (ringerMode)
            {
                case AudioManager.RINGER_MODE_VIBRATE:
                    ringerModeName = "Vibrate";
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    ringerModeName = "Silent";
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    ringerModeName = "Sound";
            }
            requiredRingMode = am.getRingerMode();
            if (incomingCallContact.getCallRingMode().equals(Params.AM_RING_MODE))
                setRingingMode();
            else if (incomingCallContact.getCallRingMode().equals(Params.AM_SILENT_MODE))
                setSilentMode();
            else if (incomingCallContact.getCallRingMode().equals(Params.AM_VIBRATE_MODE)){
                setVibratingMode();
            }
            else
                Toast.makeText(this, "incming call contact ring mode: "+incomingCallContact.getCallRingMode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setSilentMode() {
        requiredRingMode = AudioManager.RINGER_MODE_SILENT;
        if (ringerMode != requiredRingMode) {
            preference.setRequiredRingMode(requiredRingMode);
            preference.setRingMode(ringerMode);
            preference.setRingerModeName(ringerModeName);

            am.setRingerMode(requiredRingMode);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(this, notification);
            if(r.isPlaying())
                r.stop();
            Toast.makeText(this, "Ringer Mode Changed to: Silent" , Toast.LENGTH_SHORT).show();
        }

    }

    private void setVibratingMode() {
        requiredRingMode = AudioManager.RINGER_MODE_VIBRATE;
        if (ringerMode != requiredRingMode) {
            preference.setRequiredRingMode(requiredRingMode);
            preference.setRingMode(ringerMode);
            preference.setRingerModeName(ringerModeName);

            am.setRingerMode(requiredRingMode);
            Uri ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(this, ring);
            r.play();
            Toast.makeText(this, "Ringer Mode Changed to: Vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRingingMode() {
        requiredRingMode = AudioManager.RINGER_MODE_NORMAL;

        if (ringerMode != requiredRingMode) {
            preference.setRequiredRingMode(requiredRingMode);
            preference.setRingMode(ringerMode);
            preference.setRingerModeName(ringerModeName);

            am.setRingerMode(requiredRingMode);
            Uri ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(this, ring);
            r.play();
            Toast.makeText(this, "Ringer Mode Changed to: Sound", Toast.LENGTH_SHORT).show();
        }
    }
    private void manageNotification(String callTyp, String callerName) {

        Intent acceptIntent = new Intent(this, CallService.class);
        acceptIntent.setAction(ACCEPT_CALL);
        PendingIntent pendingAnswerIntent = PendingIntent.getService(this, 0, acceptIntent, 0);
 //       Action receiveAction = new Action(android.R.drawable.ic_media_play, "ACCEPT", pendingPlayIntent);

        Intent rejectIntent = new Intent(this, CallService.class);
        rejectIntent.setAction(REJECT_CALL);
        PendingIntent pendingRejectIntent = PendingIntent.getService(this, 1, rejectIntent, 0);
//        Action rejectAction = new Action(android.R.drawable.ic_media_play, "REJECT", pendingRejectIntent);


        Intent notifyIntent = new Intent(this, CallActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notificationLayout = new RemoteViews(getPackageName(), R.layout.layout_custom_call_notification);
        notificationLayout.setTextViewText(R.id.notification_title_tv,callTyp);
        notificationLayout.setTextViewText(R.id.notification_incoming_caller,callerName);
        notificationLayout.setOnClickPendingIntent(R.id.notification_answer_call_tv,pendingAnswerIntent);
        notificationLayout.setOnClickPendingIntent(R.id.notification_reject_call_tv,pendingRejectIntent);

        smallNotificationLayout = new RemoteViews(getPackageName(), R.layout.layout_custom_call_small_notification);
        smallNotificationLayout.setTextViewText(R.id.notification_small_title_tv,callTyp);
        smallNotificationLayout.setTextViewText(R.id.notification_small_incoming_caller,callerName);
        smallNotificationLayout.setOnClickPendingIntent(R.id.notification_small_accept_call_iv,pendingAnswerIntent);
        smallNotificationLayout.setOnClickPendingIntent(R.id.notification_small_reject_call_iv,pendingRejectIntent);

        notificationBuilder =
                new Builder(this, "CHANNEL_ID")
                        .setSmallIcon(R.drawable.ic_phone_colorful)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setStyle(new DecoratedCustomViewStyle())
                        .setCustomContentView(smallNotificationLayout)
                        .setCustomBigContentView(notificationLayout)
//                        .setContentTitle(callTyp)
//                        .setContentText(callerName)
//                        .addAction(receiveAction)
//                        .addAction(rejectAction)
                        .setFullScreenIntent(notifyPendingIntent, true);
        notificationBuilder.setSilent(true);

        Notification incomingCallNotification = notificationBuilder.build();
        startForeground(NOTIF_ID, incomingCallNotification);

    }
    public static ContactModel retrieveContactInfo(Context context, String number) {
        ContactModel contactModel = new ContactModel();
        ContentResolver contentResolver = context.getContentResolver();
        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor =
                contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                contactModel.setName(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)));
            }
            cursor.close();
        }

        Bitmap photo;

        try {
            if(contactId != null) {
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));

                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    contactModel.setPhoto(photo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  ContactModel contactModel = new ContactModel("name","number",photo,myMsg);
        contactModel.setMobileNumber(number);
        return contactModel;
    }



//    private void manageNotification(String callTyp, String callerName) {
//
//        Intent acceptIntent = new Intent(this, CallService.class);
//        acceptIntent.setAction(ACCEPT_CALL);
//        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, acceptIntent, 0);
//        NotificationCompat.Action receiveAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "ACCEPT", pendingPlayIntent);
//
//        Intent rejectIntent = new Intent(this, CallService.class);
//        rejectIntent.setAction(REJECT_CALL);
//        PendingIntent pendingRejectIntent = PendingIntent.getService(this, 0, rejectIntent, 0);
//        NotificationCompat.Action rejectAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "REJECT", pendingRejectIntent);
//
////        Intent fullScreenIntent = new Intent(this, CallActivity.class);
////        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
////                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent notifyIntent = new Intent(this, CallActivity.class);
//    // Set the Activity to start in a new, empty task
//        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//    // Create the PendingIntent
//        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
//                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//        notificationBuilder =
//                new NotificationCompat.Builder(this, "CHANNEL_ID")
//                        .setSmallIcon(R.drawable.ic_phone_colorful)
//                        .setContentTitle(callTyp)
//                        .setContentText(callerName)
////                        .addAction(R.drawable.circle_bg, "Receive Call", receiveCallPendingIntent)
//                        .addAction(receiveAction)
//                        .addAction(rejectAction)
//                        .setPriority(NotificationCompat.PRIORITY_HIGH)
//                        .setCategory(NotificationCompat.CATEGORY_CALL)
////                        .setContentIntent(notifyPendingIntent)
//                        .setFullScreenIntent(notifyPendingIntent, true);
//        notificationBuilder.setSilent(true);
//
//        Notification incomingCallNotification = notificationBuilder.build();
//        startForeground(NOTIF_ID, incomingCallNotification);
//
//    }

    @Override
    public void onCallRemoved(Call call) {
        new OngoingCall().setCall(null);
    }

    public static CallService getInstance(){
        return sInstance;
    }
}
