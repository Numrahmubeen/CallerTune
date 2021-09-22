package com.caller.tune.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.CallLog;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.caller.tune.CallHistoryDetailActivity;
import com.caller.tune.R;
import com.caller.tune.models.ContactModel;
import com.caller.tune.models.RecentCall;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class RecentCallsAdapter extends Section {
    private List<RecentCall> recentCallList;
    private List<RecentCall> listSearch;
    private Context context;
    private String title;
    private boolean checkFrom;

    public RecentCallsAdapter(Context context, List<RecentCall> recentCalls, boolean check, String title) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_recent_calls_rv)
                .headerResourceId(R.layout.item_header_recent_call_rv)
                .build());
        this.context = context;
        this.title = title;
        recentCallList = recentCalls;
        listSearch = new ArrayList<>();
        listSearch.addAll(recentCalls);
        checkFrom = check;
    }
    @Override
    public int getContentItemsTotal() {
        return recentCallList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new RecentCallsViewHolder(view) ;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder vHolder, int position) {
        final RecentCallsViewHolder holder = (RecentCallsViewHolder) vHolder;

        final RecentCall result = recentCallList.get(position);
        if (result.getName() != null && result.getName().length() != 0) {
            holder.contactName.setText(result.getName());
        } else
            holder.contactName.setText(result.getPhoneNumber());
        holder.callTime.setText(result.getCallTime());

        if(checkFrom){
            holder.container_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CallHistoryDetailActivity.class);
                    intent.putExtra("phoneNumber",result.getPhoneNumber());
                    intent.putExtra("name",result.getName());
                    intent.putExtra("callerDp",result.getCallerDp());
                    context.startActivity(intent);
                    //                Toast.makeText(context, "Sim Id is: "+result.getSubscriberId(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        switch (result.getCallTyp()) {
            case "Outgoing":
                holder.callType_iv.setImageResource(R.drawable.ic_call_outgoing);
                break;
            case "Incoming":
                holder.callType_iv.setImageResource(R.drawable.ic_call_recieved);
                break;
            case "Missed":
                holder.callType_iv.setImageResource(R.drawable.ic_missed_call);
                break;
            case "Blocked":
                holder.callType_iv.setImageResource(R.drawable.ic_call_block);
                break;
            case "Rejected":
                holder.callType_iv.setImageResource(R.drawable.ic_rejected_call);
                break;
            default:
                holder.callType_iv.setImageResource(R.drawable.ic_known);

        }
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
        if(phoneAccountHandleList.size() < 2)
        {
            holder.sim_iv.setVisibility(View.GONE);
        }
        else {
//            if(sim1CarrierId != null && sim2CarrierId != null)
//            {
//                if(result.getSubscriberId().equals(sim1CarrierId)) {
//                    holder.sim_iv.setImageResource(R.drawable.ic_dual_sim_1);
//                }
//                else if(result.getSubscriberId().equals(sim2CarrierId)){
//                    holder.sim_iv.setImageResource(R.drawable.ic_dual_sim_2);
//                }
//                else {
//                    holder.sim_iv.setVisibility(View.GONE);
//                }
//            }
        }

    }
    public void filter(String text) {
        recentCallList.clear();
        if(text.isEmpty()){
            recentCallList.addAll(listSearch);
        } else{
            text = text.toLowerCase();
            for(RecentCall item: listSearch){
                if(item.getName().toLowerCase().replaceAll("\\p{Z}","").contains(text) || item.getPhoneNumber().toLowerCase().replaceAll("\\p{Z}","").contains(text)){
                    recentCallList.add(item);
                }
            }
        }
    }
    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.tvTitle.setText(title);
    }

    final class HeaderViewHolder extends RecyclerView.ViewHolder {

        final TextView tvTitle;

        HeaderViewHolder(@NonNull View view) {
            super(view);

            tvTitle = view.findViewById(R.id.txt_title);
        }
    }
    public class RecentCallsViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, callTime;
        ImageView callType_iv, sim_iv;
        RelativeLayout container_rl;

        public RecentCallsViewHolder(View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.caller_tv);
            callTime = itemView.findViewById(R.id.callTime_tv);
            callType_iv = itemView.findViewById(R.id.callType_iv);
            sim_iv = itemView.findViewById(R.id.callSim_iv);
            container_rl = itemView.findViewById(R.id.item_recent_calls_rv);
        }
    }
}
