package com.caller.tune.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.caller.tune.R;
import com.caller.tune.data.MyDbHandler;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PriorityContactsAdapter extends RecyclerView.Adapter {
    private ArrayList<ContactModel> contactsList;
    private Context context;
    private MyDbHandler db;
    boolean action_mode = false;
    static ArrayList<ContactModel> removedList = new ArrayList<>();
    public static androidx.appcompat.view.ActionMode actionMode;
    boolean selectAll=false,unSelectAll = false;

    public PriorityContactsAdapter(Context context, ArrayList<ContactModel> contacts) {
        this.context = context;
        contactsList = contacts;
        db = new MyDbHandler(context);
    }

    @Override
    public PriorityContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_priority_contact_rv, parent, false);
        return new PriorityContactViewHolder(view,(Activity) context,this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vholder, int position) {

        ContactModel result = contactsList.get(position);
        PriorityContactViewHolder holder = (PriorityContactViewHolder) vholder;
        switch (result.getCallRingMode()){
            case Params.AM_SILENT_MODE:
                holder.ringMode_iv.setImageResource(R.drawable.call_silent);
                break;
            case Params.AM_VIBRATE_MODE:
                holder.ringMode_iv.setImageResource(R.drawable.call_vibration);
                break;
            case Params.AM_SKIP_MODE:
                holder.ringMode_iv.setImageResource(R.drawable.call_default);
                break;
            default:
                holder.ringMode_iv.setImageResource(R.drawable.call_sound);

        }
        holder.ringMode_iv.setOnClickListener(v -> {
            if(result.getCallRingMode().equals(Params.AM_RING_MODE)){
                holder.ringMode_iv.setImageResource(R.drawable.call_vibration);
                result.setCallRingMode(Params.AM_VIBRATE_MODE);
                db.updateContact(result);
                Snackbar.make(holder.itemView, "Vibration", Snackbar.LENGTH_SHORT).show();
            }
            else if(result.getCallRingMode().equals(Params.AM_VIBRATE_MODE)){
                holder.ringMode_iv.setImageResource(R.drawable.call_silent);
                result.setCallRingMode(Params.AM_SILENT_MODE);
                db.updateContact(result);
                Snackbar.make(holder.itemView, "Silent", Snackbar.LENGTH_SHORT).show();
            }else if(result.getCallRingMode().equals(Params.AM_SILENT_MODE)){
                holder.ringMode_iv.setImageResource(R.drawable.call_default);
                result.setCallRingMode(Params.AM_SKIP_MODE);
                db.updateContact(result);
                Snackbar.make(holder.itemView, "Default Mode App will skip this Number", Snackbar.LENGTH_SHORT).show();
            }else if(result.getCallRingMode().equals(Params.AM_SKIP_MODE)){
                holder.ringMode_iv.setImageResource(R.drawable.call_sound);
                result.setCallRingMode(Params.AM_RING_MODE);
                db.updateContact(result);
                Snackbar.make(holder.itemView, "Sound", Snackbar.LENGTH_SHORT).show();
            }
        });
        holder.contactName.setText(result.getName());
        holder.contactNumber.setText(result.getMobileNumber());
        if(!action_mode)
        {
            holder.isCheck_iv.setVisibility(View.GONE);
        }
        if(action_mode && selectAll)
        {
            holder.isCheck_iv.setVisibility(View.VISIBLE);
        }
        else if(action_mode && unSelectAll)
        {
            holder.isCheck_iv.setVisibility(View.GONE);
        }

        holder.row.setOnLongClickListener(v -> {
            actionMode = ((AppCompatActivity)context).startSupportActionMode(new ContextualCallBack(holder));
            return true;
        });
        holder.row.setOnClickListener(v -> {
            if(action_mode){
                if (!removedList.contains(contactsList.get(position))) {
                    removedList.add(contactsList.get(position));
                    holder.isCheck_iv.setVisibility(View.VISIBLE);
                    actionMode.setTitle(String.valueOf(removedList.size()) + " " + " SELECTED");
                } else {
                    holder.isCheck_iv.setVisibility(View.GONE);
                    removedList.remove(contactsList.get(position));
                    actionMode.setTitle(String.valueOf(removedList.size()) + " " + " SELECTED");

                }
            }else {
                Toast.makeText(context, "Ring Mode: " + result.getCallRingMode(), Toast.LENGTH_SHORT).show();
            }

        });

        if(result.getPhotoUri()!=null){
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(result.getId())));
            Bitmap photo = null;
            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }
            if(photo != null)
            {
                Glide.with(context).asBitmap().load(photo).centerCrop().into(new BitmapImageViewTarget(holder.civ) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    holder.civ.setImageDrawable(circularBitmapDrawable);
                }
            });
            }
            else {
                Glide.with(context).asBitmap().load(R.drawable.ic_person).centerCrop().into(new BitmapImageViewTarget(holder.civ) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        holder.civ.setImageDrawable(circularBitmapDrawable);
                    }
                });
            }

        }
        else {
            Glide.with(context).asBitmap().load(R.drawable.ic_person).centerCrop().into(new BitmapImageViewTarget(holder.civ) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    holder.civ.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public  class PriorityContactViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber;
        CircleImageView civ;
        ImageView ringMode_iv,isCheck_iv;

        RelativeLayout row;

        Activity activity;
        PriorityContactsAdapter recyclerViewAdapter;

        public PriorityContactViewHolder(View itemView,Activity activity,PriorityContactsAdapter rView) {
            super(itemView);

            this.activity = activity;
            recyclerViewAdapter = rView;

            row = itemView.findViewById(R.id.contact_rl);
            civ = itemView.findViewById(R.id.contact_civ);
            contactName = itemView.findViewById(R.id.contact_name_tv);
            contactNumber = itemView.findViewById(R.id.contact_number_tv);
            ringMode_iv = itemView.findViewById(R.id.callRing_iv);
            isCheck_iv = itemView.findViewById(R.id.isCheck_iv);
        }

    }
    class ContextualCallBack implements androidx.appcompat.view.ActionMode.Callback {

        PriorityContactViewHolder holder;

        ContextualCallBack(PriorityContactViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.delete_menu, menu);
            action_mode = true;
            holder.recyclerViewAdapter.notifyDataSetChanged();
            removedList = new ArrayList<>();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            mode.setTitle(R.string.app_name);
            return false;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.del_item) {
                holder.recyclerViewAdapter.removeContacts(removedList);
                for(ContactModel contactModel:removedList)
                    holder.recyclerViewAdapter.deleteContact(contactModel);
                holder.recyclerViewAdapter.notifyDataSetChanged();
                removedList.clear();
                actionMode.finish();

            }
            else if(id == R.id.select_all){
                if(selectAll)
                {
                    removedList.clear();
                    selectAll = false;
                    unSelectAll = true;
                    holder.recyclerViewAdapter.notifyDataSetChanged();
                    actionMode.setTitle(String.valueOf(removedList.size()) + " " + " SELECTED");


                }else
                    {
                        for (ContactModel contactModel : contactsList)
                            removedList.add(contactModel);
                        unSelectAll = false;
                        selectAll = true;
                        holder.recyclerViewAdapter.notifyDataSetChanged();
                        actionMode.setTitle(String.valueOf(removedList.size()) + " " + " SELECTED");

                    }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
            action_mode = false;
            holder.recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private void deleteContact(ContactModel contactModel) {
        db.deleteContact(contactModel);
    }

    private void removeContacts(ArrayList<ContactModel> removedList) {
        for (ContactModel contact : removedList)
            contactsList.remove(contact);
        notifyDataSetChanged();

    }
}
