package com.appsuite.prioritycontacts;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.Button;

import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

public class PermissionActivity extends AppCompatActivity {
    Button setDefault_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        setDefault_bt = findViewById(R.id.setDefault_bt);
        ActivityResultLauncher<Intent> defaultPhoneRequest = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = new Intent(PermissionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

        setDefault_bt.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
                if (!getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        RoleManager rm = (RoleManager) this.getSystemService(Context.ROLE_SERVICE);
                        defaultPhoneRequest.launch(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER));
//                        startActivityForResult(, 120);
                    }
                }

            }
            else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                TelecomManager systemService = this.getSystemService(TelecomManager.class);
                if (systemService != null && !systemService.getDefaultDialerPackage().equals(this.getPackageName())) {
                    defaultPhoneRequest.launch((new Intent(ACTION_CHANGE_DEFAULT_DIALER)).putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.getPackageName()));
                }
            }
        });
    }
}