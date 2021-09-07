package com.caller.tune;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.caller.tune.adapter.ViewPagerAdapter;
import com.caller.tune.data.MyDbHandler;
import com.caller.tune.fragments.PriorityFragment;
import com.caller.tune.models.ContactModel;
import com.caller.tune.params.Params;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import static android.Manifest.permission.WRITE_CONTACTS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private MyDbHandler db;
    private String[] titles = new String[]{"Priority Contacts", "Phone", "Recent"};
    TabLayout tabLayout;

    private ActivityResultContracts.RequestMultiplePermissions requestMultiplePermissionsContract;
    private ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher;
    final String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        managePermissions();

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }
        db = new MyDbHandler(this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        viewPager = findViewById(R.id.mypager);
        pagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager rm = (RoleManager) this.getSystemService(Context.ROLE_SERVICE);
            startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER), 120);
        } else {
            TelecomManager systemService = this.getSystemService(TelecomManager.class);
            if (systemService != null && !systemService.getDefaultDialerPackage().equals(this.getPackageName())) {
                startActivity((new Intent(ACTION_CHANGE_DEFAULT_DIALER)).putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.getPackageName()));
            }
        }
        //inflating tab layout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        //displaying tabs
        new TabLayoutMediator(tabLayout, viewPager,(tab, position) ->tab.setText(titles[position])).attach();
        requestMutePermissions();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_PHONE_STATE},3);
        }

    }

    private void managePermissions() {
        requestMultiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionActivityResultLauncher = registerForActivityResult(requestMultiplePermissionsContract, isGranted -> {
            if (isGranted.containsValue(false)) {
                multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
            }
        });
        if (!hasPermissions(PERMISSIONS)) {
            multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
        }
    }



    private boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    void refreshFragments()
    {
        viewPager.setAdapter(null);
        pagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager,(tab, position) ->tab.setText(titles[position])).attach();
    }
    private void requestMutePermissions() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void showAddNumberDialog(Context context)
    {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_add_number);

        EditText numberEt = dialog.findViewById(R.id.number_et);
        EditText nameEt = dialog.findViewById(R.id.name_et);
        Spinner spinner = dialog.findViewById(R.id.numberTyp_sp);
        CheckBox shouldAddToContacts = dialog.findViewById(R.id.shouldAddToPhone);
        TextView cancel_tv = dialog.findViewById(R.id.cancel_tv);
        TextView add_tv = dialog.findViewById(R.id.add_tv);

        cancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        add_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameEt.getText().length()>0 && numberEt.getText().length()>0)
                {
                    MyDbHandler db = new MyDbHandler(context);

                    ContactModel contact = new ContactModel();
                    contact.setId(numberEt.getText().toString()+System.currentTimeMillis());
                    contact.setName(nameEt.getText().toString());
                    contact.setMobileNumber(numberEt.getText().toString());
                    contact.setPhotoUri(null);
                    contact.setCallRingMode(Params.AM_RING_MODE);
                    contact.setMsgRingMode(Params.AM_RING_MODE);

                    if(shouldAddToContacts.isChecked()){
                        if (ContextCompat.checkSelfPermission(context, WRITE_CONTACTS) != PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, WRITE_CONTACTS)) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{WRITE_CONTACTS}, 11);
                            } else {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{WRITE_CONTACTS}, 11);
                            }
                        }
                        else {
                            long cId = Params.addContact(nameEt.getText().toString(),numberEt.getText().toString(),spinner.getSelectedItem().toString(),context);
                            contact.setId(String.valueOf(cId));
                            db.addContact(contact);
                            Toast.makeText(context, "New Contact Successfully Saved!", Toast.LENGTH_SHORT).show();

                        }
                    }
                    else {
                        db.addContact(contact);
                        Toast.makeText(context, "Contact Successfully Saved as Priority Contact!", Toast.LENGTH_SHORT).show();

                    }
                    refreshFragments();
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(context, "Please Enter complete details.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();


            }
        });

        dialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_add_contact_manually) {
            showAddNumberDialog(this);

        }else if (id == R.id.ring_all) {
            ArrayList<ContactModel> contacts = db.getAllContacts();
            if(contacts.size()>0)
            {
                for (ContactModel c:contacts)
                {
                    c.setCallRingMode(Params.AM_RING_MODE);
                    db.updateContact(c);
                }
                Toast.makeText(this, "All contact's call and message are on Ringtone Mode.", Toast.LENGTH_SHORT).show();
                refreshFragments();
            }

        }else if (id == R.id.vibrate_all) {
            ArrayList<ContactModel> contacts = db.getAllContacts();
            if(contacts.size()>0)
            {
                for (ContactModel c:contacts)
                {
                    c.setCallRingMode(Params.AM_VIBRATE_MODE);
                    db.updateContact(c);
                }
                Toast.makeText(this, "All contact's call and message are on Vibrate Mode.", Toast.LENGTH_SHORT).show();
                refreshFragments();
            }

        }else if (id == R.id.mute_all) {
            ArrayList<ContactModel> contacts = db.getAllContacts();
            if(contacts.size()>0)
            {
                for (ContactModel c:contacts)
                {
                    c.setCallRingMode(Params.AM_SILENT_MODE);
                    db.updateContact(c);
                }
                Toast.makeText(this, "All contact's call and message are on Silent Mode.", Toast.LENGTH_SHORT).show();
                refreshFragments();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_schedule_activate) {

        } else if (id == R.id.nav_upgrade) {

        } else if (id == R.id.nav_faq) {

        } else if (id == R.id.nav_rate_us) {

        }else if (id == R.id.nav_info) {
            Intent intent = new Intent(MainActivity.this,AboutUsActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_invite_friends) {

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
// If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.d
            super.onBackPressed();
        } else {
// Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

}
