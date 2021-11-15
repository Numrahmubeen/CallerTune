package com.appsuite.prioritycontacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.widget.ImageView;
import android.widget.Toast;

public class AboutUsActivity extends AppCompatActivity {
    private CardView goto_other_app_cv;
    private ImageView facebook_iv, gmail_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        facebook_iv = findViewById(R.id.about_facebook_iv);
        gmail_iv = findViewById(R.id.about_gmail_iv);

        goto_other_app_cv = findViewById(R.id.goto_other_app_cv);

        goto_other_app_cv.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.other_app_link))));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.other_app_link))));
            }
        });

        facebook_iv.setOnClickListener(v -> {
            Intent facebookAppIntent;
            try {
                facebookAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/568690480215566"));
                startActivity(facebookAppIntent);
            } catch (ActivityNotFoundException e) {
                facebookAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/AppSuiteCo"));
                startActivity(facebookAppIntent);
            }
        });
        gmail_iv.setOnClickListener(v -> {
            String to = "appsuiteco@gmail.com";
            String subject = "I'd like to contact from Priority Contacts";
            String msg = "Hello AppSuite," +
                    "\n \n \n \n \n" +
                    "Best regards \n" +
                    "User Name";
            Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
            selectorIntent.setData(Uri.parse("mailto:"));

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, msg);
            emailIntent.setSelector(selectorIntent);

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email using..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
            }

        });

    }
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
}
