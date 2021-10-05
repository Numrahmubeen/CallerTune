package com.caller.tune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;
import com.caller.tune.params.Preference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.mutasem.slidetoanswer.SwipeToAnswerView;

import static com.caller.tune.CallService.r;
import static com.caller.tune.OngoingCall.state;

public class CallActivity extends AppCompatActivity {

    private CompositeDisposable disposables = new CompositeDisposable();
    public static String number;
   private SwipeToAnswerView rejectCall_iv, answerCall_iv;
    private ImageView  caller_iv, hangUp_call_iv;
    private TextView callState_tv, callerName_tv, speakerOn_tv, holdCall_tv, muteCall_tv, callerNumber_tv;
    private Chronometer chronometer;
    private boolean isHold = false;
    private boolean isTimerOn = false;
    private CardView inCall_cv;
    private LinearLayout callRinging_ll;
    private boolean enableHold = false,enableMute = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        setContentView(R.layout.activity_call);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if(keyguardManager!=null)
                keyguardManager.requestDismissKeyguard(this, null);
        }
        else
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        Preference preference = new Preference(this);

        bindView();
        clickListeners();

        Intent i = getIntent();
        if(i.getData() != null)
        {
            number = getIntent().getData().getSchemeSpecificPart();
            preference.setNumber(number);
        }
        else {
            number = preference.getNumber();
        }


    }

    private void clickListeners() {
        answerCall_iv.setSlideListner(() ->
        {
            OngoingCall.answer();
            rejectCall_iv.stopAnimation();
        }
        );

        rejectCall_iv.setSlideListner(() -> {
            OngoingCall.hangup();
            answerCall_iv.stopAnimation();
        });
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        muteCall_tv.setOnClickListener(v -> {

            if(enableMute) {
                if (audioManager.isMicrophoneMute() == false) {
                    audioManager.setMicrophoneMute(true);
                    TextViewCompat.setCompoundDrawableTintList(muteCall_tv, ColorStateList.valueOf(getResources().getColor(R.color.green)));

                } else {
                    audioManager.setMicrophoneMute(false);
                    TextViewCompat.setCompoundDrawableTintList(muteCall_tv, ColorStateList.valueOf(getResources().getColor(R.color.black)));

                }
            }
        });

        speakerOn_tv.setOnClickListener(v -> {
           toggleSpeaker();
        });
        holdCall_tv.setOnClickListener(v -> {
            if(enableHold){
                if(isHold)
                {
                    OngoingCall.unHold();
                    isHold = false;
                    TextViewCompat.setCompoundDrawableTintList(holdCall_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black)));

                }
                else
                {
                    OngoingCall.hold();
                    isHold = true;
                    TextViewCompat.setCompoundDrawableTintList(holdCall_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.green)));
                }
            }
        });
        hangUp_call_iv.setOnClickListener(v -> {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            OngoingCall.hangup();
        });
    }

    public void toggleSpeaker() {
        AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        boolean isSpeakerOn = am.isSpeakerphoneOn();
        int earpiece = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
        int speaker = CallAudioState.ROUTE_SPEAKER;

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P){
            if(isSpeakerOn){
                CallService.getInstance().setAudioRoute(earpiece);
                TextViewCompat.setCompoundDrawableTintList(speakerOn_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black)));
            }
            else {
                CallService.getInstance().setAudioRoute(speaker);
                TextViewCompat.setCompoundDrawableTintList(speakerOn_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.green)));

            }

        } else {
            if(isSpeakerOn)
            {
                am.setSpeakerphoneOn(false);
                TextViewCompat.setCompoundDrawableTintList(speakerOn_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black)));
            }
            else {
                am.setSpeakerphoneOn(true);
                TextViewCompat.setCompoundDrawableTintList(speakerOn_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.green)));

            }
        }

    }

    private void bindView() {
        answerCall_iv = findViewById(R.id.answer_call_iv);
        rejectCall_iv = findViewById(R.id.reject_call_iv);
        callState_tv = findViewById(R.id.phone_state_tv);
        callerName_tv = findViewById(R.id.caller_name_tv);
        caller_iv = findViewById(R.id.caller_iv);
        chronometer = findViewById(R.id.chronometer);
        holdCall_tv = findViewById(R.id.hold_call_tv);
        speakerOn_tv = findViewById(R.id.speaker_on_tv);
        muteCall_tv = findViewById(R.id.mute_call_tv);
        hangUp_call_iv = findViewById(R.id.hangUp_call_iv);
        callRinging_ll = findViewById(R.id.ringing_ll);
        inCall_cv = findViewById(R.id.inCall_cv);
        callerNumber_tv = findViewById(R.id.caller_number_tv);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onStart() {
        super.onStart();


        // Subscribe to state change -> call updateUi when change
        new OngoingCall();
        Disposable disposable = state.subscribe(this::updateUi);
        disposables.add(disposable);

        // Subscribe to state change (only when disconnected) -> call finish to close phone call
        new OngoingCall();
        Disposable disposable2 = state
                .filter(state -> state == Call.STATE_DISCONNECTED)
                .delay(1, TimeUnit.SECONDS)
                .firstElement()
                .subscribe(this::finish);

        disposables.add(disposable2);
    }

    // Call to Activity finish
    void finish(Integer state){
        finish();
    }
//todo add bluetooth functionality during call
    // Set the UI for the call
    @SuppressLint("SetTextI18n")
    public void updateUi(Integer state) {
        // Set callInfo text by the state
        ContactModel contact = retrieveContactInfo(this,number);
        if(contact != null && contact.getName() != null)
        {
            callerName_tv.setText(contact.getName());
            callerNumber_tv.setText(number);
            if(contact.getPhoto() != null)
                caller_iv.setImageBitmap(contact.getPhoto());
        }
        else {
            callerName_tv.setText(number);
            callerNumber_tv.setVisibility(View.GONE);
            caller_iv.setVisibility(View.GONE);
        }
//        callState_tv.setText(CallStateString.asString(state).toLowerCase()+"\n"+number);

        if (state == Call.STATE_RINGING)
        {
            callRinging_ll.setVisibility(View.VISIBLE);
            inCall_cv.setVisibility(View.GONE);
            callState_tv.setText("Incoming Call");
            isTimerOn = false;
            disableHoldAndMute();
        }else
            callRinging_ll.setVisibility(View.GONE);


        if(state == Call.STATE_DIALING){
            disableHoldAndMute();
            callState_tv.setText("Calling..");
            inCall_cv.setVisibility(View.VISIBLE);
            isTimerOn = false;
        }

        if(state == Call.STATE_ACTIVE){
            callState_tv.setVisibility(View.GONE);
            inCall_cv.setVisibility(View.VISIBLE);
            if(!isTimerOn)
                chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setVisibility(View.VISIBLE);
            isTimerOn = true;
            enableHoldAndMute();
        }

        if(state == Call.STATE_DISCONNECTED)
        {
            chronometer.stop();
            if(isTimerOn)
            {
                callState_tv.setText(chronometer.getText().toString());
            }
            else {
                callState_tv.setText("0.00 sec");
            }
            callState_tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_end_call,0,0,0);
            callState_tv.setVisibility(View.VISIBLE);
            chronometer.setVisibility(View.GONE);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            disableHoldAndMute();

        }

        if (state == Call.STATE_DIALING ||  state == Call.STATE_ACTIVE)
        {
            inCall_cv.setVisibility(View.VISIBLE);
            callRinging_ll.setVisibility(View.GONE);
        }

    }
    void enableHoldAndMute(){
        enableHold = true;
        enableMute = true;
        TextViewCompat.setCompoundDrawableTintList(holdCall_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black)));
        TextViewCompat.setCompoundDrawableTintList(muteCall_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.black)));
    }
    void disableHoldAndMute(){
        enableHold = false;
        enableMute = false;
        TextViewCompat.setCompoundDrawableTintList(holdCall_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.gray)));
        TextViewCompat.setCompoundDrawableTintList(muteCall_tv, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.gray)));
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

    @Override
    public void onStop() {
        super.onStop();
        disposables.clear();
    }

    @SuppressLint("NewApi")
    public static void start(Context context, Call call) {
        context.startActivity(new Intent(context, CallActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.getDetails().getHandle()));
    }

}
