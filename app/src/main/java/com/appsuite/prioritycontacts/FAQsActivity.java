package com.appsuite.prioritycontacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.widget.ExpandableListView;

import com.appsuite.prioritycontacts.adapter.ExpandableFaqsAdapter;

public class FAQsActivity extends AppCompatActivity {
    private ExpandableListView faqs_elv;
    private int lastPosition = -1;
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
            if (!getApplicationContext().getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                Intent intent = new Intent(this, PermissionActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

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