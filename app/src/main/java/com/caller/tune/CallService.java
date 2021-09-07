package com.caller.tune;

import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;

import androidx.annotation.RequiresApi;

import com.caller.tune.CallActivity;
import com.caller.tune.OngoingCall;

@RequiresApi(api = Build.VERSION_CODES.M)
public class CallService extends InCallService {
    public static CallService sInstance;


    @Override
    public void onCallAdded(Call call) {
        sInstance = this;
        new OngoingCall().setCall(call);
        CallActivity.start(this, call);
    }

    @Override
    public void onCallRemoved(Call call) {
        new OngoingCall().setCall(null);
    }

    public static CallService getInstance(){
        return sInstance;
    }
}
