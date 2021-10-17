package com.appsuite.prioritycontacts.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.appsuite.prioritycontacts.models.ContactModel;
import com.appsuite.prioritycontacts.repository.ContactRepository;

import java.util.ArrayList;

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
    public MutableLiveData<ArrayList<ContactModel>> getContacts() {
        contacts = repository.fetchContacts();
        return contacts;
    }

}

