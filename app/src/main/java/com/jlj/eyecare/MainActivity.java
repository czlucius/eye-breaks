package com.jlj.eyecare;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    public static final int REQUEST_CODE = 10000;
    private Intent breakIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE);
            Toast.makeText(this, getString(R.string.permission_req_string, getString(R.string.app_name)) + " and enable permissions.", Toast.LENGTH_SHORT).show();
            finish(); // Stop the app, Service would not be started, as S.A.W. permission is not given
        }

        NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHost)).getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);

        breakIntent = new Intent(this, BreaksService.class);

        startService(breakIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // Stop service
                stopService(breakIntent);
                // Close the app
                finish();
            }
        }
    }
}