package com.caller.tune;


import android.os.Build;
import android.telecom.Call;

import androidx.annotation.RequiresApi;

import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.M)
public final class OngoingCall {
    public static final BehaviorSubject<Integer> state;
    private static final Call.Callback callback;
    private static Call call;

    public final BehaviorSubject getState() {
        return state;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public final void setCall(Call value) {
        if (call != null) {
            call.unregisterCallback(callback);
        }

        if (value != null) {
            value.registerCallback(callback);
            state.onNext(value.getState());
        }

        call = value;
    }

    // Anwser the call
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void answer() {
        call.answer(0);
    }

    // Hangup the call
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void hangup() {
        call.disconnect();
    }

    static {
        // Create a BehaviorSubject to subscribe
        state = BehaviorSubject.create();
        callback = new Call.Callback() {
            public void onStateChanged(Call call, int newState) {
                Timber.d(call.toString());
                // Change call state
                OngoingCall.state.onNext(newState);
            }
        };
    }
}
