package com.caller.tune.viewModels;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.caller.tune.models.ContactModel;
import com.caller.tune.repository.ContactRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactViewModel extends AndroidViewModel {
    private ContactRepository repository;
    private MutableLiveData<ArrayList<ContactModel>> contacts;

    private static Application application;
    public ContactViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        repository = new ContactRepository(application);
        contacts = new MutableLiveData<>();
    }
    //todo load data in contacts rv without search
    public MutableLiveData<ArrayList<ContactModel>> getContacts() {
        contacts = repository.fetchContacts();
        return contacts;
    }

}

