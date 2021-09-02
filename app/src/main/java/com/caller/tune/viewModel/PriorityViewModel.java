package com.caller.tune.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PriorityViewModel extends ViewModel {
    MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    public void setText(String s)
    {
        mutableLiveData.setValue(s);
    }
    public MutableLiveData<String> getText(){
        return mutableLiveData;
    }
}
