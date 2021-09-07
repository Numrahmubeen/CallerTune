package com.caller.tune;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutUsActivity extends AppCompatActivity {
    LinearLayout fb_ll, gMail_ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        fb_ll = findViewById(R.id.fb_ll);
        gMail_ll = findViewById(R.id.gmail_ll);

        fb_ll.setOnClickListener(v -> {
            if (isAppInstalled()) {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL(this);
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);

            } else {
                Toast.makeText(getApplicationContext(), "facebook app not installing", Toast.LENGTH_SHORT).show();
            }
        });
        gMail_ll.setOnClickListener(v -> {
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
    public static String FACEBOOK_URL = "https://www.facebook.com/AppSuiteCo";
    public static String FACEBOOK_PAGE_ID = "https://www.facebook.com/AppSuiteCo";

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.orca", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }



    public boolean isAppInstalled() {
        try {
            getApplicationContext().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
