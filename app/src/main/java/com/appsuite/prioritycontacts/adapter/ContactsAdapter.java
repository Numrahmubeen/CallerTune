package com.appsuite.prioritycontacts.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.appsuite.prioritycontacts.R;
import com.appsuite.prioritycontacts.models.ContactModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter {
    private List<ContactModel> contactsList;
    private ArrayList<ContactModel> mySelectedList;
    private List<ContactModel> listSearch;
    private Context context;

    public ContactsAdapter(Context context) {
        this.context = context;
        contactsList = new ArrayList<>();
        mySelectedList = new ArrayList<>();

    }
    public void setItems(ArrayList<ContactModel> items) {
        if (items != null) {
            contactsList = items;
            listSearch =new ArrayList<>();
            listSearch.addAll(items);
            notifyDataSetChanged();
        }
    }

    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_contact_rv, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vholder, int position) {

        ContactModel result = contactsList.get(position);
        ContactViewHolder holder = (ContactViewHolder) vholder;

        holder.contactName.setText(result.getName());
        holder.contactNumber.setText(result.getMobileNumber());
        holder.bind(result);

    }


    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public ArrayList<ContactModel> getSelectedContacts()

    {
        return mySelectedList;
    }

    public void filter(String text) {
        contactsList.clear();
        if(text.isEmpty()){
            contactsList.addAll(listSearch);
        } else{
            text = text.toLowerCase().replaceAll("\\p{Z}","");
            for(ContactModel item: listSearch){
                if(item.getName().toLowerCase().replaceAll("\\p{Z}","").contains(text) || item.getMobileNumber().toLowerCase().replaceAll("\\p{Z}","").contains(text)){
                    contactsList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber;
        CircleImageView civ;
        CheckBox checkBox;
        RelativeLayout relativeLayout;

        public ContactViewHolder(View itemView) {
            super(itemView);

            civ = itemView.findViewById(R.id.contact_civ);
            contactName = itemView.findViewById(R.id.contact_name_tv);
            contactNumber = itemView.findViewById(R.id.contact_number_tv);
            checkBox = itemView.findViewById(R.id.checkbox);
            relativeLayout = itemView.findViewById(R.id.contact_rl);
        }
        public void bind(final ContactModel contact)
        {
            checkBox.setChecked(contact.isSelected() ? true : false);
            itemView.setOnClickListener(v -> {

                contact.setSelected(!contact.isSelected());
                checkBox.setChecked(contact.isSelected() ? true : false);
                if(contact.isSelected())
                {
                    mySelectedList.add(contact);
                }else if(!contact.isSelected() && mySelectedList.contains(contact)){
                    mySelectedList.remove(contact);

                }

            });
            checkBox.setOnClickListener(v -> {
                contact.setSelected(!contact.isSelected());
                if(contact.isSelected())
                {
                    mySelectedList.add(contact);
                }else if(!contact.isSelected() && mySelectedList.contains(contact)){
                    mySelectedList.remove(contact);

                }
            });
            if(contact.getPhoto()!=null){

                Glide.with(context).asBitmap().load(contact.getPhoto()).centerCrop().into(new BitmapImageViewTarget(civ) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        civ.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
            else {
                Glide.with(context).asBitmap().load(R.drawable.ic_person).centerCrop().into(new BitmapImageViewTarget(civ) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        civ.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }
        }


    }


}
