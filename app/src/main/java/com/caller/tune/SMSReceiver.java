package com.caller.tune;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;

import java.util.ArrayList;

public class SMSReceiver extends BroadcastReceiver {

    private Context context;
    private SmsMessage currentSMS;
    private String senderNo;
    private ArrayList<ContactModel> priorityContactsList;
    private MyDbHandler db;
    private boolean isIncomingNumberPriority_msg = false;
    private ContactModel incomingContact_msg;
    private int ringerMode_msg, requiredRingMode_msg;
    private String ringerModeName_msg;
    private AudioManager am_msg;


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
//                        message = currentSMS.getDisplayMessageBody();
                    }
                    managePriorityMessages();
//                    Toast.makeText(context, "senderNum: " + senderNo + " :\n message: " + message, Toast.LENGTH_LONG).show();

                    this.abortBroadcast();
//                    if(ringerMode != requiredRingMode)
//                    {
//                        am.setRingerMode(ringerMode);
//                        incomingCallContact = null;
//                        if(ringerModeName != null){
//                            Toast.makeText(context, "Ringer Mode Changed to: "+ ringerModeName, Toast.LENGTH_SHORT).show();
//                        }
//                    }
                    // End of loop
                }
            }
        } // bundle null
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