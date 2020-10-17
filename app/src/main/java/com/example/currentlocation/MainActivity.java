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

import static com.example.currentlocation.Constants.EMPTY;
import static com.example.currentlocation.Constants.PERMISSION_PLACE;

public class MainActivity extends AppCompatActivity
{


    private final static int PERMISSION_FINE_LOCATION = 1; //permission request number, identifies the permission
    private ToggleButton toggle; // used to switch the service on and off

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // create toggle button
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        // present the toggle button as on if the app has been opened again and the service is still running
        if(isLocationServiceRunning())
        {
            toggle.setChecked(true);
        }
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
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
                    }
                    else
                    {
                        // the app has permission - start location service
                        startLocationService();
                    }
                }
                else
                {
                    // the button has been switched off - stop location service
                    stopLocationService();
                }
            }
        });
    }


    // Callback for the result from requesting permissions.
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        // if permission is granted
        if(requestCode == PERMISSION_FINE_LOCATION && grantResults.length>EMPTY)

        {
            if(grantResults[PERMISSION_PLACE] == PackageManager.PERMISSION_GRANTED)
            {
                // start location service
                startLocationService();
            }
            else
            {
                // didn't get permission, notify user
                Toast.makeText(this, "You must enable these permissions inorder to to use this app", Toast.LENGTH_SHORT);
            }
        }
    }

    // returns is the location service running (as foreground)
    private boolean isLocationServiceRunning()
    {
        // get to the handle to the ActivityManager for interacting with the global activity state of the system.
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null)
        {
            // goes over every service in the system that is currently running
            // get running services - returns a list of RunningServiceInfo records describing each of the running tasks.
            // the max_value represents the maximum number of entries to return in the list
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE))
            {
                // if the running service being inspected is the location service
                if(LocationService.class.getName().equals(service.service.getClassName()))
                {
                    // check if its running as foreground
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