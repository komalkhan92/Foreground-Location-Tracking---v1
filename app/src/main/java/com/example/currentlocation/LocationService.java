package com.example.currentlocation;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class LocationService extends Service
{
    private LocationCallback locationCallBack ; //Used for receiving notifications from the FusedLocationProviderApi
                                                // when the device location has changed or can no longer be determined
    private Location location; // user's location

    // triggered when starting the service (every single time)
    @Override
    public int onStartCommand(Intent intent, int flag, int startId)
    {
        locationCallBack = new LocationCallback()
        {
            //event that is triggered whenever the update interval is met
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);
                // if the location is available
                if(locationResult!=null && locationResult.getLastLocation()!=null)
                {
                    //handle location result
                     location = locationResult.getLastLocation();
                     double  latitude = location.getLatitude();
                     double longitude = location.getLongitude();
                     Log.d("Location update", latitude + ", " + longitude + ", " + getUserAddress(location));
                }
            }
        };
        // start location service
        startLocationService();
        //  return the value that indicates what semantics the system should use for the service's current started state
        return super.onStartCommand(intent, flag, startId);
    }

    // called through stopService() and stops the service
    @Override
    public void onDestroy()
    {
        stopLocationService();
    }


    // why not public?
    @SuppressLint("MissingPermission")
    private void startLocationService()
    {
        // set notification channel id
        String channel_id = "location_notification_channel"; //MOVE&document / constant
        // get notification manager
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class); // will take to the mainActivity when notification is clicked
        // to set it on a notification, must create a pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT); //Flag indicating that if the described PendingIntent already exists, then keep it but replace its extra data with what is in this new Intent.
        // create the notification builder (set it's params)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id);
        builder.setSmallIcon(R.drawable.ic_baseline);
        builder.setContentTitle("Tracking Location...");
        builder.setContentText("Tracking your location... You may disable the service at any time");
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle()); // make notification expandable
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT); // he did max (and added defaults)

        // notification is necessary only if the version is over 26
        // check where was put it my service
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // if notification channel is empty???
            if(notificationManager != null && notificationManager.getNotificationChannel(channel_id) == null)
            {
                // create the notification channel?
                NotificationChannel notificationChannel = new NotificationChannel(channel_id, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("Description - This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // tidy!!!
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // set interval in which I want to get location in
        locationRequest.setFastestInterval(2000);
        // according to button
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // see how was handled in main
        // check whats looper
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
        // start service!
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());



    }


    private void stopLocationService()
    {
        // see how was handled in main
        // check whats looper
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallBack);
        // stop the service and remove the notification
        stopForeground(true);
        stopSelf();
    }


    private String getUserAddress(Location lastLocation)
    {
        // get address from location and show it
        Geocoder geocoder = new Geocoder(LocationService.this);
        try
        {
            List<Address> addressList = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), Constants.MAX_RESULTS);
            return (addressList.get(0).getAddressLine(0));

        }
        catch (Exception e)
        {
           return("Unable to get address");
        }
    }


    // used only in bound services yet must be overridden
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
