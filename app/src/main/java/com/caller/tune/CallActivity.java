package com.caller.tune;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.telecom.Call;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.caller.tune.OngoingCall.state;

public class CallActivity extends AppCompatActivity {

    private CompositeDisposable disposables = new CompositeDisposable();
    public static String number;
    private Button answer, hangup;
    private TextView callInfo;
    private ArrayList<ContactModel> priorityContactsList;
    private MyDbHandler db;
    private boolean isIncomingNumberPriority = false;
    private ContactModel incomingCallContact;
    private int ringerMode, requiredRingMode;
    private String ringerModeName;
    private AudioManager am;
    Ringtone r;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        answer = findViewById(R.id.answer);
        hangup = findViewById(R.id.hangup);
        callInfo = findViewById(R.id.callInfo);
        db = new MyDbHandler(CallActivity.this);
        priorityContactsList = db.getAllContacts();
        number = getIntent().getData().getSchemeSpecificPart();

    }

    private void managePriorityContacts() {
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
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
            am.setRingerMode(requiredRingMode);
            Uri ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(this, ring);
            r.play();
            Toast.makeText(this, "Ringer Mode Changed to: Sound", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void onStart() {
        super.onStart();

        answer.setOnClickListener(v -> OngoingCall.answer());

        hangup.setOnClickListener(v -> OngoingCall.hangup());

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

    // Set the UI for the call
    @SuppressLint("SetTextI18n")
    public void updateUi(Integer state) {
        // Set callInfo text by the state
        callInfo.setText(CallStateString.asString(state).toLowerCase()+"\n"+number);

        if (state == Call.STATE_RINGING)
        {
            managePriorityContacts();
            answer.setVisibility(View.VISIBLE);
        }
        else{
            answer.setVisibility(View.GONE);
            if(r != null){
                if(r.isPlaying())
                    r.stop();
            }
        }
        if(state == Call.STATE_DISCONNECTED)
        {
            if(ringerMode != requiredRingMode)
            {
                am.setRingerMode(ringerMode);
                incomingCallContact = null;
                if(ringerModeName != null){
                    Toast.makeText(this, "Ringer Mode Changed to: "+ ringerModeName, Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (state == Call.STATE_DIALING || state == Call.STATE_RINGING || state == Call.STATE_ACTIVE)
            hangup.setVisibility(View.VISIBLE);
        else
            hangup.setVisibility(View.GONE);
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
