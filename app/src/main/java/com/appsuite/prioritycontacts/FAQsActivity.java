package com.appsuite.prioritycontacts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ExpandableListView;

import com.appsuite.prioritycontacts.adapter.ExpandableFaqsAdapter;

public class FAQsActivity extends AppCompatActivity {
    private ExpandableListView faqs_elv;
    private int lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);

        faqs_elv = findViewById(R.id.faqs_list);
        ExpandableFaqsAdapter adapter = new ExpandableFaqsAdapter(this);
        faqs_elv.setAdapter(adapter);
        faqs_elv.setOnGroupExpandListener(groupPosition -> {
            if (lastPosition != -1
                    && groupPosition != lastPosition) {
                faqs_elv.collapseGroup(lastPosition);
            }
            lastPosition = groupPosition;
        });
    }
}