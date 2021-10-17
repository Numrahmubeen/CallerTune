package com.appsuite.prioritycontacts.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appsuite.prioritycontacts.CallHistoryDetailActivity;
import com.appsuite.prioritycontacts.R;
import com.appsuite.prioritycontacts.models.RecentCall;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

import static android.Manifest.permission.READ_PHONE_STATE;

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
                }
            });
        }
        else {
            holder.call_iv.setVisibility(View.GONE);
        }
        holder.call_iv.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "READ_PHONE_STATE Permission is missing", Toast.LENGTH_SHORT).show();
            } else
            {
                if(!isDefaultSimSetForCall()){
                    selectSim(result.getPhoneNumber());
                }
                else
                    makeCall(-1,result.getPhoneNumber());            }
        });

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
            ActivityCompat.requestPermissions((Activity) context, new String[]{READ_PHONE_STATE}, 2);
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
    private void selectSim(String phoneNumber){
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_sim);

        TextView sim1_tv = dialog.findViewById(R.id.sim1Choose_tv);
        TextView sim2_tv = dialog.findViewById(R.id.sim2Choose_tv);

        sim1_tv.setOnClickListener(v -> {
            makeCall(0,phoneNumber);
            dialog.dismiss();
        });
        sim2_tv.setOnClickListener(v -> {
            makeCall(1,phoneNumber);
            dialog.dismiss();
        });
        dialog.show();


    }

    boolean isDefaultSimSetForCall() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{READ_PHONE_STATE}, 2);
        } else {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            PhoneAccountHandle defaultPhoneAccount = telecomManager.getDefaultOutgoingPhoneAccount(Uri.fromParts("tel", "text", null).getScheme());
            if (defaultPhoneAccount != null) {
                return true;
            }
        }
        return false;
    }


    private void makeCall(int simNumber,String phoneNumber) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber));
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions((Activity) context, new String[]{READ_PHONE_STATE}, 2);
                return;
            }
            intent.setPackage("com.android.server.telecom");
            List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
            if (simNumber == 0) {  // simNumber = 0 or 1 according to sim......
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0)
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
            } else if(simNumber == 1) {
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 1)
                    intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
            }

            context.startActivity(intent);
        }
        else
            Toast.makeText(context, "Your device incompatible to make a call from this app.", Toast.LENGTH_SHORT).show();
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
        ImageView callType_iv, sim_iv, call_iv;
        RelativeLayout container_rl;


        public RecentCallsViewHolder(View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.caller_tv);
            callTime = itemView.findViewById(R.id.callTime_tv);
            callType_iv = itemView.findViewById(R.id.callType_iv);
            sim_iv = itemView.findViewById(R.id.callSim_iv);
            container_rl = itemView.findViewById(R.id.item_recent_calls_rv);
            call_iv = itemView.findViewById(R.id.item_recentCall_call_iv);
        }
    }
}
