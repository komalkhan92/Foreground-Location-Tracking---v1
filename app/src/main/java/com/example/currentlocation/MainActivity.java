package com.example.currentlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity
{

    private final int REQUEST_CODE_LOCATION_PERMISSION = 1; //CHECK IN SUAD LOCATION
    private ToggleButton toggle; // used to switch the service on and off

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // create toggle button
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        // set a listener for interactions
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            // handle toggle button
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // when clicked - start or stop the service according to the users choice
                if (isChecked)
                {
                   // if the app didn't get the required permissions
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        // request fine location permission
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                    }
                    else
                    {
                        startLocationService();
                    }
                }
                else
                {
                    stopLocationService();
                }
            }
        });
    }

    // DOCUMENTATION FROM LAST BASIC
    // Callback for the result from requesting permissions.
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                startLocationService();
            }
            else
            {
                Toast.makeText(this, "You must enable these permissions inorder to to use this app", Toast.LENGTH_SHORT);
            }
        }
    }

    private boolean isLocationServiceRunning()
    {
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null)
        {
            // does -
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE))
            {
                // if location service is running
                if(LocationService.class.getName().equals(service.service.getClassName()))
                {
                    // if the service is running as a foreground service
                    if(service.foreground)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    // next two functions - check last project

    // add description
    private void startLocationService()
    {
        // if location service isn't running
        if(!isLocationServiceRunning())
        {
            //
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location Service has started",Toast.LENGTH_SHORT).show();
        }
    }

    // add description
    private void stopLocationService()
    {
        // if location service is running
        if(isLocationServiceRunning())
        {
            //
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_END_SERVICE);
            //CHANGE!!!!
            startService(intent);
            Toast.makeText(this, "Location Service stopped", Toast.LENGTH_SHORT).show();
        }
    }
}