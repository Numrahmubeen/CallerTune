package com.caller.tune.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.caller.tune.models.ContactModel;
import com.caller.tune.models.RecentCall;
import com.caller.tune.repository.CallLogRepository;
import com.caller.tune.repository.ContactRepository;

import java.util.ArrayList;

public class CallLogViewModel extends AndroidViewModel {
    private CallLogRepository repository;
    private MutableLiveData<ArrayList<RecentCall>> recentCalls;

    private static Application application;

    public CallLogViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        repository = new CallLogRepository(application);
        recentCalls = new MutableLiveData<>();
    }


    public MutableLiveData<ArrayList<RecentCall>> getRecentCalls() {
        recentCalls = repository.fetchCallLogs();
        return recentCalls;
    }

}
