package com.caller.tune.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.caller.tune.R;

public class ExpandableFaqsAdapter extends BaseExpandableListAdapter {
    private Context context;

    public ExpandableFaqsAdapter(Context context){
        this.context = context;
    }

    String[] faqs = {
            "How many priority contacts can be add in Free membership?",
            "Sometimes, Don't want to ring our phone even for important contacts!",
            "Can I control volume for important contacts?",
            "In settings we can see volume for message.",
            "We can't use some feature, but why?",
            "What is our next plan for priority contacts?"
    };

    String [][] answer = { {"You can add up to 6 contacts in Free membership"},
            {"Yes, Deactivate app that time. You can find 'Deactivate' button in our app home screen."},
            {"Yes! \n" +
                    "You can control volume from settings!"},
            {"Yes! \n" +
                    "In future update we take message feature like call."},
            {"In some feature have a icon 'PRO'. Those are only for premium user!"},
            {"We have many plan and we will process those step by step. Let's know about our some future plan: \n" +
                    "\n" +
                    "* Message notification Sound, Vibration & silent option.\n" +
                    "* Schedule Phone Sound Mode Control\n" +
                    "* Spam & Sim Company disturb call / message protection. \n" +
                    "\n" +
                    "  And many more! But we need time, day by day we will take those feature.\n" +
                    "Let us know what feature you need first!"}

    };

    @Override
    public int getGroupCount() {
        return faqs.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return answer[groupPosition].length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return faqs[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return answer[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String qFaq = (String) getGroup(groupPosition);
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_faqs_title,null);
        }
        TextView qFaq_tv = convertView.findViewById(R.id.item_faq_title_tv);
        qFaq_tv.setText(qFaq);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String ansFaq = (String) getChild(groupPosition,childPosition);
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.items_faqs_ans,null);
        }
        TextView ansFaq_tv = convertView.findViewById(R.id.item_faq_ans_tv);
        ansFaq_tv.setText(ansFaq);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
