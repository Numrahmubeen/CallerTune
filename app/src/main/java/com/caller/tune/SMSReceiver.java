package com.caller.tune;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SMSReceiver extends BroadcastReceiver {

    private Context context;
    private SmsMessage currentSMS;
    private String senderNo;
    private ArrayList<ContactModel> priorityContactsList;
    private MyDbHandler db;
    private boolean isIncomingNumberPriority_msg = false;
    private ContactModel incomingContact_msg;
    public static int ringerMode_msg, requiredRingMode_msg;
    public static String ringerModeName_msg;
    private AudioManager am_msg;
    private Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        db = new MyDbHandler(context);
        priorityContactsList = db.getAllContacts();
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects) {
                        currentSMS = getIncomingMessage(aObject, bundle);
                         senderNo = currentSMS.getDisplayOriginatingAddress();
                         RunNotification(senderNo,currentSMS.getDisplayMessageBody());
                    }
                    managePriorityMessages();
                    this.abortBroadcast();

                }
            }
        } // bundle null
    }
    public static ContactModel contactModel;
    private void RunNotification(String senderNo, String msg) {

        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "Enable Pop up Screen to get SMS in popup.", Toast.LENGTH_SHORT).show();
        }
        else {
            intent = new Intent(context, FloatingActivity.class);

            if(context.startService(intent) != null)
            {
                contactModel = retrieveContactInfo(context,senderNo);
                contactModel.setMsgRingMode(msg);
                context.stopService(intent);
            }
            else {
            contactModel = retrieveContactInfo(context,senderNo);
            contactModel.setMsgRingMode(msg);
            }
            context.startService(intent);
        }

//        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "notify_001");
//
//        contentView = new RemoteViews(context.getPackageName(), R.layout.layout_sms_receiver_notification);
//        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
//        contentView.setTextViewText(R.id.title,senderNo);
//        contentView.setTextViewText(R.id.charging,msg);
//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//        PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);
////        contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
//
//        mBuilder.setSmallIcon(R.drawable.call_sound);
//        mBuilder.setAutoCancel(false);
//        mBuilder.setOngoing(true);
//        mBuilder.setPriority(Notification.PRIORITY_HIGH);
//        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
//        mBuilder.setOnlyAlertOnce(true);
//        mBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH;
//        mBuilder.setContent(contentView);
//        mBuilder.setCustomHeadsUpContentView(contentView);
//        mBuilder.setFullScreenIntent(pendingIntent,true);
//        if (Build.VERSION.SDK_INT >= 21)
//            mBuilder.setVibrate(new long[0]);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            String channelId = "channel_id";
//            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
//            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//            notificationManager.createNotificationChannel(channel);
//            mBuilder.setChannelId(channelId);
//        }
//        notification = mBuilder.build();
//        notificationManager.notify(NotificationID, notification);
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

    private void managePriorityMessages() {
        for (ContactModel c:priorityContactsList)
        {
            String cNo = c.getMobileNumber().replaceAll("\\p{Z}","");
            if(PhoneNumberUtils.compare(senderNo, cNo))
            {
                isIncomingNumberPriority_msg = true;
                incomingContact_msg = c;
                break;
            }
        }
        if(incomingContact_msg != null) {
            am_msg = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            ringerMode_msg = am_msg.getRingerMode();
            switch (ringerMode_msg)
            {
                case AudioManager.RINGER_MODE_VIBRATE:
                    ringerModeName_msg = "Vibrate";
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    ringerModeName_msg = "Silent";
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    ringerModeName_msg = "Sound";
            }
            requiredRingMode_msg = am_msg.getRingerMode();
            if (incomingContact_msg.getMsgRingMode().equals(Params.AM_RING_MODE))
                setMsgRingingMode();
            else if (incomingContact_msg.getMsgRingMode().equals(Params.AM_SILENT_MODE))
                setMsgSilentMode();
            else if (incomingContact_msg.getMsgRingMode().equals(Params.AM_VIBRATE_MODE)){
                setMsgVibratingMode();
            }
            else
                Toast.makeText(context, "Incoming call contact ring mode: "+incomingContact_msg.getMsgRingMode(), Toast.LENGTH_SHORT).show();
        }
    }
    private void setMsgSilentMode() {
        requiredRingMode_msg = AudioManager.RINGER_MODE_SILENT;
        if (ringerMode_msg != requiredRingMode_msg) {
            am_msg.setRingerMode(requiredRingMode_msg);
            Toast.makeText(context, "Ringer Mode Changed to: Silent" , Toast.LENGTH_SHORT).show();
        }

    }
    private void setMsgVibratingMode() {
        requiredRingMode_msg = AudioManager.RINGER_MODE_VIBRATE;
        if (ringerMode_msg != requiredRingMode_msg) {
            am_msg.setRingerMode(requiredRingMode_msg);
            Toast.makeText(context, "Ringer Mode Changed to: Vibrate", Toast.LENGTH_SHORT).show();
        }
    }
    private void setMsgRingingMode() {
        requiredRingMode_msg = AudioManager.RINGER_MODE_NORMAL;
        if (ringerMode_msg != requiredRingMode_msg) {
            am_msg.setRingerMode(requiredRingMode_msg);

            Toast.makeText(context, "Ringer Mode Changed to: Sound", Toast.LENGTH_SHORT).show();
        }
    }
    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }
}