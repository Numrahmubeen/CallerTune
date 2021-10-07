package com.caller.tune.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.caller.tune.R;
import com.caller.tune.models.ContactModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhoneContactsAdapter extends RecyclerView.Adapter {

    private List<ContactModel> contactsList;
    private List<ContactModel> listSearch;
    private Context context;

    public PhoneContactsAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        listSearch = new ArrayList<>();
        contactsList = new ArrayList<>();
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(ContactModel item);
    }

    public void setItems(ArrayList<ContactModel> items) {
        if (items != null) {
            contactsList = items;
            listSearch.addAll(items);
            notifyDataSetChanged();
        }
    }

    private OnItemClickListener listener;


    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacts_rv, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vholder, int position) {

        ContactModel result = contactsList.get(position);
        ContactViewHolder holder = (ContactViewHolder) vholder;

        holder.contactName.setText(result.getName());

        String str = result.getMobileNumber().replaceAll("\\s", "");
        str = str.replaceAll("-", "");
        holder.contactNumber.setText(str);
        holder.bind(result);

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public void filter(String text) {
        contactsList.clear();
        if (text.isEmpty()) {
            contactsList.addAll(listSearch);
        } else {
            text = text.toLowerCase().replaceAll("\\s", "");
            for (ContactModel item : listSearch) {
                if (item.getName().toLowerCase().replaceAll("\\s", "").contains(text) || item.getMobileNumber().toLowerCase().replaceAll("\\s", "").contains(text)) {
                    contactsList.add(item);
                }
            }
        }
        notifyDataSetChanged();
//        contactsList.clear();
//        if(text.isEmpty()){
//            contactsList.addAll(listSearch);
//        } else{
//            text = text.toLowerCase();
//            for(ContactModel item: listSearch){
//                if(item.getName().toLowerCase().replaceAll("\\p{Z}","").contains(text) || item.getMobileNumber().toLowerCase().replaceAll("\\p{Z}","").contains(text)){
//                    contactsList.add(item);
//                }
//            }
//        }
//        notifyDataSetChanged();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber;
        CircleImageView civ;
        RelativeLayout relativeLayout;

        public ContactViewHolder(View itemView) {
            super(itemView);

            civ = itemView.findViewById(R.id.contact_civ);
            contactName = itemView.findViewById(R.id.contact_name_tv);
            contactNumber = itemView.findViewById(R.id.contact_number_tv);
            relativeLayout = itemView.findViewById(R.id.contact_rl);
        }

        public void bind(final ContactModel contact) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(contact);

                }
            });
            if (contact.getPhoto() != null) {

                Glide.with(context).asBitmap().load(contact.getPhoto()).centerCrop().into(new BitmapImageViewTarget(civ) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        civ.setImageDrawable(circularBitmapDrawable);
                    }
                });
            } else {
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
