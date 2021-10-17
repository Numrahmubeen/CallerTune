package com.appsuite.prioritycontacts;

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
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.InCallService;
import android.telephony.PhoneNumberUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.appsuite.prioritycontacts.data.MyDbHandler;
import com.appsuite.prioritycontacts.models.ContactModel;
import com.appsuite.prioritycontacts.params.Params;
import com.appsuite.prioritycontacts.params.Preference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.view.View.GONE;
import static androidx.core.app.NotificationCompat.*;
import static com.appsuite.prioritycontacts.OngoingCall.state;

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
    private int dnd;

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
                if(preference.getRingerModeName().equals("DND"))
                {
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                    incomingCallContact = null;
                    Toast.makeText(this, "Switched to DND.", Toast.LENGTH_SHORT).show();

                }
                else {
                    am.setRingerMode(preference.getRingMode());
                    incomingCallContact = null;
                    if(preference.getRingerModeName() != null){
                        Toast.makeText(this, "Ringer Mode Changed to: "+ preference.getRingerModeName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            preference.setRequiredRingMode(1111);
            preference.setRingMode(1111);
            preference.setRingerModeName("empty");

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

            requiredRingMode = am.getRingerMode();
            if (incomingCallContact.getCallRingMode().equals(Params.AM_RING_MODE))
            {
                setRingerModeAndRingerName();
                setRingingMode();
            }
            else if (incomingCallContact.getCallRingMode().equals(Params.AM_SILENT_MODE)){
                ringerMode = am.getRingerMode();
                switch (ringerMode) {
                    case AudioManager.RINGER_MODE_VIBRATE:
                        ringerModeName = "Vibrate";
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        ringerModeName = "Silent";
                        break;
                    case AudioManager.RINGER_MODE_NORMAL:
                        ringerModeName = "Sound";
                }
                setSilentMode();
            }
            else if (incomingCallContact.getCallRingMode().equals(Params.AM_VIBRATE_MODE)) {
                setRingerModeAndRingerName();
                setVibratingMode();
            } else
                Toast.makeText(this, "incoming call contact ring mode: " + incomingCallContact.getCallRingMode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setRingerModeAndRingerName() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // Check if the notification policy access has been granted for the app.
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new
                    Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
            return;
        }
        if(ToggleDoNotDisturb(notificationManager)){
            ringerMode = 1122;
            ringerModeName = "DND";
        }
        else
        {
            ringerMode = am.getRingerMode();
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_VIBRATE:
                    ringerModeName = "Vibrate";
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    ringerModeName = "Silent";
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    ringerModeName = "Sound";
            }
        }
    }

    private boolean ToggleDoNotDisturb(NotificationManager notificationManager) {
        if (notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALL)
        {
            return false;
        }
        else
        {
            dnd = notificationManager.getCurrentInterruptionFilter();
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            Toast.makeText(sInstance, "DND turn Off " + dnd +"  new "+notificationManager.getCurrentInterruptionFilter(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    private void setSilentMode() {
        requiredRingMode = AudioManager.RINGER_MODE_SILENT;
        if (ringerMode != requiredRingMode) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(this, notification);
            if(r.isPlaying())
                r.stop();
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            preference.setRequiredRingMode(requiredRingMode);
            preference.setRingMode(ringerMode);
            preference.setRingerModeName(ringerModeName);
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

        Intent rejectIntent = new Intent(this, CallService.class);
        rejectIntent.setAction(REJECT_CALL);
        PendingIntent pendingRejectIntent = PendingIntent.getService(this, 1, rejectIntent, 0);


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
        String NOTIFICATION_CHANNEL_ID = getPackageName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }

        notificationBuilder =
                new Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_phone_colorful)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setStyle(new DecoratedCustomViewStyle())
                        .setCustomContentView(smallNotificationLayout)
                        .setCustomBigContentView(notificationLayout)
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
        contactModel.setMobileNumber(number);
        return contactModel;
    }


    @Override
    public void onCallRemoved(Call call) {
        new OngoingCall().setCall(null);
    }

    public static CallService getInstance(){
        return sInstance;
    }
}
