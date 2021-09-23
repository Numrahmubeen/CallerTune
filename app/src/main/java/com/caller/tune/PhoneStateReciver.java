package com.caller.tune;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;

import java.util.ArrayList;


public class PhoneStateReciver {}
//        extends BroadcastReceiver {
//
//    String savedNumber;
//    private ArrayList<ContactModel> priorityContactsList;
//    private MyDbHandler db;
//    private boolean isIncomingNumberPriority = false;
//    private ContactModel incomingCallContact;
//    private int ringerMode, requiredRingMode;
//    private AudioManager am;
//    private Context context;
//    Ringtone r;
//    @Override
//    public void onReceive(Context context, Intent intent) {
 //       try {
//            this.context = context;
//            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL"))
//            {
//                savedNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//                db = new MyDbHandler(context);
//                priorityContactsList = db.getAllContacts();
//                if (intent.getAction()!=null && intent.getAction().equals("android.intent.action.PHONE_STATE"))
//                {
//                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
//                        managePriorityContacts();
//                    }
//                    else {
//                        if(r.isPlaying())
//                            r.stop();
//                    }
//                }
//            }
//            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
//                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
//            }
//            else{
//                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
//                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//                int state = 0;
//                if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
//                    state = TelephonyManager.CALL_STATE_IDLE;
//                }
//                else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
//                    state = TelephonyManager.CALL_STATE_OFFHOOK;
//                }
//                else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
//                    state = TelephonyManager.CALL_STATE_RINGING;
//                }
//                if (number != null && !number.isEmpty() && !number.equals("null")) {
//                    onCallStateChanged(context, state, number);
//                    Log.d("TEST :","NUMBER =>"+number);
//                    return;
//                }


    //    }
//            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//
//            Toast.makeText(context," Receiver start for call" + number, Toast.LENGTH_SHORT).show();
//            if(number.equals("03017090646")){
//                int resID= R.raw.song;
//                MediaPlayer ring = MediaPlayer.create(context, resID);
//                ring.start();
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//    private void managePriorityContacts() {
//        for (ContactModel c:priorityContactsList)
//        {
//            String cNo = c.getMobileNumber().replaceAll("\\p{Z}","");
//            if(savedNumber.equals(cNo))
//            {
//                isIncomingNumberPriority = true;
//                incomingCallContact = c;
//                break;
//            }
//        }
//        if(incomingCallContact != null) {
//            am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            ringerMode = am.getRingerMode();
//            requiredRingMode = am.getRingerMode();
//            if (incomingCallContact.getRingMode().equals(Params.AM_RING_MODE))
//                setRingingMode();
//            else if (incomingCallContact.getRingMode().equals(Params.AM_SILENT_MODE))
//                setSilentMode();
//            else if (incomingCallContact.getRingMode().equals(Params.AM_VIBRATE_MODE)){
//                setVibratingMode();
//            }
//            else
//                Toast.makeText(context, "incming call contact ring mode: "+incomingCallContact.getRingMode(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void setSilentMode() {
//        requiredRingMode = AudioManager.RINGER_MODE_SILENT;
//        if (ringerMode != requiredRingMode) {
//            am.setRingerMode(requiredRingMode);
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            r = RingtoneManager.getRingtone(context, notification);
//            if(r.isPlaying())
//                r.stop();
//        }
//    }
//
//    private void setVibratingMode() {
//        requiredRingMode = AudioManager.RINGER_MODE_VIBRATE;
//        if (ringerMode != requiredRingMode) {
//            am.setRingerMode( AudioManager.RINGER_MODE_VIBRATE);
//            Uri ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            r = RingtoneManager.getRingtone(context, ring);
//            r.play();
//            Toast.makeText(context, "Ringer Mode Changed to: " + requiredRingMode, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void setRingingMode() {
//        requiredRingMode = AudioManager.RINGER_MODE_NORMAL;
//        if (ringerMode != requiredRingMode) {
//            am.setRingerMode( AudioManager.RINGER_MODE_NORMAL);
//            Uri ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//            r = RingtoneManager.getRingtone(context, ring);
//            r.play();
//            Toast.makeText(context, "Ringer Mode Changed to: " + requiredRingMode, Toast.LENGTH_SHORT).show();
//        }
//    }
//}
//Best code

//    public void onReceive(Context context, Intent intent) {

//    Bundle bundle = intent.getExtras();
//   phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
//
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        Ringtone r = RingtoneManager.getRingtone(context, uri);
//
//        if (intent.getAction()!=null && intent.getAction().equals("android.intent.action.PHONE_STATE")){
//
//            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//
//            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
//
//                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//                audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume/2, AudioManager.FLAG_PLAY_SOUND);
//
//                r.play();
//
//            }else {
//
//                r.stop();
//
//            }
//        }
//    }
