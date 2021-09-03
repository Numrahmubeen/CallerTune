package com.caller.tune;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import static android.Manifest.permission.READ_PHONE_STATE;

public class FloatingActivity extends Service {

    private WindowManager windowmanager;
    private View floatingview;
    private TextView senderName_tv, senderMsg_tv, closeButtonExpanded_tv;
    private ImageView senderDp_iv, expand_iv, sendMsg_iv, callSender_iv;
    private EditText sendMsg_et;
    private String phoneNumber;

    public FloatingActivity() {
    }

    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        floatingview = LayoutInflater.from(this).inflate(R.layout.activity_floating, null);

        closeButtonExpanded_tv = (TextView) floatingview.findViewById(R.id.close_box);
        senderName_tv = floatingview.findViewById(R.id.floating_senderName_tv);
        senderMsg_tv = floatingview.findViewById(R.id.floating_senderMsg_tv);
        senderDp_iv = floatingview.findViewById(R.id.floating_sender_iv);
        expand_iv = floatingview.findViewById(R.id.floating_expandMsg_iv);
        sendMsg_iv = floatingview.findViewById(R.id.floating_sendMsg_iv);
        callSender_iv = floatingview.findViewById(R.id.floating_call_iv);
        sendMsg_et = floatingview.findViewById(R.id.floating_sendMsg_et);

        if(SMSReceiver.contactModel.getName() != null){
            senderName_tv.setText(SMSReceiver.contactModel.getName() );
            if(SMSReceiver.contactModel.getPhoto() != null)
                senderDp_iv.setImageBitmap(SMSReceiver.contactModel.getPhoto());
        }
        else
        {
            senderName_tv.setText(SMSReceiver.contactModel.getMobileNumber());
        }
        phoneNumber = SMSReceiver.contactModel.getMobileNumber();
        senderMsg_tv.setText(SMSReceiver.contactModel.getMsgRingMode());

        expand_iv.setOnClickListener(v -> {
            Uri uri = Uri.parse("smsto:" + phoneNumber);
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            stopSelf();
        });
        callSender_iv.setOnClickListener(v -> {
//            boolean dualActive = checkSimAvailability();
//            if (dualActive) {
//                selectSim();
//            }
//            else
            makeCall(-1);
            stopSelf();

        });

        sendMsg_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendMsg_et.getText().length()>0){
                  SmsManager sms = SmsManager.getDefault();
                  sms.sendTextMessage(phoneNumber, null, sendMsg_et.getText().toString(), null, null);
                  stopSelf();
                }
                else
                    Toast.makeText(FloatingActivity.this, "Enter some text to send", Toast.LENGTH_SHORT).show();

            }
        });
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY ,
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER ;// Initially view will be
        // added to top-left
        // corner
        params.x = 0;
        params.y = 100;

        windowmanager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowmanager.addView(floatingview, params);

//        final View collapsedView = floatingview
//                .findViewById(R.id.collapse_view);
        final View expandedView = floatingview
                .findViewById(R.id.expanded_container);

//        // Set the close button
//        ImageView closeButtonCollapsed = (ImageView) floatingview
//                .findViewById(R.id.close_btn);
//        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopSelf();
//            }
//        });


//        closeButtonCollapsed.bringToFront();
        closeButtonExpanded_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        floatingview.findViewById(R.id.root_container).setOnTouchListener(
                new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:

                                initialX = params.x;
                                initialY = params.y;

                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;
                            case MotionEvent.ACTION_UP:
                                int Xdiff = (int) (event.getRawX() - initialTouchX);
                                int Ydiff = (int) (event.getRawY() - initialTouchY);

                                if (Xdiff < 10 && Ydiff < 10) {
//                                    if (isViewCollapsed()) {
//                                        collapsedView.setVisibility(View.GONE);
//                                        expandedView.setVisibility(View.VISIBLE);
//                                    }
                                }
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                params.x = initialX
                                        + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY
                                        + (int) (event.getRawY() - initialTouchY);

                                windowmanager
                                        .updateViewLayout(floatingview, params);
                                return true;
                        }
                        return false;
                    }
                });
    }

//    private boolean isViewCollapsed() {
//        return floatingview == null
//                || floatingview.findViewById(R.id.collapse_view)
//                .getVisibility() == View.VISIBLE;
//    }
private boolean checkSimAvailability() {
    // cursor_call_logs.getColumnIndexOrThrow("subscription_id")
    final SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return false;
    }
    final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
    int simCount = activeSubscriptionInfoList.size();
    if(simCount > 1){
        return true;
    }
    return false;
}

    private void selectSim(){
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_sim);

        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.select_sim_radio_group);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.sim1_rb)
            {
                makeCall(0);
                dialog.dismiss();
            }
            else if(checkedId == R.id.sim2_rb)
            {
                makeCall(1);
                dialog.dismiss();
            }
        });
        dialog.show();


    }

    private void makeCall(int simNumber) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{READ_PHONE_STATE}, 2);
                return;
            }
            List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
            if (simNumber == 0) {  // simNumber = 0 or 1 according to sim......
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0)
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
            } else if(simNumber == 1) {
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 1)
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingview != null)
            windowmanager.removeView(floatingview);
    }

}