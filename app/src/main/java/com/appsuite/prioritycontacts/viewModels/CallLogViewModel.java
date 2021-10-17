package com.appsuite.prioritycontacts.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.appsuite.prioritycontacts.models.RecentCall;
import com.appsuite.prioritycontacts.repository.CallLogRepository;

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
