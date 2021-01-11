// final consistent location tracker
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
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class LocationService extends Service
{
    private String TAG = LocationService.class.getSimpleName();
    private static final String CHANNEL_ID = "location_notification_channel"; // notification channel id
    private static final String CHANNEL_NAME = "Location Service"; // The user visible name of the notification channel
    private static final String CHANNEL_DESCRIPTION = "This channel is used by location service"; // notification channel description
    private static final int REQUEST_CODE = 0; // private request code for the sender of the pending intent
    public static final int DEFAULT_INTERVAL = 4000; // default location update interval
    public static final int FASTEST_INTERVAL = 2000; // fastest location update interval
    private LocationCallback locationCallBack ; //Used for receiving notifications from the FusedLocationProviderApi
                                                // when the device location has changed or can no longer be determined
    private Location location; // last known location or updated location
    private LocationRequest locationRequest; //

    // triggered when starting the service (every single time)

    @Override
    public void onCreate() {
        super.onCreate();
        locationCallBack = new LocationCallback()
        {
            //event that is triggered whenever the update interval is met
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                Log.d("Location update", "*****onLocationResult, before if****");
                super.onLocationResult(locationResult);
                // if the location is available
                if(locationResult!=null && locationResult.getLastLocation()!=null)
                {
                    //handle location result
                    location = locationResult.getLastLocation();
                    double  latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d(TAG ,latitude + ", " + longitude + ", " + getUserAddress());
                    try {
                        SockMngr.sendAndReceive(location.getLatitude() + "," + location.getLongitude());
                        Log.d(TAG, SockMngr.response);
                        // if an alert is received
                        if(SockMngr.response.equals("CODE RED"))
                        {
                            Log.d(TAG, "EXPOSED");
                            // pop notification
//                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                                    .setSmallIcon(R.drawable.ic_baseline_error_24)
//                                    .setContentTitle("ALERT")
//                                    .setContentText("You are exposed to a person with Covid-19 ")
//                                    .setAutoCancel(true)
//                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                            notificationManager.notify(1111, builder.build());
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "*****exception****");
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "*****finished function");
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId)
    {


        // start location service
        startLocationService();
        //  return the value that indicates what semantics the system should use for the service's current started state
        return START_STICKY;
    }

    // called through stopService() and stops the service
    @Override
    public void onDestroy()
    {
        Log.d(TAG, "*****DESTROY****");
        stopLocationService();
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG,"STOP SERVICE");
        stopLocationService();
        return super.stopService(name);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG,"ON TASK REMOVED");

        super.onTaskRemoved(rootIntent);
    }

    // starts the service
    @SuppressLint("MissingPermission")
    private void startLocationService()
    {
        /// get notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class); // will take to the mainActivity when notification is clicked
        // to set it on a notification, must create a pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT); //Flag indicating that if the described PendingIntent already exists, then keep it but replace its extra data with what is in this new Intent.
        // create and configure the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        configNotification(builder, pendingIntent);


        // notification is necessary only if the version is over 26
        // check where was put it my service
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // if notification channel is empty
            if(notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null)
            {
                // create the notification channel
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME


                        , NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(CHANNEL_DESCRIPTION);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // create and configure location request
         locationRequest = new LocationRequest();
        configLocationRequest();

        // request location updates according to the parameters in the locationRequest - callBacks performed on the mainLooper (in the mainThread)
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
        // start service!
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());



    }


    private void stopLocationService()
    {
        Log.d("location", "*****STOPPED****");
        // Remove all location updates for the given location result listener
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallBack);
        // stop the service and remove the notification
        stopForeground(true);
        stopSelf();
    }




    private String getUserAddress()
    {
        // get address from location and show it
        Geocoder geocoder = new Geocoder(LocationService.this);
        try
        {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), Constants.MAX_RESULTS);
            return (addressList.get(0).getAddressLine(0));

        }
        catch (Exception e)
        {
           return("Unable to get address");
        }
    }

    //  set the various fields of the notification
    public void configNotification(NotificationCompat.Builder builder, PendingIntent pendingIntent)
    {
        builder.setSmallIcon(R.drawable.ic_baseline);
        builder.setContentTitle("Tracking Location");
        builder.setContentText("Tracking your location... You may disable the service at any time");
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle()); // make notification expandable
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    // configure location request
    public void configLocationRequest()
    {
        locationRequest.setInterval(DEFAULT_INTERVAL); // set interval in which we want to get location in
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // used only in bound services yet must be overridden
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
